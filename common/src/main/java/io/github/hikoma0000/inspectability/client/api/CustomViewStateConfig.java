package io.github.hikoma0000.inspectability.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.function.Consumer;

public class CustomViewStateConfig implements ViewState {
    private final String id;
    private final String translationKey;
    private final ItemDisplayContext context;
    private final Variant[] variants;
    private final Quaternionf initialRotation;
    private final float scaleMultiplier;
    private final Vector3f offset;
    private final boolean hideDefaultItem;
    private final boolean hideDefaultBlock;
    private final VariantCountProvider variantCountProvider;
    private final Consumer<ViewStateRenderContext> ticker;

    private CustomViewStateConfig(Builder builder) {
        this.id = builder.id;
        this.translationKey = builder.translationKey;
        this.context = builder.context;
        this.variants = builder.variants;
        this.initialRotation = builder.initialRotation;
        this.scaleMultiplier = builder.scaleMultiplier;
        this.offset = builder.offset;
        this.hideDefaultItem = builder.hideDefaultItem;
        this.hideDefaultBlock = builder.hideDefaultBlock;
        this.variantCountProvider = builder.variantCountProvider;
        this.ticker = builder.ticker;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public ItemDisplayContext getContext() {
        return context;
    }

    @Override
    public int getVariantCount(Minecraft mc, ItemStack stack) {
        if (variantCountProvider != null) {
            return variantCountProvider.getCount(mc, stack);
        }
        return Math.max(1, variants.length);
    }

    @Nullable
    public Entity createEntity(Minecraft mc, ItemStack stack, int variantIndex) {
        if (variants == null || variants.length == 0) return null;
        int index = Math.min(variantIndex, variants.length - 1);
        return variants[index] != null ? variants[index].createEntity(mc, stack) : null;
    }

    @Nullable
    public ItemStack createItem(Minecraft mc, ItemStack stack, int variantIndex) {
        if (variants == null || variants.length == 0) return null;
        int index = Math.min(variantIndex, variants.length - 1);
        return variants[index] != null ? variants[index].createItem(mc, stack) : null;
    }

    public float getVariantScaleMultiplier(int variantIndex) {
        if (variants == null || variants.length == 0) return 1.0f;
        int index = Math.min(variantIndex, variants.length - 1);
        return variants[index] != null ? variants[index].getScaleMultiplier() : 1.0f;
    }

    public Quaternionf getInitialRotation() {
        return initialRotation;
    }

    public float getScaleMultiplier() {
        return scaleMultiplier;
    }

    public Vector3f getOffset() {
        return offset;
    }

    public boolean isHideDefaultItem() {
        return hideDefaultItem;
    }

    public boolean isHideDefaultBlock() {
        return hideDefaultBlock;
    }

    public Consumer<ViewStateRenderContext> getTicker() {
        return ticker;
    }

    public interface Variant {
        @Nullable
        Entity createEntity(Minecraft mc, ItemStack stack);

        @Nullable
        ItemStack createItem(Minecraft mc, ItemStack stack);

        default float getScaleMultiplier() {
            return 1.0f;
        }
    }

    public static Variant entity(EntityFactory factory) {
        return new Variant() {
            @Override
            public Entity createEntity(Minecraft mc, ItemStack stack) {
                return factory.create(mc, stack);
            }

            @Override
            public ItemStack createItem(Minecraft mc, ItemStack stack) {
                return null;
            }
        };
    }

    public static Variant item(ItemStackFactory factory) {
        return new Variant() {
            @Override
            public Entity createEntity(Minecraft mc, ItemStack stack) {
                return null;
            }

            @Override
            public ItemStack createItem(Minecraft mc, ItemStack stack) {
                return factory.create(mc, stack);
            }
        };
    }

    public static Variant item(Item item) {
        return item((mc, stack) -> new ItemStack(item));
    }

    public interface EntityFactory {
        @Nullable
        Entity create(Minecraft mc, ItemStack stack);
    }

    public interface ItemStackFactory {
        @Nullable
        ItemStack create(Minecraft mc, ItemStack stack);
    }

    public interface VariantCountProvider {
        int getCount(Minecraft mc, ItemStack stack);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static class Builder {
        private final String id;
        private String translationKey;
        private ItemDisplayContext context = ItemDisplayContext.NONE;
        private Variant[] variants = new Variant[0];
        private Quaternionf initialRotation = new Quaternionf().rotateX((float) Math.toRadians(30)).rotateY((float) Math.toRadians(45));
        private float scaleMultiplier = 1.0f;
        private Vector3f offset = new Vector3f(0.0f, 0.0f, 0.0f);
        private boolean hideDefaultItem = false;
        private boolean hideDefaultBlock = false;
        private VariantCountProvider variantCountProvider = null;
        private Consumer<ViewStateRenderContext> ticker = null;

        public Builder(String id) {
            this.id = id;
            this.translationKey = "inspectability.state." + id;
        }

        public Builder translate(String key) {
            this.translationKey = key;
            return this;
        }

        public Builder entity(EntityFactory factory) {
            this.variants = new Variant[] { CustomViewStateConfig.entity(factory) };
            return this;
        }

        public Builder item(ItemStackFactory factory) {
            this.variants = new Variant[] { CustomViewStateConfig.item(factory) };
            return this;
        }

        public Builder item(Item item) {
            this.variants = new Variant[] { CustomViewStateConfig.item(item) };
            return this;
        }

        public Builder variants(Variant... variants) {
            this.variants = variants;
            return this;
        }

        public Builder hideDefaultItem() {
            this.hideDefaultItem = true;
            return this;
        }

        public Builder hideDefaultBlock() {
            this.hideDefaultBlock = true;
            return this;
        }

        public Builder hideDefaultStates() {
            this.hideDefaultItem = true;
            this.hideDefaultBlock = true;
            return this;
        }

        public Builder rotation(Quaternionf rot) {
            this.initialRotation = rot;
            return this;
        }

        public Builder scale(float scale) {
            this.scaleMultiplier = scale;
            return this;
        }

        public Builder offset(Vector3f offset) {
            this.offset = offset;
            return this;
        }

        public Builder offset(float x, float y, float z) {
            this.offset = new Vector3f(x, y, z);
            return this;
        }

        public Builder displayContext(ItemDisplayContext context) {
            this.context = context;
            return this;
        }

        public Builder variants(VariantCountProvider provider) {
            this.variantCountProvider = provider;
            return this;
        }

        public Builder variants(int count) {
            this.variantCountProvider = (mc, stack) -> count;
            return this;
        }

        public Builder ticker(Consumer<ViewStateRenderContext> ticker) {
            this.ticker = ticker;
            return this;
        }

        public CustomViewStateConfig build() {
            return new CustomViewStateConfig(this);
        }
    }
}
