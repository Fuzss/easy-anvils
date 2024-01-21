package fuzs.easyanvils.mixin;

import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(AnvilBlock.class)
abstract class AnvilBlockMixin extends FallingBlock implements EntityBlock {

    public AnvilBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void easyanvils$use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> callbackInfo) {
        if (!this.easyanvils$validAnvil()) return;
        if (!worldIn.isClientSide) {
            if (worldIn.getBlockEntity(pos) instanceof MenuProvider blockEntity) {
                player.openMenu(blockEntity);
                if (player.containerMenu instanceof ModAnvilMenu && blockEntity instanceof Container container) {
                    // items might still be in inventory slots, so this needs to update so that correct result is shown
                    player.containerMenu.slotsChanged(container);
                }
                player.awardStat(Stats.INTERACT_WITH_ANVIL);
                callbackInfo.setReturnValue(InteractionResult.CONSUME);
            }
        }
    }

    @Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
    public void easyanvils$getMenuProvider(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> callback) {
        if (!this.easyanvils$validAnvil()) return;
        if (level.getBlockEntity(pos) instanceof MenuProvider blockEntity) {
            callback.setReturnValue(blockEntity);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (!this.easyanvils$validAnvil()) return null;
        return ModRegistry.ANVIL_BLOCK_ENTITY_TYPE.get().create(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (this.easyanvils$validAnvil() && !state.is(newState.getBlock())) {
            if (worldIn.getBlockEntity(pos) instanceof Container blockEntity) {
                Containers.dropContents(worldIn, pos, blockEntity);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return this.easyanvils$validAnvil();
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        if (!this.easyanvils$validAnvil()) return 0;
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
    }

    @Unique
    private boolean easyanvils$validAnvil() {
        // don't change modded anvils, they usually have their own different behavior from vanilla to justify their existence
        return this == Blocks.ANVIL || this == Blocks.CHIPPED_ANVIL || this == Blocks.DAMAGED_ANVIL;
    }
}
