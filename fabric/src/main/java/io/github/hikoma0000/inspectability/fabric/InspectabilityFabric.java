package io.github.hikoma0000.inspectability.fabric;

import io.github.hikoma0000.inspectability.Inspectability;
import net.fabricmc.api.ModInitializer;

public final class InspectabilityFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Inspectability.init();
    }
}
