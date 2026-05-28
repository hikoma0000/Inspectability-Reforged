package io.github.hikoma0000.inspectability.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ViewStateRenderContext {
    private final Minecraft mc;
    private final float partialTick;
    private final long tickCount;
    
    private Quaternionf rotationOverride = null;
    private Float scaleOverride = null;
    private Vector3f offsetOverride = null;
    
    private Entity entityOverride = null;
    private ItemStack itemOverride = null;
    
    public ViewStateRenderContext(Minecraft mc, float partialTick, long tickCount) {
        this.mc = mc;
        this.partialTick = partialTick;
        this.tickCount = tickCount;
    }
    
    public Minecraft getMinecraft() {
        return mc;
    }
    
    public float getPartialTick() {
        return partialTick;
    }
    
    public long getTickCount() {
        return tickCount;
    }
    
    public void setRotation(Quaternionf rotation) {
        this.rotationOverride = rotation;
    }
    
    public Quaternionf getRotationOverride() {
        return rotationOverride;
    }
    
    public void setScale(float scale) {
        this.scaleOverride = scale;
    }
    
    public Float getScaleOverride() {
        return scaleOverride;
    }
    
    public void setOffset(Vector3f offset) {
        this.offsetOverride = offset;
    }
    
    public Vector3f getOffsetOverride() {
        return offsetOverride;
    }
    
    public void setEntity(Entity entity) {
        this.entityOverride = entity;
    }
    
    public Entity getEntityOverride() {
        return entityOverride;
    }
    
    public void setItem(ItemStack item) {
        this.itemOverride = item;
    }
    
    public ItemStack getItemOverride() {
        return itemOverride;
    }
}
