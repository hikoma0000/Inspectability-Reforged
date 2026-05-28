package io.github.hikoma0000.inspectability.client.init;

import io.github.hikoma0000.inspectability.client.api.CustomViewStateConfig;
import io.github.hikoma0000.inspectability.client.api.InspectorStateRegistry;
import io.github.hikoma0000.inspectability.mixin.client.BoatItemAccessor;
import io.github.hikoma0000.inspectability.mixin.client.MinecartItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.ThrowablePotionItem;
import org.joml.Quaternionf;

public class InspectorState {
        public static void init() {

                InspectorStateRegistry.register(ArrowItem.class,
                                CustomViewStateConfig.builder("entity")
                                                .variants(
                                                                CustomViewStateConfig.entity((mc,
                                                                                stack) -> ((ArrowItem) stack.getItem())
                                                                                                .createArrow(mc.level,
                                                                                                                stack,
                                                                                                                mc.player)))
                                                .build());
                InspectorStateRegistry.register(ThrowablePotionItem.class,
                                CustomViewStateConfig.builder("entity").entity((mc, stack) -> {
                                        ThrownPotion potion = new ThrownPotion(mc.level, 0, 0, 0);
                                        potion.setItem(stack);
                                        return potion;
                                }).build());

                InspectorStateRegistry.register(Items.ARMOR_STAND, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new ArmorStand(mc.level, 0.0D, 0.0D, 0.0D))
                                .build());
                InspectorStateRegistry.register(Items.ITEM_FRAME, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new ItemFrame(mc.level, BlockPos.ZERO,
                                                Direction.SOUTH))
                                .build());
                InspectorStateRegistry.register(Items.GLOW_ITEM_FRAME, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new GlowItemFrame(mc.level, BlockPos.ZERO,
                                                Direction.SOUTH))
                                .build());

                InspectorStateRegistry.register(BoatItem.class,
                                CustomViewStateConfig.builder("entity").entity((mc, stack) -> {
                                        Boat.Type type = ((BoatItemAccessor) stack.getItem()).inspectability$getType();
                                        boolean hasChest = ((BoatItemAccessor) stack.getItem())
                                                        .inspectability$hasChest();
                                        Boat boat = hasChest ? new ChestBoat(mc.level, 0.0D, 0.0D, 0.0D)
                                                        : new Boat(mc.level, 0.0D, 0.0D, 0.0D);
                                        boat.setVariant(type);
                                        return boat;
                                }).build());

                InspectorStateRegistry.register(MinecartItem.class,
                                CustomViewStateConfig.builder("entity").entity((mc, stack) -> {
                                        AbstractMinecart.Type type = ((MinecartItemAccessor) stack.getItem())
                                                        .inspectability$getType();
                                        return AbstractMinecart.createMinecart(mc.level, 0.0D, 0.0D, 0.0D, type);
                                }).build());

                InspectorStateRegistry.register(Items.TRIDENT, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new ThrownTrident(mc.level, mc.player, stack))
                                .rotation(new Quaternionf(0.7809881f, -0.046147957f, 0.6211268f, 0.046148032f))
                                .scale(0.625f)
                                .build());
                InspectorStateRegistry.register(Items.SNOWBALL, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new Snowball(mc.level, 0.0D, 0.0D, 0.0D)).build());
                InspectorStateRegistry.register(Items.EGG, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new ThrownEgg(mc.level, 0.0D, 0.0D, 0.0D))
                                .build());
                InspectorStateRegistry.register(Items.ENDER_PEARL, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new ThrownEnderpearl(EntityType.ENDER_PEARL,
                                                mc.level))
                                .build());
                InspectorStateRegistry.register(Items.EXPERIENCE_BOTTLE, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new ThrownExperienceBottle(mc.level, 0.0D, 0.0D,
                                                0.0D))
                                .build());
                InspectorStateRegistry.register(Items.FIREWORK_ROCKET, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new FireworkRocketEntity(mc.level, 0.0D, 0.0D,
                                                0.0D, stack))
                                .build());
                InspectorStateRegistry.register(Items.END_CRYSTAL, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new EndCrystal(mc.level, 0.0D, 0.0D, 0.0D))
                                .build());
                InspectorStateRegistry.register(Items.LEAD, CustomViewStateConfig.builder("entity")
                                .entity((mc, stack) -> new LeashFenceKnotEntity(mc.level, BlockPos.ZERO))
                                .build());

                InspectorStateRegistry.register(Items.FILLED_MAP, CustomViewStateConfig.builder("map").build());
        }
}
