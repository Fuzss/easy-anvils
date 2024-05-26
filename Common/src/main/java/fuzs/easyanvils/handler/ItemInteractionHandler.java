package fuzs.easyanvils.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.network.S2CAnvilRepairMessage;
import fuzs.easyanvils.network.S2COpenNameTagEditorMessage;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ItemInteractionHandler {

    public static EventResultHolder<InteractionResult> onUseItem(Player player, Level level, InteractionHand hand) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.editNameTagsNoAnvil) return EventResultHolder.pass();
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && stack.is(Items.NAME_TAG)) {
            if (!level.isClientSide) {
                EasyAnvils.NETWORK.sendTo(new S2COpenNameTagEditorMessage(hand, stack.getHoverName()), (ServerPlayer) player);
            }
            return EventResultHolder.interrupt(InteractionResult.sidedSuccess(level.isClientSide));
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
                return EventResultHolder.interrupt(InteractionResult.sidedSuccess(level.isClientSide));
            }
        }
        return EventResultHolder.pass();
    }

    public static boolean tryRepairAnvil(Level level, BlockPos pos, BlockState state) {
        BlockState repairedState = getRepairedState(state);
        if (repairedState != null) {
            if (!level.isClientSide) {
                level.setBlock(pos, repairedState, 2);
                EasyAnvils.NETWORK.sendToAllNear(new S2CAnvilRepairMessage(pos, repairedState), pos, level);
            }
            return true;
        }
        return false;
    }

    @Nullable
    private static BlockState getRepairedState(BlockState oldBlockState) {
        oldBlockState = BlockConversionHandler.convertReplacementToOriginal(oldBlockState);
        BlockState newBlockState;
        if (oldBlockState.is(Blocks.DAMAGED_ANVIL)) {
            newBlockState = Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, oldBlockState.getValue(AnvilBlock.FACING));
        } else if (oldBlockState.is(Blocks.CHIPPED_ANVIL)) {
            newBlockState = Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, oldBlockState.getValue(AnvilBlock.FACING));
        } else {
            return null;
        }
        return BlockConversionHandler.convertOriginalToReplacement(newBlockState);
    }

    public static void onAnvilUse(Player player, ItemStack left, ItemStack right, ItemStack output, MutableFloat breakChance) {
        if (EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.riskFreeAnvilRenaming && right.isEmpty()) {
            breakChance.accept(0.0F);
        } else {
            breakChance.accept((float) EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.anvilBreakChance);
        }
    }
}
