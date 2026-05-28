package io.github.hikoma0000.inspectability.mixin.client;

import net.minecraft.world.item.BoatItem;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BoatItem.class)
public interface BoatItemAccessor {
    @Accessor("type")
    Boat.Type inspectability$getType();

    @Accessor("hasChest")
    boolean inspectability$hasChest();
}
