package io.github.hikoma0000.inspectability.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class BoundsCalculatingBufferSource implements MultiBufferSource {
    private final BoundsCalculatingVertexConsumer consumer;

    public BoundsCalculatingBufferSource(BoundsCalculatingVertexConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public VertexConsumer getBuffer(RenderType pRenderType) {
        return consumer;
    }
}
