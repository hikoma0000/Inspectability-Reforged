package io.github.hikoma0000.inspectability.mixin.client;

import io.github.hikoma0000.inspectability.InspectabilityClient;
import io.github.hikoma0000.inspectability.client.InspectorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (InspectabilityClient.inspectorKey.matches(keyCode, scanCode)) {
            if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
                Minecraft mc = Minecraft.getInstance();
                InspectorScreen inspector = new InspectorScreen(this.hoveredSlot.getItem(), mc.screen);
                inspector.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                mc.screen = inspector;
                cir.setReturnValue(true);
            }
        }
    }
}
