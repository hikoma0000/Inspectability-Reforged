package io.github.hikoma0000.inspectability.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hikoma0000.inspectability.client.api.CustomViewStateConfig;
import io.github.hikoma0000.inspectability.client.api.ViewState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.joml.Matrix3f;
public class InspectorDebug {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void renderDebugOverlay(GuiGraphics graphics, Font font, ItemStack stack, ViewState currentState) {
        if (stack == null || stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int y = screenHeight - 10;
        int x = 10;


        if (currentState instanceof CustomViewStateConfig customState) {
            String stateInfo = "State: " + customState.getId() + " (Scale: " + customState.getScaleMultiplier() + ", Offset: " + customState.getOffset().x() + ", " + customState.getOffset().y() + ", " + customState.getOffset().z() + ")";
            graphics.drawString(font, stateInfo, x, y, 0xFFFFFF, true);
            y -= 10;
        } else {
            String stateInfo = "State: " + currentState.getId();
            graphics.drawString(font, stateInfo, x, y, 0xFFFFFF, true);
            y -= 10;
        }


        CompoundTag tag = stack.getTag();
        if (tag != null && !tag.isEmpty()) {
            String nbtStr = "NBT: " + tag.toString();
            if (nbtStr.length() > 50) {
                nbtStr = nbtStr.substring(0, 47) + "...";
            }
            graphics.drawString(font, nbtStr, x, y, 0xFFFFFF, true);
            y -= 10;
        }


        String itemId = "ID: " + BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        graphics.drawString(font, itemId, x, y, 0xFFFFFF, true);
    }

    public static void renderDebugUILabels(GuiGraphics graphics, Font font, EditBox rotXBox, EditBox rotYBox, EditBox rotZBox, EditBox scaleBox, EditBox posXBox, EditBox posYBox, EditBox posZBox) {
        if (rotXBox != null) {
            graphics.drawString(font, "RotX", rotXBox.getX() - 30, rotXBox.getY() + 4, 0xFFFFFF, true);
            graphics.drawString(font, "RotY", rotYBox.getX() - 30, rotYBox.getY() + 4, 0xFFFFFF, true);
            graphics.drawString(font, "RotZ", rotZBox.getX() - 30, rotZBox.getY() + 4, 0xFFFFFF, true);
            graphics.drawString(font, "Scale", scaleBox.getX() - 30, scaleBox.getY() + 4, 0xFFFFFF, true);
            graphics.drawString(font, "PosX", posXBox.getX() - 30, posXBox.getY() + 4, 0xFFFFFF, true);
            graphics.drawString(font, "PosY", posYBox.getX() - 30, posYBox.getY() + 4, 0xFFFFFF, true);
            graphics.drawString(font, "PosZ", posZBox.getX() - 30, posZBox.getY() + 4, 0xFFFFFF, true);
        }
    }

    public static void renderPivot(PoseStack poseStack, MultiBufferSource buffers, Vector3f pivot) {
        if (pivot == null) return;
        

        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(RenderType.lines());
        }

        RenderType.lines().setupRenderState();
        RenderSystem.disableDepthTest();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();
        
        float length = 0.5f;
        float box = 0.025f;


        buffer.vertex(matrix, pivot.x() - box, pivot.y() - box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() - box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() + box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() + box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() - box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() - box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() + box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() + box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 1, 0, 0).endVertex();


        buffer.vertex(matrix, pivot.x() - box, pivot.y() - box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() + box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() - box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() + box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() - box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() + box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() - box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() + box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 0, 1, 0).endVertex();


        buffer.vertex(matrix, pivot.x() - box, pivot.y() - box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() - box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() - box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() - box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() + box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix, pivot.x() - box, pivot.y() + box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() + box, pivot.z() - box).color(255, 255, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix, pivot.x() + box, pivot.y() + box, pivot.z() + box).color(255, 255, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();


        buffer.vertex(matrix, pivot.x(), pivot.y(), pivot.z()).color(255, 0, 0, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix, pivot.x() + length, pivot.y(), pivot.z()).color(255, 0, 0, 255).normal(normalMatrix, 1, 0, 0).endVertex();


        buffer.vertex(matrix, pivot.x(), pivot.y(), pivot.z()).color(0, 255, 0, 255).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix, pivot.x(), pivot.y() + length, pivot.z()).color(0, 255, 0, 255).normal(normalMatrix, 0, 1, 0).endVertex();


        buffer.vertex(matrix, pivot.x(), pivot.y(), pivot.z()).color(0, 0, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix, pivot.x(), pivot.y(), pivot.z() + length).color(0, 0, 255, 255).normal(normalMatrix, 0, 0, 1).endVertex();

        tesselator.end();

        RenderSystem.enableDepthTest();
        RenderType.lines().clearRenderState();
    }

    public static boolean handleKeyboardInput(int keyCode, int scanCode, int modifiers) {
        if (!InspectorConstants.DEBUG_MODE) return false;

        Minecraft mc = Minecraft.getInstance();
        if (keyCode == GLFW.GLFW_KEY_C) {

            JsonObject root = new JsonObject();
            
            Quaternionf rot = HeldItemTransformManager.getTargetRotation();
            JsonArray rotArray = new JsonArray();
            rotArray.add(rot.x());
            rotArray.add(rot.y());
            rotArray.add(rot.z());
            rotArray.add(rot.w());
            root.add("rotation", rotArray);

            root.addProperty("scale", HeldItemTransformManager.getTargetScale());

            JsonArray offsetArray = new JsonArray();
            offsetArray.add(HeldItemTransformManager.getTargetOffsetX());
            offsetArray.add(HeldItemTransformManager.getTargetOffsetY());
            offsetArray.add(HeldItemTransformManager.getTargetOffsetZ());
            root.add("offset", offsetArray);

            String json = GSON.toJson(root);
            mc.keyboardHandler.setClipboard(json);
            
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("Copied transform to clipboard"), true);
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_V) {

            String json = mc.keyboardHandler.getClipboard();
            try {
                JsonObject root = GSON.fromJson(json, JsonObject.class);
                if (root != null) {
                    if (root.has("rotation")) {
                        JsonArray rotArray = root.getAsJsonArray("rotation");
                        HeldItemTransformManager.setTargetRotation(new Quaternionf(
                                rotArray.get(0).getAsFloat(),
                                rotArray.get(1).getAsFloat(),
                                rotArray.get(2).getAsFloat(),
                                rotArray.get(3).getAsFloat()
                        ));
                    }
                    if (root.has("scale")) {
                        HeldItemTransformManager.setTargetScale(root.get("scale").getAsFloat());
                    }
                    if (root.has("offset")) {
                        JsonArray offsetArray = root.getAsJsonArray("offset");
                        HeldItemTransformManager.setTargetOffsetX(offsetArray.get(0).getAsFloat());
                        HeldItemTransformManager.setTargetOffsetY(offsetArray.get(1).getAsFloat());
                        HeldItemTransformManager.setTargetOffsetZ(offsetArray.get(2).getAsFloat());
                    }
                    
                    if (mc.player != null) {
                        mc.player.displayClientMessage(Component.literal("Pasted transform from clipboard"), true);
                    }
                }
            } catch (Exception e) {
                if (mc.player != null) {
                    mc.player.displayClientMessage(Component.literal("Failed to parse transform JSON"), true);
                }
            }
            return true;
        }
        return false;
    }
}
