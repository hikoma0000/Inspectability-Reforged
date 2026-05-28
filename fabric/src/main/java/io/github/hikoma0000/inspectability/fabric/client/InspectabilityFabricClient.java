package io.github.hikoma0000.inspectability.fabric.client;

import io.github.hikoma0000.inspectability.InspectabilityClient;
import net.fabricmc.api.ClientModInitializer;

public final class InspectabilityFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        InspectabilityClient.init();
    }
}
