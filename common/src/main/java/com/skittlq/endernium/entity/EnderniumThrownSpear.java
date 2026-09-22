package com.skittlq.endernium.entity;

import com.skittlq.endernium.item.EnderniumItems;
import com.skittlq.endernium.item.tools.EnderniumSpear;
import com.skittlq.endernium.particles.EnderniumParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class EnderniumThrownSpear extends ThrowableItemProjectile {
    private static final int MAX_FLIGHT_TICKS = 20 * 10;
    private InteractionHand returnHand = InteractionHand.MAIN_HAND;
    private float meleeDamage = 1.0F;
    private boolean resolved;

    public EnderniumThrownSpear(EntityType<? extends EnderniumThrownSpear> type, Level level) {
        super(type, level);
    }

    public EnderniumThrownSpear(
            Level level,
            LivingEntity owner,
            ItemStack stack,
            InteractionHand returnHand,
            float meleeDamage
    ) {
        super(EnderniumEntityTypes.thrownSpear(), owner, level, stack);
        this.returnHand = returnHand;
        this.meleeDamage = meleeDamage;
    }

    @Override
    protected Item getDefaultItem() {
        return EnderniumItems.ENDERNIUM_SPEAR.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    public Entity getOwner() {
        Entity currentOwner = super.getOwner();
        if (currentOwner != null || !(level() instanceof ServerLevel serverLevel) || owner == null) {
            return currentOwner;
        }
        // The projectile can be in another dimension while its thrower stays behind.
        return serverLevel.getServer().getPlayerList().getPlayer(owner.getUUID());
    }

    public boolean belongsTo(UUID playerId) {
        return !resolved && owner != null && owner.getUUID().equals(playerId);
    }

    public InteractionHand returnHand() {
        return returnHand;
    }

    public ItemStack takeForDisconnect(ServerPlayer player) {
        if (!belongsTo(player.getUUID())) {
            return ItemStack.EMPTY;
        }
        resolved = true;
        ItemStack returnedStack = getItem().copy();
        returnedStack.hurtAndBreak(1, player, returnHand);
        discard();
        return returnedStack;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("ReturnToOffHand", returnHand == InteractionHand.OFF_HAND);
        output.putFloat("MeleeDamage", meleeDamage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        returnHand = input.getBooleanOr("ReturnToOffHand", false)
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        meleeDamage = input.getFloatOr("MeleeDamage", 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel && !resolved) {
            if (tickCount >= MAX_FLIGHT_TICKS) {
                recall(serverLevel);
                return;
            }

            // Keep the projectile simulated as it crosses unloaded terrain. This
            // is the same short-lived radius-2 ticket used by vanilla ender pearls;
            // chunks behind the spear expire automatically after 40 ticks.
            ServerPlayer.placeEnderPearlTicket(serverLevel, chunkPosition());
        }
    }

    @Override
    protected void onBelowWorld() {
        if (level() instanceof ServerLevel serverLevel && !resolved) {
            recall(serverLevel);
        } else {
            super.onBelowWorld();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!(level() instanceof ServerLevel serverLevel) || resolved) {
            return;
        }
        Entity owner = getOwner();
        Entity target = hitResult.getEntity();
        ItemStack spearStack = getItem();
        if (owner instanceof ServerPlayer player) {
            DamageSource damageSource = spearStack.getDamageSource(player);
            float damage = EnchantmentHelper.modifyDamage(
                    serverLevel, spearStack, target, damageSource, meleeDamage);
            damage += spearStack.getItem().getAttackDamageBonus(target, damage, damageSource);
            if (target.hurtServer(serverLevel, damageSource, damage)) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(
                        serverLevel, target, damageSource, spearStack);
            }
        } else {
            target.hurtServer(
                    serverLevel,
                    serverLevel.damageSources().trident(this, owner),
                    meleeDamage
            );
        }
        resolve(serverLevel, hitResult.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (level() instanceof ServerLevel serverLevel && !resolved) {
            resolve(serverLevel, hitResult.getLocation());
        }
    }

    @Override
    public void teleportToPortalDestination(ServerLevel sourceLevel, TeleportTransition projectileDestination) {
        // End gateways and End portals resolve immediately. Other portals,
        // including Nether and modded portals, let the spear fly on like a pearl.
        if (portalProcess == null || (!portalProcess.isSamePortal((Portal) Blocks.END_PORTAL)
                && !portalProcess.isSamePortal((Portal) Blocks.END_GATEWAY))) {
            super.teleportToPortalDestination(sourceLevel, projectileDestination);
            return;
        }

        Entity owner = getOwner();
        if (!(owner instanceof ServerPlayer player) || !player.isAlive()
                || player.level() != sourceLevel || !player.canUsePortal(false)) {
            recall(sourceLevel);
            return;
        }

        boolean endPortal = portalProcess.isSamePortal((Portal) Blocks.END_PORTAL);
        TeleportTransition playerDestination = portalProcess.getPortalDestination(sourceLevel, player);
        if (playerDestination == null
                || !sourceLevel.isAllowedToEnterPortal(playerDestination.newLevel())
                || !player.canTeleport(sourceLevel, playerDestination.newLevel())) {
            recall(sourceLevel);
            return;
        }

        // The first trip out of the End still shows vanilla's credits.
        if (endPortal && sourceLevel.dimension() == Level.END
                && playerDestination.newLevel().dimension() == Level.OVERWORLD
                && !player.seenCredits) {
            returnToPlayer(sourceLevel, player, position());
            player.showEndCredits();
            return;
        }

        ServerPlayer teleportedPlayer = player.teleport(playerDestination);
        if (teleportedPlayer == null) {
            recall(sourceLevel);
            return;
        }
        teleportedPlayer.setPortalCooldown();
        teleportedPlayer.resetFallDistance();
        teleportedPlayer.resetCurrentImpulseContext();
        returnToPlayer(teleportedPlayer.level(), teleportedPlayer, teleportedPlayer.position());
    }

    private void resolve(ServerLevel level, Vec3 impactPosition) {
        Entity owner = getOwner();
        if (!(owner instanceof ServerPlayer player) || !player.isAlive()) {
            resolved = true;
            spawnAtLocation(level, getItem().copy());
            discard();
            return;
        }

        if (player.level() != level) {
            if (!player.canUsePortal(true)
                    || !player.level().isAllowedToEnterPortal(level)
                    || !player.canTeleport(player.level(), level)) {
                returnToPlayer(player.level(), player, player.position());
                return;
            }
            TeleportTransition destination = new TeleportTransition(
                    level, impactPosition, Vec3.ZERO, 0.0F, 0.0F,
                    Relative.ROTATION, TeleportTransition.DO_NOTHING);
            ServerPlayer teleportedPlayer = player.teleport(destination);
            if (teleportedPlayer == null) {
                returnToPlayer(player.level(), player, player.position());
                return;
            }
            teleportedPlayer.setPortalCooldown();
            teleportedPlayer.resetFallDistance();
            teleportedPlayer.resetCurrentImpulseContext();
            returnToPlayer(level, teleportedPlayer, impactPosition);
            return;
        }

        player.teleportTo(impactPosition.x, impactPosition.y, impactPosition.z);
        player.resetFallDistance();
        returnToPlayer(level, player, impactPosition);
    }

    private void returnToPlayer(ServerLevel level, ServerPlayer player, Vec3 position) {
        resolved = true;
        ItemStack returnedStack = getItem().copy();
        returnedStack.hurtAndBreak(1, player, returnHand);
        if (player.getItemInHand(returnHand).isEmpty()) {
            player.setItemInHand(returnHand, returnedStack);
        } else if (!player.getInventory().add(returnedStack)) {
            player.drop(returnedStack, false, Prediction.SERVER_ONLY);
        }
        EnderniumSpear.beginReturnCooldown(player);
        level.playSound(null, position.x, position.y, position.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, 1.15F);
        level.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                position.x, position.y, position.z, 36, 0.45, 0.65, 0.45, 0.16);
        discard();
    }

    private void recall(ServerLevel level) {
        resolved = true;
        Vec3 recallPosition = position();
        ItemStack returnedStack = getItem().copy();
        Entity owner = getOwner();
        if (!(owner instanceof ServerPlayer player) || !player.isAlive()) {
            spawnAtLocation(level, returnedStack);
            discard();
            return;
        }

        returnedStack.hurtAndBreak(1, player, returnHand);
        if (player.getItemInHand(returnHand).isEmpty()) {
            player.setItemInHand(returnHand, returnedStack);
        } else if (!player.getInventory().add(returnedStack)) {
            player.drop(returnedStack, false, Prediction.SERVER_ONLY);
        }
        EnderniumSpear.beginReturnCooldown(player);

        level.playSound(null, recallPosition.x, recallPosition.y, recallPosition.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.65F, 1.25F);
        level.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                recallPosition.x, recallPosition.y, recallPosition.z,
                20, 0.25, 0.25, 0.25, 0.1);

        Vec3 playerPosition = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        player.level().playSound(null, playerPosition.x, playerPosition.y, playerPosition.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.75F, 1.35F);
        player.level().sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                playerPosition.x, playerPosition.y, playerPosition.z,
                30, 0.35, 0.55, 0.35, 0.12);
        discard();
    }

}
