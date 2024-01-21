//package fuzs.easyanvils.mixin.integration.apotheosis;
//
//import fuzs.easyanvils.integration.apotheosis.ApothAnvilBlockEntity;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.Container;
//import net.minecraft.world.Containers;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.AnvilBlock;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.state.BlockState;
//import org.jetbrains.annotations.Nullable;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import shadows.apotheosis.ench.anvil.ApothAnvilBlock;
//
//@Mixin(ApothAnvilBlock.class)
//abstract class ApothAnvilBlockMixin extends AnvilBlock {
//
//    public ApothAnvilBlockMixin(Properties arg) {
//        super(arg);
//    }
//
//    @Nullable
//    @Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true)
//    public void newBlockEntity(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> callback) {
//        if (!this.easyanvils$validAnvil()) return;
//        callback.setReturnValue(new ApothAnvilBlockEntity(pos, state));
//    }
//
//    @Inject(method = "onRemove", at = @At("HEAD"))
//    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo callback) {
//        if (this.easyanvils$validAnvil() && !state.is(newState.getBlock())) {
//            if (worldIn.getBlockEntity(pos) instanceof Container blockEntity) {
//                Containers.dropContents(worldIn, pos, blockEntity);
//            }
//        }
//    }
//
//    @Unique
//    private boolean easyanvils$validAnvil() {
//        // don't change modded anvils, they usually have their own different behavior from vanilla to justify their usage
//        return this == Blocks.ANVIL || this == Blocks.CHIPPED_ANVIL || this == Blocks.DAMAGED_ANVIL;
//    }
//}
