package io.github.hikoma0000.inspectability.client.util;

import org.joml.Quaternionf;

public class HeldItemTransformManager {
    private static boolean inspectorMode = false;

    private static float currentOffsetX = 0.0f;
    private static float currentOffsetY = 0.0f;
    private static float currentOffsetZ = 0.0f;
    private static float currentScale = 1.0f;
    private static final Quaternionf currentRotation = new Quaternionf();

    private static float targetOffsetX = 0.0f;
    private static float targetOffsetY = 0.0f;
    private static float targetOffsetZ = 0.0f;
    private static float targetScale = 1.0f;
    private static final Quaternionf targetRotation = new Quaternionf();

    public static boolean isInspectorMode() {
        return inspectorMode;
    }

    public static void setInspectorMode(boolean enabled) {
        inspectorMode = enabled;
    }

    public static void update(float partialTicks) {
        if (!inspectorMode) return;

        float alpha = easeInOut(InspectorConstants.TRANSITION_SPEED);

        currentOffsetX = lerp(alpha, currentOffsetX, targetOffsetX);
        currentOffsetY = lerp(alpha, currentOffsetY, targetOffsetY);
        currentOffsetZ = lerp(alpha, currentOffsetZ, targetOffsetZ);
        currentScale = lerp(alpha, currentScale, targetScale);

        currentRotation.slerp(targetRotation, alpha, currentRotation);
        currentRotation.normalize();
    }

    private static float easeInOut(float progress) {
        return (float) (-0.5 * (Math.cos(Math.PI * progress) - 1.0));
    }

    private static float lerp(float progress, float start, float end) {
        return start + progress * (end - start);
    }

    public static float getOffsetX() { return currentOffsetX; }
    public static float getOffsetY() { return currentOffsetY; }
    public static float getOffsetZ() { return currentOffsetZ; }
    public static float getScale() { return currentScale; }
    public static Quaternionf getRotation() { return currentRotation; }

    public static void setTargetOffsetX(float x) {
        targetOffsetX = x;
    }

    public static void setTargetOffsetY(float y) {
        targetOffsetY = y;
    }

    public static void setTargetOffsetZ(float z) {
        targetOffsetZ = z;
    }

    public static void setTargetScale(float s) {
        targetScale = s;
    }

    public static void setTargetRotation(Quaternionf q) {
        targetRotation.set(q).normalize();
    }

    public static void setCurrentScale(float s) { 
        currentScale = s; 
    }

    public static void setCurrentRotation(Quaternionf q) {
        currentRotation.set(q).normalize();
    }

    public static float getTargetOffsetX() { return targetOffsetX; }
    public static float getTargetOffsetY() { return targetOffsetY; }
    public static float getTargetOffsetZ() { return targetOffsetZ; }
    public static float getTargetScale() { return targetScale; }
    public static Quaternionf getTargetRotation() { return targetRotation; }

    public static void applyImmediate() {
        currentOffsetX = targetOffsetX;
        currentOffsetY = targetOffsetY;
        currentOffsetZ = targetOffsetZ;
        currentScale = targetScale;
        currentRotation.set(targetRotation);
    }
}
