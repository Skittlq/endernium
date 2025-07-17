package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EnderniumPickaxe extends PickaxeItem {
    public EnderniumPickaxe(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player) entity : null;
        if (!level.isClientSide && !player.isCreative()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            LootParams.Builder builder = new LootParams.Builder(((ServerLevel) level))
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, stack)
                    .withParameter(LootContextParams.BLOCK_STATE, state);

            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.sendParticles(ModParticles.REVERSE_ENDERNIUM_BIT.get(),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.01);
            level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.04F, 1.5F);


            if (blockEntity != null) {
                builder.withParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
            }

            List<ItemStack> drops = state.getDrops(builder);

            if (state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)) {
                level.removeBlock(pos, false);
                return super.mineBlock(stack, level, state, pos, entity);
            }

            for (ItemStack drop : drops) {
                if (!player.getInventory().add(drop)) {
                    ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), drop);
                    level.addFreshEntity(itemEntity);
                }
            }

            level.removeBlock(pos, false);
        }

        return super.mineBlock(stack, level, state, pos, entity);
    }
}
