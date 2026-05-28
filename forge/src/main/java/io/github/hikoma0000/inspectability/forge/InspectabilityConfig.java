package io.github.hikoma0000.inspectability.forge;

import io.github.hikoma0000.inspectability.Inspectability;
import io.github.hikoma0000.inspectability.client.util.InspectorConstants;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Inspectability.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InspectabilityConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("debug");
        DEBUG_MODE = builder.define("debugMode", false);
        builder.pop();

        CLIENT_SPEC = builder.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        updateConstants();
    }

    public static void updateConstants() {
        InspectorConstants.DEBUG_MODE = DEBUG_MODE.get();
    }
}
