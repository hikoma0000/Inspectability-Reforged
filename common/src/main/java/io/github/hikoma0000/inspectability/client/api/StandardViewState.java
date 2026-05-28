package io.github.hikoma0000.inspectability.client.api;

import net.minecraft.world.item.ItemDisplayContext;

public enum StandardViewState implements ViewState {
    ITEM("item", "inspectability.state.item", ItemDisplayContext.GUI),
    BLOCK("block", "inspectability.state.block", ItemDisplayContext.FIXED),
    ENTITY("entity", "inspectability.state.entity", ItemDisplayContext.NONE);

    private final String id;
    private final String translationKey;
    private final ItemDisplayContext context;

    StandardViewState(String id, String translationKey, ItemDisplayContext context) {
        this.id = id;
        this.translationKey = translationKey;
        this.context = context;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getTranslationKey() { return translationKey; }

    @Override
    public ItemDisplayContext getContext() { return context; }
}
