package io.github.hikoma0000.inspectability.mixin.client;

import io.github.hikoma0000.inspectability.client.util.HeldItemTransformManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandRenderer.class, priority = 1500)
public class HeldItemTransformMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void hideArmAndItemInInspector(
            AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress,
            ItemStack stack, float equipProgress, PoseStack poseStack,
            MultiBufferSource buffers, int light, CallbackInfo ci
    ) {
        if (HeldItemTransformManager.isInspectorMode()) {
            ci.cancel();
        }
    }
}

