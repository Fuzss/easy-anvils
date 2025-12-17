package fuzs.easyanvils.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.renderer.blockentity.state.AnvilRenderState;
import fuzs.easyanvils.config.ClientConfig;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class AnvilRenderer implements BlockEntityRenderer<AnvilBlockEntity, AnvilRenderState> {
    private final ItemModelResolver itemModelResolver;

    public AnvilRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public AnvilRenderState createRenderState() {
        return new AnvilRenderState();
    }

    @Override
    public void extractRenderState(AnvilBlockEntity blockEntity, AnvilRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity,
                renderState,
                partialTick,
                cameraPosition,
                breakProgress);
        int position = (int) blockEntity.getBlockPos().asLong();
        renderState.items = new ArrayList<>();
        if (EasyAnvils.CONFIG.get(ClientConfig.class).renderAnvilContents) {
            for (int i = 0; i < blockEntity.getContainerSize(); i++) {
                ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                this.itemModelResolver.updateForTopItem(itemStackRenderState,
                        blockEntity.getItem(i),
                        ItemDisplayContext.FIXED,
                        blockEntity.getLevel(),
                        null,
                        position + i);
                renderState.items.add(itemStackRenderState);
            }
        }

        // light is normally always 0 since it checks inside the crafting table block which is solid, but contents are rendered in the block above
        renderState.itemLightCoords = blockEntity.getLevel() != null ?
                LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above()) : 0XF000F0;
        renderState.direction = blockEntity.getBlockState().getValue(AnvilBlock.FACING);
    }

    @Override
    public void submit(AnvilRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        for (int i = 0; i < renderState.items.size(); i++) {
            ItemStackRenderState itemStackRenderState = renderState.items.get(i);
            if (!itemStackRenderState.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.0F, 1.0375F, 0.0F);
                poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
                boolean isMirrored = (renderState.direction.getAxisDirection().getStep() == 1 ? 1 : 0) != i % 2;
                switch (renderState.direction.getAxis()) {
                    case X -> {
                        if (isMirrored) {
                            poseStack.translate(0.25F, -0.5F, 0.0F);
                        } else {
                            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                            poseStack.translate(-0.75F, 0.5F, 0.0F);
                        }
                    }
                    case Z -> {
                        if (isMirrored) {
                            poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
                            poseStack.translate(0.25F, 0.5F, 0.0F);
                        } else {
                            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                            poseStack.translate(-0.75F, -0.5F, 0.0F);
                        }
                    }
                }

                poseStack.scale(0.375F, 0.375F, 0.375F);
                itemStackRenderState.submit(poseStack,
                        nodeCollector,
                        renderState.itemLightCoords,
                        OverlayTexture.NO_OVERLAY,
                        0);
                poseStack.popPose();
            }
        }
    }
}
