package io.github.hikoma0000.inspectability.mixin.client;

import io.github.hikoma0000.inspectability.client.util.HeldItemTransformManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    private void hideHudInInspectorMode(Gui instance, GuiGraphics guiGraphics, float partialTick) {
        if (!HeldItemTransformManager.isInspectorMode()) {
            instance.render(guiGraphics, partialTick);
        }
    }
}
