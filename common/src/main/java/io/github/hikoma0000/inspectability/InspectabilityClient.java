package io.github.hikoma0000.inspectability;

import io.github.hikoma0000.inspectability.client.InspectorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import io.github.hikoma0000.inspectability.client.init.InspectorState;

public final class InspectabilityClient {
    public static final String MOD_ID = "inspectability";
    public static KeyMapping inspectorKey;
    private static boolean initialized = false;

    public static void init() {
        if (initialized)
            return;
        initialized = true;

        inspectorKey = new KeyMapping(
                "key.inspectability.openiteminspector",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "key.categories.inspectability");

        KeyMappingRegistry.register(inspectorKey);

        InspectorState.init();

        ClientTickEvent.CLIENT_POST.register(InspectabilityClient::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        while (inspectorKey.consumeClick()) {
            LocalPlayer player = client.player;
            if (player != null) {
                if (client.screen != null) {
                    InspectorScreen inspector = new InspectorScreen((ItemStack) null, client.screen);
                    inspector.init(client, client.getWindow().getGuiScaledWidth(),
                            client.getWindow().getGuiScaledHeight());
                    client.screen = inspector;
                } else {
                    client.setScreen(new InspectorScreen((ItemStack) null, null));
                }
            }
        }
    }
}
