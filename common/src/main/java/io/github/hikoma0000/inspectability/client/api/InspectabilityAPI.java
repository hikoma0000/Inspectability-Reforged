package io.github.hikoma0000.inspectability.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import io.github.hikoma0000.inspectability.client.InspectorScreen;

import java.util.Collections;
import java.util.List;

/**
 * An API class that allows other mods to easily open the inspection screen
 * for specific entities, items, or custom view states.
 * 
 * NOTE: This is client-side only API.
 */
public final class InspectabilityAPI {

    private InspectabilityAPI() {
    }

    /**
     * Opens the item inspector screen for the specified ItemStack.
     * 
     * @param stack The item stack to inspect.
     */
    public static void inspect(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new InspectorScreen(stack, mc.screen));
    }

    /**
     * Opens the inspector screen for the specified Entity.
     * 
     * @param entity The entity to inspect.
     */
    public static void inspect(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        CustomViewStateConfig entityState = CustomViewStateConfig.builder("entity")
                .entity((m, s) -> entity)
                .build();
        mc.setScreen(new InspectorScreen(Collections.singletonList(entityState), mc.screen));
    }

    /**
     * Opens the inspector screen with a custom list of ViewStates without context.
     * 
     * @param states The list of view states to display.
     */
    public static void inspect(List<ViewState> states) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new InspectorScreen(states, mc.screen));
    }

    /**
     * Opens the inspector screen for the specified ItemStack, but forces it
     * to only use a custom list of ViewStates instead of the default ones.
     * 
     * @param stack The item stack context.
     * @param states The specific view states to display.
     */
    public static void inspect(ItemStack stack, List<ViewState> states) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new InspectorScreen(stack, states, mc.screen));
    }
}
