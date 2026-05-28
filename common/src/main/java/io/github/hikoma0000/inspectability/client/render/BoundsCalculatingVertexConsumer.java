package io.github.hikoma0000.inspectability.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;

public class BoundsCalculatingVertexConsumer implements VertexConsumer {
    public float minX = Float.MAX_VALUE;
    public float minY = Float.MAX_VALUE;
    public float minZ = Float.MAX_VALUE;
    public float maxX = -Float.MAX_VALUE;
    public float maxY = -Float.MAX_VALUE;
    public float maxZ = -Float.MAX_VALUE;

    private boolean hasVertices = false;

    @Override
    public VertexConsumer vertex(double pX, double pY, double pZ) {
        minX = (float) Math.min(minX, pX);
        minY = (float) Math.min(minY, pY);
        minZ = (float) Math.min(minZ, pZ);
        maxX = (float) Math.max(maxX, pX);
        maxY = (float) Math.max(maxY, pY);
        maxZ = (float) Math.max(maxZ, pZ);
        hasVertices = true;
        return this;
    }

    @Override
    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        return this;
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int pU, int pV) {
        return this;
    }

    @Override
    public VertexConsumer uv2(int pU, int pV) {
        return this;
    }

    @Override
    public VertexConsumer normal(float pX, float pY, float pZ) {
        return this;
    }

    @Override
    public void endVertex() {
    }

    @Override
    public void defaultColor(int pRed, int pGreen, int pBlue, int pAlpha) {
    }

    @Override
    public void unsetDefaultColor() {
    }

    public Vector3f getCenter() {
        if (!hasVertices) {
            return new Vector3f(0, 0, 0);
        }
        return new Vector3f((minX + maxX) / 2.0f, (minY + maxY) / 2.0f, (minZ + maxZ) / 2.0f);
    }

    public float getMaxSize() {
        if (!hasVertices) {
            return 1.0f;
        }
        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;
        return Math.max(dx, Math.max(dy, dz));
    }
}
