package io.github.hikoma0000.inspectability.client;

import io.github.hikoma0000.inspectability.Inspectability;
import io.github.hikoma0000.inspectability.InspectabilityClient;
import io.github.hikoma0000.inspectability.client.util.HeldItemTransformManager;
import io.github.hikoma0000.inspectability.client.util.InspectorConstants;
import io.github.hikoma0000.inspectability.client.util.InspectorDebug;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.Locale;
import net.minecraft.world.item.BlockItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.core.BlockPos;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.decoration.ItemFrame;
import io.github.hikoma0000.inspectability.client.api.InspectorStateRegistry;
import io.github.hikoma0000.inspectability.client.api.CustomViewStateConfig;
import io.github.hikoma0000.inspectability.client.api.ViewState;
import io.github.hikoma0000.inspectability.client.api.StandardViewState;
import io.github.hikoma0000.inspectability.client.render.BoundsCalculatingVertexConsumer;
import io.github.hikoma0000.inspectability.client.render.BoundsCalculatingBufferSource;
import org.joml.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import net.minecraft.client.gui.components.EditBox;
import io.github.hikoma0000.inspectability.client.api.ViewStateRenderContext;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class InspectorScreen extends Screen {
    private double lastMouseX;
    private double lastMouseY;
    private final Map<String, Vector3f> pivotCache = new HashMap<>();
    private final Map<String, Float> sizeCache = new HashMap<>();

    private long screenOpenedTime;
    private final ItemStack targetStack;
    private final List<ViewState> providedStates;

    private final Screen previousScreen;

    private BlockEntity dummyBlockEntity;
    private final Map<ViewState, Entity[]> stateEntities = new HashMap<>();
    private final Map<ViewState, ItemStack[]> stateItems = new HashMap<>();
    private final Map<ViewState, Integer> stateVariantIndices = new HashMap<>();

    private final List<ViewState> availableStates = new ArrayList<>();
    private int currentStateIndex = 0;
    private int previousStateIndex = 0;
    private float horizontalSlideProgress = 1.0f;
    private int horizontalSlideDirection = -1;
    private float verticalSlideProgress = 1.0f;
    private int verticalSlideDirection = -1;
    private int previousVariantIndex = 0;
    private final Quaternionf previousRotation = new Quaternionf().identity();

    private EditBox rotXBox, rotYBox, rotZBox, scaleBox, posXBox, posYBox, posZBox;

    private boolean initialized = false;

    public InspectorScreen() {
        this((ItemStack) null, null);
    }

    public InspectorScreen(ItemStack targetStack, Screen previousScreen) {
        this(targetStack, null, previousScreen);
    }

    public InspectorScreen(List<ViewState> providedStates, Screen previousScreen) {
        this(null, providedStates, previousScreen);
    }

    public InspectorScreen(ItemStack targetStack, List<ViewState> providedStates, Screen previousScreen) {
        super(Component.literal("Inspector"));
        this.targetStack = targetStack;
        this.providedStates = providedStates;
        this.previousScreen = previousScreen;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        HeldItemTransformManager.update(delta);

        renderInspectorItem(delta);

        RenderSystem.clear(256, Minecraft.ON_OSX);

        long elapsed = System.currentTimeMillis() - screenOpenedTime;
        if (!InspectorConstants.HIDE_TOOLTIP) {
            RenderSystem.disableDepthTest();
            graphics.pose().pushPose();
            graphics.pose().translate(0.0f, 0.0f, 500.0f);
            graphics.pose().scale(1.0f, 1.0f, 1.0f);
            graphics.drawString(this.font, Component.translatable("tooltip." + Inspectability.MOD_ID + ".move"), 10, 10,
                    -1, true);
            graphics.drawString(this.font, Component.translatable("tooltip." + Inspectability.MOD_ID + ".reposition"),
                    10, 20, -1,
                    true);
            graphics.drawString(this.font, Component.translatable("tooltip." + Inspectability.MOD_ID + ".rotate"), 10,
                    30, -1, true);
            graphics.drawString(this.font, Component.translatable("tooltip." + Inspectability.MOD_ID + ".zoom"), 10, 40,
                    -1, true);
            graphics.drawString(this.font, Component.translatable("tooltip." + Inspectability.MOD_ID + ".roll"), 10, 50,
                    -1, true);

            if (availableStates.size() > 1) {
                int yOffset = 70;
                if (currentStateIndex > 0) {
                    ViewState prevState = availableStates.get(currentStateIndex - 1);
                    Component stateName = Component.translatable(prevState.getTranslationKey());
                    Component keyName = Minecraft.getInstance().options.keyLeft.getTranslatedKeyMessage();
                    graphics.drawString(this.font, Component
                            .translatable("tooltip." + Inspectability.MOD_ID + ".view_state", stateName, keyName), 10,
                            yOffset, -1, true);
                    yOffset += 10;
                }
                if (currentStateIndex < availableStates.size() - 1) {
                    ViewState nextState = availableStates.get(currentStateIndex + 1);
                    Component stateName = Component.translatable(nextState.getTranslationKey());
                    Component keyName = Minecraft.getInstance().options.keyRight.getTranslatedKeyMessage();
                    graphics.drawString(this.font, Component
                            .translatable("tooltip." + Inspectability.MOD_ID + ".view_state", stateName, keyName), 10,
                            yOffset, -1, true);
                }

                ViewState state = availableStates.get(currentStateIndex);
                if (state.getVariantCount(Minecraft.getInstance(),
                        targetStack != null ? targetStack
                                : (Minecraft.getInstance().player != null
                                        ? Minecraft.getInstance().player.getMainHandItem()
                                        : ItemStack.EMPTY)) > 1) {
                    yOffset += 10;
                    Component keyUp = Minecraft.getInstance().options.keyUp.getTranslatedKeyMessage();
                    Component keyDown = Minecraft.getInstance().options.keyDown.getTranslatedKeyMessage();
                    graphics.drawString(this.font, Component
                            .translatable("tooltip." + Inspectability.MOD_ID + ".change_variant", keyUp, keyDown), 10,
                            yOffset, -1, true);
                }
            }

            graphics.flush();
            graphics.pose().popPose();
            RenderSystem.enableDepthTest();
        }

        if (InspectorConstants.DEBUG_MODE && !availableStates.isEmpty()) {
            ItemStack stack = targetStack != null ? targetStack
                    : (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getMainHandItem()
                            : ItemStack.EMPTY);
            InspectorDebug.renderDebugOverlay(graphics, this.font, stack, availableStates.get(currentStateIndex));

            InspectorDebug.renderDebugUILabels(graphics, this.font, rotXBox, rotYBox, rotZBox, scaleBox, posXBox,
                    posYBox, posZBox);
        }

        super.render(graphics, mouseX, mouseY, delta);

        handleMouseInput(mouseX, mouseY);
    }

    private void renderInspectorItem(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ItemStack stack = targetStack != null ? targetStack
                : (mc.player != null ? mc.player.getMainHandItem() : ItemStack.EMPTY);

        if ((!stack.isEmpty() || providedStates != null) && !availableStates.isEmpty()) {
            BufferSource buffers = mc.renderBuffers().bufferSource();

            Matrix4f lastProj = new Matrix4f(RenderSystem.getProjectionMatrix());
            float aspect = (float) mc.getWindow().getWidth() / (float) mc.getWindow().getHeight();
            Matrix4f perspective = new Matrix4f().perspective((float) Math.toRadians(InspectorConstants.FOV), aspect,
                    InspectorConstants.Z_NEAR, InspectorConstants.Z_FAR);
            RenderSystem.setProjectionMatrix(perspective, RenderSystem.getVertexSorting());

            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.applyModelViewMatrix();

            Lighting.setupFor3DItems();
            RenderSystem.setShaderLights(InspectorConstants.LIGHT_0_DIR, InspectorConstants.LIGHT_1_DIR);

            ViewState currentState = availableStates.get(currentStateIndex);
            int currentVariant = stateVariantIndices.getOrDefault(currentState, 0);

            if (currentState instanceof CustomViewStateConfig customState && customState.getTicker() != null) {
                ViewStateRenderContext ctx = new ViewStateRenderContext(mc, partialTicks, System.currentTimeMillis());
                customState.getTicker().accept(ctx);
                if (ctx.getRotationOverride() != null)
                    HeldItemTransformManager.setTargetRotation(ctx.getRotationOverride());
                if (ctx.getScaleOverride() != null)
                    HeldItemTransformManager.setTargetScale(ctx.getScaleOverride());
                if (ctx.getOffsetOverride() != null) {
                    HeldItemTransformManager.setTargetOffsetX(ctx.getOffsetOverride().x());
                    HeldItemTransformManager.setTargetOffsetY(ctx.getOffsetOverride().y());
                    HeldItemTransformManager.setTargetOffsetZ(ctx.getOffsetOverride().z());
                }
                if (ctx.getEntityOverride() != null && stateEntities.containsKey(currentState)) {
                    stateEntities.get(currentState)[currentVariant] = ctx.getEntityOverride();
                }
                if (ctx.getItemOverride() != null && stateItems.containsKey(currentState)) {
                    stateItems.get(currentState)[currentVariant] = ctx.getItemOverride();
                }
            }

            if (horizontalSlideProgress < 1.0f) {
                horizontalSlideProgress += InspectorConstants.STATE_TRANSITION_SPEED;
                if (horizontalSlideProgress > 1.0f)
                    horizontalSlideProgress = 1.0f;

                float easeOut = 1.0f - (float) Math.pow(1.0f - horizontalSlideProgress, 3);
                float currentScale = HeldItemTransformManager.getScale();
                float dynamicSlideDistance = InspectorConstants.STATE_SLIDE_DISTANCE + (currentScale * 1.5f);

                float previousOffset = easeOut * dynamicSlideDistance * horizontalSlideDirection;
                float currentOffset = -(1.0f - easeOut) * dynamicSlideDistance * horizontalSlideDirection;

                float previousScaleMult = Math.max(0.01f, 1.0f - easeOut);
                float currentScaleMult = Math.max(0.01f, easeOut);

                int prevVariant = stateVariantIndices.getOrDefault(availableStates.get(previousStateIndex), 0);
                renderSingleState(mc, stack, availableStates.get(previousStateIndex), prevVariant, buffers,
                        previousOffset, 0.0f,
                        previousScaleMult, this.previousRotation, false,
                        getPivot(availableStates.get(previousStateIndex), prevVariant, mc, stack));
                renderSingleState(mc, stack, currentState, currentVariant, buffers, currentOffset, 0.0f,
                        currentScaleMult, HeldItemTransformManager.getRotation(), false,
                        getPivot(currentState, currentVariant, mc, stack));
            } else if (verticalSlideProgress < 1.0f) {
                verticalSlideProgress += InspectorConstants.STATE_TRANSITION_SPEED;
                if (verticalSlideProgress > 1.0f)
                    verticalSlideProgress = 1.0f;

                float easeOut = 1.0f - (float) Math.pow(1.0f - verticalSlideProgress, 3);
                float currentScale = HeldItemTransformManager.getScale();
                float dynamicSlideDistance = InspectorConstants.STATE_SLIDE_DISTANCE + (currentScale * 1.5f);

                float previousOffset = easeOut * dynamicSlideDistance * verticalSlideDirection;
                float currentOffset = -(1.0f - easeOut) * dynamicSlideDistance * verticalSlideDirection;

                float previousScaleMult = Math.max(0.01f, 1.0f - easeOut);
                float currentScaleMult = Math.max(0.01f, easeOut);

                renderSingleState(mc, stack, currentState, previousVariantIndex, buffers, 0.0f, previousOffset,
                        previousScaleMult, HeldItemTransformManager.getRotation(), false,
                        getPivot(currentState, previousVariantIndex, mc, stack));
                renderSingleState(mc, stack, currentState, currentVariant, buffers, 0.0f, currentOffset,
                        currentScaleMult, HeldItemTransformManager.getRotation(), false,
                        getPivot(currentState, currentVariant, mc, stack));
            } else {
                renderSingleState(mc, stack, currentState, currentVariant, buffers, 0.0f, 0.0f, 1.0f,
                        HeldItemTransformManager.getRotation(), false,
                        getPivot(currentState, currentVariant, mc, stack));
            }

            buffers.endBatch();

            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(lastProj, RenderSystem.getVertexSorting());
        }
    }

    private void renderSingleState(Minecraft mc, ItemStack stack, ViewState state, int variantIndex,
            MultiBufferSource buffers, float xOffset, float yOffset, float scaleMultiplier, Quaternionf rotation,
            boolean isDummy, Vector3f pivot) {
        PoseStack itemPoseStack = new PoseStack();
        MultiBufferSource wrappedBuffers = isDummy ? buffers : new FullbrightBufferSource(buffers);

        if (!isDummy) {
            itemPoseStack.translate(0.0f, 0.0f, InspectorConstants.CAMERA_BASE_Z);
            itemPoseStack.translate(HeldItemTransformManager.getOffsetX() + xOffset,
                    HeldItemTransformManager.getOffsetY() + yOffset,
                    HeldItemTransformManager.getOffsetZ());
        }

        float autoScale = isDummy ? 1.0f : (1.0f / Math.max(0.1f, getSize(state, variantIndex, mc, stack)));
        float scale = isDummy ? 1.0f : (HeldItemTransformManager.getScale() * scaleMultiplier * autoScale);
        itemPoseStack.scale(scale, scale, scale);

        if (!isDummy && pivot != null) {
            itemPoseStack.mulPose(rotation);
            itemPoseStack.translate(-pivot.x(), -pivot.y(), -pivot.z());
        } else if (!isDummy) {
            itemPoseStack.mulPose(rotation);
        }

        if (state.getId().equals("map") && stack.getItem() instanceof MapItem) {
            Integer mapId = MapItem.getMapId(stack);
            if (mapId != null) {
                MapItemSavedData mapData = MapItem.getSavedData(mapId, mc.level);
                if (mapData != null) {
                    itemPoseStack.pushPose();
                    itemPoseStack.mulPose(new Quaternionf().rotateY((float) Math.PI).rotateZ((float) Math.PI));
                    itemPoseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
                    itemPoseStack.translate(-64.0F, -64.0F, 0.0F);

                    RenderType mapBackground = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
                    VertexConsumer vertexConsumer = wrappedBuffers.getBuffer(mapBackground);
                    Matrix4f matrix4f = itemPoseStack.last().pose();
                    vertexConsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F)
                            .uv2(15728880).endVertex();
                    vertexConsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F)
                            .uv2(15728880).endVertex();
                    vertexConsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F)
                            .uv2(15728880).endVertex();
                    vertexConsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F)
                            .uv2(15728880).endVertex();

                    vertexConsumer.vertex(matrix4f, -7.0F, -7.0F, 0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F)
                            .uv2(15728880).endVertex();
                    vertexConsumer.vertex(matrix4f, 135.0F, -7.0F, 0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F)
                            .uv2(15728880).endVertex();
                    vertexConsumer.vertex(matrix4f, 135.0F, 135.0F, 0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F)
                            .uv2(15728880).endVertex();
                    vertexConsumer.vertex(matrix4f, -7.0F, 135.0F, 0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F)
                            .uv2(15728880).endVertex();

                    mc.gameRenderer.getMapRenderer().render(itemPoseStack, wrappedBuffers, mapId, mapData, true,
                            15728880);
                    itemPoseStack.popPose();
                }
            }
        } else if (state == StandardViewState.BLOCK && stack.getItem() instanceof BlockItem blockItem) {
            boolean isCustomRenderer = mc.getItemRenderer().getModel(stack, null, null, 0).isCustomRenderer();

            if (isCustomRenderer) {
                mc.getItemRenderer().renderStatic(
                        stack,
                        ItemDisplayContext.NONE,
                        15728880,
                        OverlayTexture.NO_OVERLAY,
                        itemPoseStack,
                        wrappedBuffers,
                        mc.player != null ? mc.player.level() : null,
                        0);
            } else {
                itemPoseStack.pushPose();
                itemPoseStack.translate(-0.5f, -0.5f, -0.5f);
                BlockState blockState = blockItem.getBlock().defaultBlockState();
                mc.getBlockRenderer().renderSingleBlock(
                        blockState,
                        itemPoseStack,
                        wrappedBuffers,
                        15728880,
                        OverlayTexture.NO_OVERLAY);
                if (this.dummyBlockEntity != null) {
                    BlockEntityRenderer<BlockEntity> renderer = mc
                            .getBlockEntityRenderDispatcher().getRenderer(this.dummyBlockEntity);
                    if (renderer != null) {
                        renderer.render(this.dummyBlockEntity, mc.getFrameTime(), itemPoseStack, wrappedBuffers,
                                15728880,
                                OverlayTexture.NO_OVERLAY);
                    }
                }
                itemPoseStack.popPose();
            }
        } else if (state instanceof CustomViewStateConfig customState && stateEntities.containsKey(state)
                && stateEntities.get(state)[variantIndex] != null) {
            Entity dummy = stateEntities.get(state)[variantIndex];
            itemPoseStack.pushPose();
            float entityScale = customState.getScaleMultiplier() * customState.getVariantScaleMultiplier(variantIndex);
            itemPoseStack.scale(entityScale, entityScale, entityScale);
            if (dummy instanceof ItemFrame) {
                itemPoseStack.translate(0.0f, 0.0f, -0.5f);
            }
            mc.getEntityRenderDispatcher().render(
                    dummy,
                    0.0D, 0.0D, 0.0D,
                    0.0F, mc.getFrameTime(),
                    itemPoseStack, wrappedBuffers, 15728880);
            itemPoseStack.popPose();
        } else if (state instanceof CustomViewStateConfig customState && stateItems.containsKey(state)
                && stateItems.get(state)[variantIndex] != null) {
            ItemStack customStack = stateItems.get(state)[variantIndex];
            itemPoseStack.pushPose();
            float itemScale = customState.getScaleMultiplier() * customState.getVariantScaleMultiplier(variantIndex);
            itemPoseStack.scale(itemScale, itemScale, itemScale);
            mc.getItemRenderer().renderStatic(
                    customStack,
                    customState.getContext() != ItemDisplayContext.NONE ? customState.getContext()
                            : ItemDisplayContext.GUI,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    itemPoseStack,
                    wrappedBuffers,
                    mc.player != null ? mc.player.level() : null,
                    0);
            itemPoseStack.popPose();
        } else {
            mc.getItemRenderer().renderStatic(
                    stack,
                    state.getContext(),
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    itemPoseStack,
                    wrappedBuffers,
                    mc.player != null ? mc.player.level() : null,
                    0);
        }

        if (!isDummy && InspectorConstants.DEBUG_MODE) {
            InspectorDebug.renderPivot(itemPoseStack, wrappedBuffers, pivot);
        }
    }

    @Override
    protected void init() {
        if (!initialized) {
            HeldItemTransformManager.setInspectorMode(true);

            lastMouseX = Minecraft.getInstance().mouseHandler.xpos();
            lastMouseY = Minecraft.getInstance().mouseHandler.ypos();

            screenOpenedTime = System.currentTimeMillis();

            Minecraft mc = Minecraft.getInstance();
            ItemStack stack = targetStack != null ? targetStack
                    : (mc.player != null ? mc.player.getMainHandItem() : ItemStack.EMPTY);
            availableStates.clear();
            pivotCache.clear();
            sizeCache.clear();
            stateEntities.clear();
            stateItems.clear();
            this.dummyBlockEntity = null;

            if (providedStates != null) {
                availableStates.addAll(providedStates);
            } else {
                availableStates.addAll(InspectorStateRegistry.getStates(mc, stack));
            }
            stateVariantIndices.clear();

            if (availableStates.isEmpty()) {

                Minecraft.getInstance().tell(this::onClose);
                return;
            }

            for (ViewState state : availableStates) {
                stateVariantIndices.put(state, 0);
                int variantCount = state.getVariantCount(mc, stack);

                if (state == StandardViewState.BLOCK || (state instanceof CustomViewStateConfig customState
                        && customState.getId().equals("block"))) {
                    Item item = stack.getItem();
                    if (item instanceof BlockItem blockItem
                            && blockItem.getBlock() instanceof EntityBlock entityBlock) {
                        this.dummyBlockEntity = entityBlock.newBlockEntity(BlockPos.ZERO,
                                blockItem.getBlock().defaultBlockState());
                        if (this.dummyBlockEntity != null && mc.level != null) {
                            this.dummyBlockEntity.setLevel(mc.level);
                        }
                    }
                }
                if (state instanceof CustomViewStateConfig customConfig) {
                    Entity[] entities = new Entity[variantCount];
                    ItemStack[] items = new ItemStack[variantCount];
                    for (int i = 0; i < variantCount; i++) {
                        Entity dummy = customConfig.createEntity(mc, stack, i);
                        if (dummy != null)
                            entities[i] = dummy;

                        ItemStack customItem = customConfig.createItem(mc, stack, i);
                        if (customItem != null)
                            items[i] = customItem;
                    }
                    stateEntities.put(state, entities);
                    stateItems.put(state, items);
                }
            }
            currentStateIndex = 0;
            previousStateIndex = 0;
            horizontalSlideProgress = 1.0f;
            verticalSlideProgress = 1.0f;

            ViewState initialState = availableStates.get(currentStateIndex);
            Vector3f initialOffset = getInitialOffsetForState(initialState);
            HeldItemTransformManager.setTargetOffsetX(initialOffset.x());
            HeldItemTransformManager.setTargetOffsetY(initialOffset.y());
            HeldItemTransformManager.setTargetOffsetZ(initialOffset.z());
            HeldItemTransformManager.setTargetScale(InspectorConstants.DEFAULT_SCALE);
            HeldItemTransformManager.setTargetRotation(getInitialRotationForState(initialState));

            HeldItemTransformManager.applyImmediate();
            HeldItemTransformManager.setCurrentScale(InspectorConstants.INITIAL_ZOOM_SCALE);

            initialized = true;
        }

        if (InspectorConstants.DEBUG_MODE) {
            setupDebugUI();
        }
    }

    private void setupDebugUI() {
        int boxWidth = 50;
        int boxHeight = 16;
        int x = this.width - boxWidth - 10;
        int startY = this.height - 150;

        rotXBox = new EditBox(this.font, x, startY, boxWidth, boxHeight, Component.literal("RotX"));
        rotYBox = new EditBox(this.font, x, startY + 20, boxWidth, boxHeight, Component.literal("RotY"));
        rotZBox = new EditBox(this.font, x, startY + 40, boxWidth, boxHeight, Component.literal("RotZ"));
        scaleBox = new EditBox(this.font, x, startY + 60, boxWidth, boxHeight, Component.literal("Scale"));
        posXBox = new EditBox(this.font, x, startY + 80, boxWidth, boxHeight, Component.literal("PosX"));
        posYBox = new EditBox(this.font, x, startY + 100, boxWidth, boxHeight, Component.literal("PosY"));
        posZBox = new EditBox(this.font, x, startY + 120, boxWidth, boxHeight, Component.literal("PosZ"));

        Consumer<String> responder = (val) -> applyDebugUIVals();
        rotXBox.setResponder(responder);
        rotYBox.setResponder(responder);
        rotZBox.setResponder(responder);
        scaleBox.setResponder(responder);
        posXBox.setResponder(responder);
        posYBox.setResponder(responder);
        posZBox.setResponder(responder);

        addRenderableWidget(rotXBox);
        addRenderableWidget(rotYBox);
        addRenderableWidget(rotZBox);
        addRenderableWidget(scaleBox);
        addRenderableWidget(posXBox);
        addRenderableWidget(posYBox);
        addRenderableWidget(posZBox);
    }

    private boolean isUpdatingDebugUI = false;

    private void updateDebugUI() {
        if (!InspectorConstants.DEBUG_MODE || isUpdatingDebugUI || rotXBox == null)
            return;
        isUpdatingDebugUI = true;

        if (!rotXBox.isFocused())
            rotXBox.setValue(String.format(Locale.US, "%.1f",
                    Math.toDegrees(HeldItemTransformManager.getTargetRotation().getEulerAnglesXYZ(new Vector3f()).x)));
        if (!rotYBox.isFocused())
            rotYBox.setValue(String.format(Locale.US, "%.1f",
                    Math.toDegrees(HeldItemTransformManager.getTargetRotation().getEulerAnglesXYZ(new Vector3f()).y)));
        if (!rotZBox.isFocused())
            rotZBox.setValue(String.format(Locale.US, "%.1f",
                    Math.toDegrees(HeldItemTransformManager.getTargetRotation().getEulerAnglesXYZ(new Vector3f()).z)));

        if (!scaleBox.isFocused())
            scaleBox.setValue(String.format(Locale.US, "%.2f", HeldItemTransformManager.getTargetScale()));

        if (!posXBox.isFocused())
            posXBox.setValue(String.format(Locale.US, "%.2f", HeldItemTransformManager.getTargetOffsetX()));
        if (!posYBox.isFocused())
            posYBox.setValue(String.format(Locale.US, "%.2f", HeldItemTransformManager.getTargetOffsetY()));
        if (!posZBox.isFocused())
            posZBox.setValue(String.format(Locale.US, "%.2f", HeldItemTransformManager.getTargetOffsetZ()));

        isUpdatingDebugUI = false;
    }

    private void applyDebugUIVals() {
        if (isUpdatingDebugUI)
            return;
        try {
            float rx = (float) Math.toRadians(Float.parseFloat(rotXBox.getValue()));
            float ry = (float) Math.toRadians(Float.parseFloat(rotYBox.getValue()));
            float rz = (float) Math.toRadians(Float.parseFloat(rotZBox.getValue()));
            HeldItemTransformManager.setTargetRotation(new Quaternionf().rotationXYZ(rx, ry, rz));

            HeldItemTransformManager.setTargetScale(Float.parseFloat(scaleBox.getValue()));

            HeldItemTransformManager.setTargetOffsetX(Float.parseFloat(posXBox.getValue()));
            HeldItemTransformManager.setTargetOffsetY(Float.parseFloat(posYBox.getValue()));
            HeldItemTransformManager.setTargetOffsetZ(Float.parseFloat(posZBox.getValue()));
        } catch (NumberFormatException ignored) {
        }
    }

    private Vector3f getPivot(ViewState state, int variantIndex, Minecraft mc, ItemStack stack) {
        String cacheKey = state.getId() + "_" + variantIndex;
        if (!pivotCache.containsKey(cacheKey)) {
            BoundsCalculatingVertexConsumer consumer = new BoundsCalculatingVertexConsumer();
            BoundsCalculatingBufferSource dummyBuffers = new BoundsCalculatingBufferSource(consumer);

            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.applyModelViewMatrix();

            renderSingleState(mc, stack, state, variantIndex, dummyBuffers, 0.0f, 0.0f, 1.0f,
                    new Quaternionf().identity(), true,
                    null);

            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();

            pivotCache.put(cacheKey, consumer.getCenter());
            sizeCache.put(cacheKey, consumer.getMaxSize());
        }
        return pivotCache.get(cacheKey);
    }

    private float getSize(ViewState state, int variantIndex, Minecraft mc, ItemStack stack) {
        getPivot(state, variantIndex, mc, stack);
        String cacheKey = state.getId() + "_" + variantIndex;
        return sizeCache.getOrDefault(cacheKey, 1.0f);
    }

    private Quaternionf getInitialRotationForState(ViewState state) {
        if (state instanceof CustomViewStateConfig custom) {
            return new Quaternionf(custom.getInitialRotation());
        }
        if (state == StandardViewState.BLOCK) {
            return new Quaternionf().rotateX((float) Math.toRadians(30)).rotateY((float) Math.toRadians(45));
        }
        return new Quaternionf().identity();
    }

    private Vector3f getInitialOffsetForState(ViewState state) {
        if (state instanceof CustomViewStateConfig custom) {
            return custom.getOffset();
        }
        return new Vector3f(InspectorConstants.DEFAULT_OFFSET_X, InspectorConstants.DEFAULT_OFFSET_Y,
                InspectorConstants.DEFAULT_OFFSET_Z);
    }

    @Override
    public void removed() {
        HeldItemTransformManager.setInspectorMode(false);
    }

    private void handleMouseInput(int mouseX, int mouseY) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean leftPressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightPressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean ctrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

        double dx = (mouseX - lastMouseX) * InspectorConstants.ROTATION_SENSITIVITY;
        double dy = (mouseY - lastMouseY) * InspectorConstants.ROTATION_SENSITIVITY;

        if (leftPressed || rightPressed) {
            Quaternionf targetRot = new Quaternionf(HeldItemTransformManager.getTargetRotation());
            if (leftPressed) {
                if (ctrlPressed) {
                    targetRot.premul(new Quaternionf().rotateAxis((float) -dx, 0, 0, 1));
                } else {
                    targetRot.premul(new Quaternionf().rotateAxis((float) dx, 0, 1, 0));
                    targetRot.premul(new Quaternionf().rotateAxis((float) dy, 1, 0, 0));
                }
                HeldItemTransformManager.setTargetRotation(targetRot);
            }

            if (rightPressed) {
                HeldItemTransformManager.setTargetOffsetX(HeldItemTransformManager.getTargetOffsetX()
                        + (float) (mouseX - lastMouseX) * InspectorConstants.MOVE_SENSITIVITY);
                HeldItemTransformManager.setTargetOffsetY(HeldItemTransformManager.getTargetOffsetY()
                        - (float) (mouseY - lastMouseY) * InspectorConstants.MOVE_SENSITIVITY);
            }
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        float newScale = HeldItemTransformManager.getTargetScale()
                + (verticalAmount > 0 ? InspectorConstants.ZOOM_SENSITIVITY : -InspectorConstants.ZOOM_SENSITIVITY);
        HeldItemTransformManager
                .setTargetScale(Mth.clamp(newScale, InspectorConstants.MIN_SCALE, InspectorConstants.MAX_SCALE));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            ViewState state = availableStates.isEmpty() ? null : availableStates.get(currentStateIndex);
            if (state != null) {
                Vector3f initialOffset = getInitialOffsetForState(state);
                HeldItemTransformManager.setTargetOffsetX(initialOffset.x());
                HeldItemTransformManager.setTargetOffsetY(initialOffset.y());
                HeldItemTransformManager.setTargetOffsetZ(initialOffset.z());
                HeldItemTransformManager.setTargetRotation(getInitialRotationForState(state));
            } else {
                HeldItemTransformManager.setTargetOffsetX(InspectorConstants.DEFAULT_OFFSET_X);
                HeldItemTransformManager.setTargetOffsetY(InspectorConstants.DEFAULT_OFFSET_Y);
                HeldItemTransformManager.setTargetOffsetZ(InspectorConstants.DEFAULT_OFFSET_Z);
                HeldItemTransformManager.setTargetRotation(new Quaternionf().identity());
            }
            HeldItemTransformManager.setTargetScale(InspectorConstants.DEFAULT_SCALE);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (this.previousScreen != null) {
            this.removed();
            this.minecraft.screen = this.previousScreen;
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (InspectorDebug.handleKeyboardInput(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE || InspectabilityClient.inspectorKey.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyRight.matches(keyCode, scanCode)) {
            if (availableStates.size() > 1 && horizontalSlideProgress >= 1.0f && verticalSlideProgress >= 1.0f
                    && currentStateIndex < availableStates.size() - 1) {
                this.previousRotation.set(HeldItemTransformManager.getRotation());
                previousStateIndex = currentStateIndex;
                currentStateIndex++;
                horizontalSlideProgress = 0.0f;
                horizontalSlideDirection = -1;
                ViewState newState = availableStates.get(currentStateIndex);
                Vector3f initialOffset = getInitialOffsetForState(newState);
                HeldItemTransformManager.setTargetOffsetX(initialOffset.x());
                HeldItemTransformManager.setTargetOffsetY(initialOffset.y());
                HeldItemTransformManager.setTargetOffsetZ(initialOffset.z());
                Quaternionf newRot = getInitialRotationForState(newState);
                HeldItemTransformManager.setTargetRotation(newRot);
                HeldItemTransformManager.setCurrentRotation(newRot);
            }
            return true;
        }
        if (mc.options.keyLeft.matches(keyCode, scanCode)) {
            if (availableStates.size() > 1 && horizontalSlideProgress >= 1.0f && verticalSlideProgress >= 1.0f
                    && currentStateIndex > 0) {
                this.previousRotation.set(HeldItemTransformManager.getRotation());
                previousStateIndex = currentStateIndex;
                currentStateIndex--;
                horizontalSlideProgress = 0.0f;
                horizontalSlideDirection = 1;
                ViewState newState = availableStates.get(currentStateIndex);
                Vector3f initialOffset = getInitialOffsetForState(newState);
                HeldItemTransformManager.setTargetOffsetX(initialOffset.x());
                HeldItemTransformManager.setTargetOffsetY(initialOffset.y());
                HeldItemTransformManager.setTargetOffsetZ(initialOffset.z());
                Quaternionf newRot = getInitialRotationForState(newState);
                HeldItemTransformManager.setTargetRotation(newRot);
                HeldItemTransformManager.setCurrentRotation(newRot);
            }
            return true;
        }
        if (mc.options.keyUp.matches(keyCode, scanCode)) {
            ViewState state = availableStates.get(currentStateIndex);
            int variantCount = state.getVariantCount(mc, targetStack != null ? targetStack
                    : (mc.player != null ? mc.player.getMainHandItem() : ItemStack.EMPTY));
            if (variantCount > 1 && horizontalSlideProgress >= 1.0f && verticalSlideProgress >= 1.0f) {
                int currentVar = stateVariantIndices.getOrDefault(state, 0);
                if (currentVar < variantCount - 1) {
                    previousVariantIndex = currentVar;
                    stateVariantIndices.put(state, currentVar + 1);
                    verticalSlideProgress = 0.0f;
                    verticalSlideDirection = -1;
                }
            }
            return true;
        }
        if (mc.options.keyDown.matches(keyCode, scanCode)) {
            ViewState state = availableStates.get(currentStateIndex);
            int variantCount = state.getVariantCount(mc, targetStack != null ? targetStack
                    : (mc.player != null ? mc.player.getMainHandItem() : ItemStack.EMPTY));
            if (variantCount > 1 && horizontalSlideProgress >= 1.0f && verticalSlideProgress >= 1.0f) {
                int currentVar = stateVariantIndices.getOrDefault(state, 0);
                if (currentVar > 0) {
                    previousVariantIndex = currentVar;
                    stateVariantIndices.put(state, currentVar - 1);
                    verticalSlideProgress = 0.0f;
                    verticalSlideDirection = 1;
                }
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        for (Entity[] entities : stateEntities.values()) {
            for (Entity entity : entities) {
                if (entity != null) {
                    entity.tickCount++;
                    entity.xo = entity.getX();
                    entity.yo = entity.getY();
                    entity.zo = entity.getZ();
                    entity.yRotO = entity.getYRot();
                    entity.xRotO = entity.getXRot();
                    entity.tick();
                }
            }
        }
        updateDebugUI();
    }

    private static class FullbrightBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;

        public FullbrightBufferSource(MultiBufferSource delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new FullbrightVertexConsumer(delegate.getBuffer(renderType));
        }
    }

    private static class FullbrightVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;

        public FullbrightVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            delegate.color(r, g, b, a);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(15728880);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            delegate.defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
