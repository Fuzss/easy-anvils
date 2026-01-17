package fuzs.easyanvils.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.network.ClientboundAnvilRepairMessage;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.network.v4.MessageSender;
import fuzs.puzzleslib.api.network.v4.PlayerSet;
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
import org.jspecify.annotations.Nullable;

public class ItemInteractionHandler {

    public static EventResultHolder<InteractionResult> onUseBlock(Player player, Level level, InteractionHand interactionHand, BlockHitResult hitResult) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.anvilRepairing) {
            return EventResultHolder.pass();
        }

        ItemStack itemInHand = player.getItemInHand(interactionHand);
        // This is hardcoded to vanilla iron blocks as a dispenser behavior is also registered which cannot use a block tag.
        if (itemInHand.is(Items.IRON_BLOCK)) {
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            if (blockState.is(BlockTags.ANVIL) && tryRepairAnvil(level, blockPos, blockState)) {
                if (!player.getAbilities().instabuild) {
                    itemInHand.shrink(1);
                }

                return EventResultHolder.interrupt(InteractionResult.SUCCESS);
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
        } else {
            return false;
        }
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
            if (!player.getAbilities().instabuild && blockstate.is(BlockTags.ANVIL)
                    && player.getRandom().nextFloat() < computeAnvilBreakChance(onlyRenaming)) {
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
