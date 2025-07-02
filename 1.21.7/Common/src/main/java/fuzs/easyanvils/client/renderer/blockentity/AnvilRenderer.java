package fuzs.easyanvils.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ClientConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class AnvilRenderer implements BlockEntityRenderer<BlockEntity> {
    private final ItemRenderer itemRenderer;

    public AnvilRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPosition) {
        if (!EasyAnvils.CONFIG.get(ClientConfig.class).renderAnvilContents) return;
        Direction direction = blockEntity.getBlockState().getValue(AnvilBlock.FACING);
        int posData = (int) blockEntity.getBlockPos().asLong();
        this.renderFlatItem(blockEntity.getLevel(), 0,
                ((Container) blockEntity).getItem(0),
                direction,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay,
                posData);
        this.renderFlatItem(blockEntity.getLevel(), 1,
                ((Container) blockEntity).getItem(1),
                direction,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay,
                posData);
    }

    private void renderFlatItem(Level level, int index, ItemStack itemStack, Direction direction, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int posData) {
        if (itemStack.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.0, 1.0375, 0.0);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        boolean mirrored = (direction.getAxisDirection().getStep() == 1 ? 1 : 0) != index % 2;
        switch (direction.getAxis()) {
            case X -> {
                if (mirrored) {
                    poseStack.translate(0.25, -0.5, 0.0);
                } else {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                    poseStack.translate(-0.75, 0.5, 0.0);
                }
            }
            case Z -> {
                if (mirrored) {
                    poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
                    poseStack.translate(0.25, 0.5, 0.0);
                } else {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    poseStack.translate(-0.75, -0.5, 0.0);
                }
            }
        }
        poseStack.scale(0.375F, 0.375F, 0.375F);
        this.itemRenderer.renderStatic(itemStack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                level,
                posData + index);
        poseStack.popPose();
    }
}
