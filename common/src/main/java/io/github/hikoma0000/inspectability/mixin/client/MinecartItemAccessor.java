package io.github.hikoma0000.inspectability.mixin.client;

import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartItem.class)
public interface MinecartItemAccessor {
    @Accessor("type")
    AbstractMinecart.Type inspectability$getType();
}
