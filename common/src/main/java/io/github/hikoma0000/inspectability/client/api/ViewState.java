package io.github.hikoma0000.inspectability.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface ViewState {
    String getId();

    String getTranslationKey();

    ItemDisplayContext getContext();

    default int getVariantCount(Minecraft mc, ItemStack stack) {
        return 1;
    }
}
