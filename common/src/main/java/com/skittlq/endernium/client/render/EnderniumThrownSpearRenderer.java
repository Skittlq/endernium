package com.skittlq.endernium.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.skittlq.endernium.entity.EnderniumThrownSpear;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;

public final class EnderniumThrownSpearRenderer
        extends EntityRenderer<EnderniumThrownSpear, EnderniumThrownSpearRenderState> {
    private final ItemModelResolver itemModelResolver;

    public EnderniumThrownSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void submit(
            EnderniumThrownSpearRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState
    ) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, state.yRot - 90.0F);
        // The spear-in-hand texture is authored diagonally at 45 degrees and points
        // opposite the trident model's forward axis. Correct both offsets so the
        // spearhead, rather than the handle, leads along the projectile velocity.
        poseStack.rotateDegrees(Axis.ZP, state.xRot + 225.0F);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        state.item.submit(
                poseStack,
                submitNodeCollector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor
        );
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, cameraState);
    }

    @Override
    public EnderniumThrownSpearRenderState createRenderState() {
        return new EnderniumThrownSpearRenderState();
    }

    @Override
    public void extractRenderState(
            EnderniumThrownSpear spear,
            EnderniumThrownSpearRenderState state,
            float partialTick
    ) {
        super.extractRenderState(spear, state, partialTick);
        state.xRot = spear.getXRot(partialTick);
        state.yRot = spear.getYRot(partialTick);
        itemModelResolver.updateForNonLiving(
                state.item,
                spear.getItem(),
                ItemDisplayContext.NONE,
                spear
        );
    }

    @Override
    protected AABB getBoundingBoxForCulling(EnderniumThrownSpear spear, float partialTick) {
        return super.getBoundingBoxForCulling(spear, partialTick).inflate(1.5);
    }
}
