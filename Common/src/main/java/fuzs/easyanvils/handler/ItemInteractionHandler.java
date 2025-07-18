package fuzs.easyanvils.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.network.ClientboundAnvilRepairMessage;
import fuzs.easyanvils.network.ClientboundOpenNameTagEditorMessage;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.network.v4.MessageSender;
import fuzs.puzzleslib.api.network.v4.PlayerSet;
import fuzs.puzzleslib.api.util.v1.InteractionResultHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ItemInteractionHandler {

    public static EventResultHolder<InteractionResult> onUseItem(Player player, Level level, InteractionHand hand) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.editNameTagsNoAnvil) {
            return EventResultHolder.pass();
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && itemInHand.is(Items.NAME_TAG)) {
            MessageSender.broadcast(PlayerSet.ofEntity(player),
                    new ClientboundOpenNameTagEditorMessage(hand, itemInHand.getHoverName()));
            return EventResultHolder.interrupt(InteractionResultHelper.sidedSuccess(level.isClientSide));
        }
        return EventResultHolder.pass();
    }

    public static EventResultHolder<InteractionResult> onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.anvilRepairing) return EventResultHolder.pass();
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.IRON_BLOCK)) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.ANVIL) && tryRepairAnvil(level, pos, state)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return EventResultHolder.interrupt(InteractionResultHelper.sidedSuccess(level.isClientSide));
            }
        }
        return EventResultHolder.pass();
    }

    public static boolean tryRepairAnvil(Level level, BlockPos blockPos, BlockState blockState) {
        BlockState repairedState = getRepairedState(blockState);
        if (repairedState != null) {
            if (level instanceof ServerLevel serverLevel) {
                level.setBlock(blockPos, repairedState, 2);
                MessageSender.broadcast(PlayerSet.nearPosition(blockPos, serverLevel),
                        new ClientboundAnvilRepairMessage(blockPos, repairedState));
            }
            return true;
        }
        return false;
    }

    @Nullable
    private static BlockState getRepairedState(BlockState blockState) {
        blockState = BlockConversionHandler.convertToVanillaBlock(blockState);
        blockState = getVanillaRepairedState(blockState);
        return BlockConversionHandler.convertFromVanillaBlock(blockState);
    }

    @Nullable
    private static BlockState getVanillaRepairedState(@Nullable BlockState blockState) {
        if (blockState != null && blockState.is(Blocks.DAMAGED_ANVIL)) {
            return Blocks.CHIPPED_ANVIL.defaultBlockState()
                    .setValue(AnvilBlock.FACING, blockState.getValue(AnvilBlock.FACING));
        } else if (blockState != null && blockState.is(Blocks.CHIPPED_ANVIL)) {
            return Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, blockState.getValue(AnvilBlock.FACING));
        } else {
            return null;
        }
    }

    public static void onTakeAnvilOutputItemStack(ContainerLevelAccess containerLevelAccess, Player player, boolean onlyRenaming) {
        containerLevelAccess.execute((Level level, BlockPos blockPos) -> {
            BlockState blockstate = level.getBlockState(blockPos);
            if (!player.getAbilities().instabuild && blockstate.is(BlockTags.ANVIL) &&
                    player.getRandom().nextFloat() < computeAnvilBreakChance(onlyRenaming)) {
                BlockState damagedBlockState = AnvilBlock.damage(blockstate);
                if (damagedBlockState == null) {
                    level.removeBlock(blockPos, false);
                    level.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, blockPos, 0);
                } else {
                    level.setBlock(blockPos, damagedBlockState, 2);
                    level.levelEvent(LevelEvent.SOUND_ANVIL_USED, blockPos, 0);
                }
            } else {
                level.levelEvent(LevelEvent.SOUND_ANVIL_USED, blockPos, 0);
            }
        });
    }

    private static float computeAnvilBreakChance(boolean onlyRenaming) {
        if (EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.riskFreeAnvilRenaming && onlyRenaming) {
            return 0.0F;
        } else {
            return (float) EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.anvilBreakChance;
        }
    }
}
