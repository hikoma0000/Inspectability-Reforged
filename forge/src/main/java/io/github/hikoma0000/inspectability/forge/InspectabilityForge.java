package io.github.hikoma0000.inspectability.forge;

import io.github.hikoma0000.inspectability.Inspectability;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Inspectability.MOD_ID)
public final class InspectabilityForge {
    public InspectabilityForge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, InspectabilityConfig.CLIENT_SPEC);
        Inspectability.init();
    }
}