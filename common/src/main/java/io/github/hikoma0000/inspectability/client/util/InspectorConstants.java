package io.github.hikoma0000.inspectability.client.util;

import org.joml.Vector3f;

public class InspectorConstants {
    public static float TRANSITION_SPEED = 0.08f;

    public static float ROTATION_SENSITIVITY = 0.01f;
    public static float MOVE_SENSITIVITY = 0.0025f;
    public static float ZOOM_SENSITIVITY = 0.05f;

    public static float MIN_SCALE = 0.1f;
    public static float MAX_SCALE = 20.0f;

    public static float DEFAULT_SCALE = 0.75f;
    public static float DEFAULT_OFFSET_X = 0.0f;
    public static float DEFAULT_OFFSET_Y = 0.0f;
    public static float DEFAULT_OFFSET_Z = 0.0f;

    public static long TOOLTIP_DISPLAY_TIME = 10000L;
    public static boolean HIDE_TOOLTIP = false;
    public static boolean DEBUG_MODE = false;

    public static float FOV = 70.0f;
    public static float Z_NEAR = 0.05f;
    public static float Z_FAR = 1000.0f;
    public static float CAMERA_BASE_Z = -1.0f;
    public static Vector3f LIGHT_0_DIR = new Vector3f(0.0f, 0.5f, 1.0f).normalize();
    public static Vector3f LIGHT_1_DIR = new Vector3f(0.0f, 1.0f, 0.0f);
    public static float INITIAL_ZOOM_SCALE = 0.001f;

    public static float STATE_TRANSITION_SPEED = 0.01f;
    public static float STATE_SLIDE_DISTANCE = 1.5f;
}
