package io.github.hikoma0000.inspectability.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Equipable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InspectorStateRegistry {
    private static final Map<Item, List<CustomViewStateConfig>> ITEM_CONFIGS = new HashMap<>();
    private static final List<ClassConfigPair> CLASS_CONFIGS = new ArrayList<>();

    public static void register(Item item, CustomViewStateConfig... configs) {
        List<CustomViewStateConfig> list = ITEM_CONFIGS.computeIfAbsent(item, k -> new ArrayList<>());
        for (CustomViewStateConfig config : configs) {
            list.add(config);
        }
    }

    public static void register(Class<? extends Item> itemClass, CustomViewStateConfig... configs) {
        for (CustomViewStateConfig config : configs) {
            CLASS_CONFIGS.add(new ClassConfigPair(itemClass, config));
        }
    }

    public static List<CustomViewStateConfig> getConfigs(Item item) {
        List<CustomViewStateConfig> result = new ArrayList<>();
        if (ITEM_CONFIGS.containsKey(item)) {
            result.addAll(ITEM_CONFIGS.get(item));
        }
        for (ClassConfigPair pair : CLASS_CONFIGS) {
            if (pair.itemClass.isInstance(item)) {
                result.add(pair.config);
            }
        }
        return result;
    }

    public static List<ViewState> getStates(Minecraft mc, ItemStack stack) {
        Map<String, ViewState> states = new LinkedHashMap<>();
        
        if (stack.isEmpty()) {
            return new ArrayList<>(states.values());
        }

        List<CustomViewStateConfig> configs = getConfigs(stack.getItem());
        boolean hideItem = false;
        boolean hideBlock = false;

        for (CustomViewStateConfig config : configs) {
            if (config.isHideDefaultItem()) hideItem = true;
            if (config.isHideDefaultBlock()) hideBlock = true;
        }

        if (!hideItem) {
            states.put(StandardViewState.ITEM.getId(), StandardViewState.ITEM);
        }
        if (!hideBlock && stack.getItem() instanceof BlockItem) {
            states.put(StandardViewState.BLOCK.getId(), StandardViewState.BLOCK);
        }

        for (CustomViewStateConfig config : configs) {
            states.put(config.getId(), config);
        }

        if (!states.containsKey("entity")) {
            Entity dummyEntity = null;
            if (stack.getItem() instanceof SpawnEggItem spawnEgg) {
                EntityType<?> type = spawnEgg.getType(stack.getTag());
                if (type != null) {
                    dummyEntity = type.create(mc.level);
                }
            } else if (stack.getItem() == Items.PAINTING) {
                dummyEntity = new Painting(EntityType.PAINTING, mc.level);
            }

            if (dummyEntity != null) {
                if (dummyEntity instanceof VariantHolder<?> variantHolder) {
                    Object currentVariant = variantHolder.getVariant();
                    boolean isHolder = currentVariant instanceof Holder;
                    Object rawVariant = isHolder ? ((Holder<?>) currentVariant).value() : currentVariant;

                    List<Object> allVariantsToSet = new ArrayList<>();

                    if (rawVariant != null && rawVariant.getClass().isEnum()) {
                        allVariantsToSet.addAll(Arrays.asList(rawVariant.getClass().getEnumConstants()));
                    } else if (rawVariant != null) {
                        for (Registry<Object> registry : (Iterable<Registry<Object>>)(Object)BuiltInRegistries.REGISTRY) {
                            ResourceLocation key = registry.getKey(rawVariant);
                            if (key != null && registry.get(key) == rawVariant) {
                                registry.forEach(v -> {
                                    if (isHolder) {
                                        allVariantsToSet.add(registry.wrapAsHolder(v));
                                    } else {
                                        allVariantsToSet.add(v);
                                    }
                                });
                                break;
                            }
                        }
                    }

                    if (!allVariantsToSet.isEmpty()) {
                        CustomViewStateConfig.Variant[] dynamicVariants = new CustomViewStateConfig.Variant[allVariantsToSet.size()];
                        final EntityType<?> dummyType = dummyEntity.getType();
                        for (int i = 0; i < allVariantsToSet.size(); i++) {
                            final Object varToSet = allVariantsToSet.get(i);
                            dynamicVariants[i] = CustomViewStateConfig.entity((m, s) -> {
                                Entity e = dummyType.create(m.level);
                                if (e instanceof VariantHolder vh) {
                                    ((VariantHolder<Object>) vh).setVariant(varToSet);
                                }
                                return e;
                            });
                        }
                        states.put("entity", CustomViewStateConfig.builder("entity").variants(dynamicVariants).build());
                    } else {
                        final EntityType<?> dummyType = dummyEntity.getType();
                        states.put("entity", CustomViewStateConfig.builder("entity").entity((m, s) -> dummyType.create(m.level)).build());
                    }
                } else {
                    final EntityType<?> dummyType = dummyEntity.getType();
                    states.put("entity", CustomViewStateConfig.builder("entity").entity((m, s) -> dummyType.create(m.level)).build());
                }
            }
        }

        if (!states.containsKey("equipment")) {
            EquipmentSlot slot = Mob.getEquipmentSlotForItem(stack);
            boolean isEquipable = Equipable.get(stack) != null;
            if (slot.getType() == EquipmentSlot.Type.ARMOR || isEquipable || slot == EquipmentSlot.OFFHAND) {
                states.put("equipment", CustomViewStateConfig.builder("equipment").variants(
                    CustomViewStateConfig.entity((m, s) -> {
                        ArmorStand stand = new ArmorStand(m.level, 0.0D, 0.0D, 0.0D);
                        stand.setShowArms(true);
                        stand.setInvisible(true);
                        stand.setItemSlot(slot, s.copy());
                        return stand;
                    }),
                    CustomViewStateConfig.entity((m, s) -> {
                        ArmorStand stand = new ArmorStand(m.level, 0.0D, 0.0D, 0.0D);
                        stand.setShowArms(true);
                        stand.setInvisible(false);
                        stand.setItemSlot(slot, s.copy());
                        return stand;
                    })
                ).build());
            }
        }

        return new ArrayList<>(states.values());
    }

    private static class ClassConfigPair {
        final Class<? extends Item> itemClass;
        final CustomViewStateConfig config;

        ClassConfigPair(Class<? extends Item> itemClass, CustomViewStateConfig config) {
            this.itemClass = itemClass;
            this.config = config;
        }
    }
}
