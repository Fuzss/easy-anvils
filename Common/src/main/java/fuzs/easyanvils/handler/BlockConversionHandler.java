package fuzs.easyanvils.handler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import fuzs.puzzleslib.api.block.v1.BlockConversionHelper;
import fuzs.puzzleslib.api.event.v1.AddBlockEntityTypeBlocksCallback;
import fuzs.puzzleslib.api.event.v1.RegistryEntryAddedCallback;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.*;

public class BlockConversionHandler {
    private static final BiMap<Block, Block> BLOCK_CONVERSIONS = HashBiMap.create();
    private static final Map<BlockState, BlockState> BLOCK_STATE_CONVERSIONS_CACHE = new MapMaker().weakKeys()
            .weakValues()
            .makeMap();

    public static RegistryEntryAddedCallback<Block> onRegistryEntryAdded(Predicate<Block> filter, Function<BlockBehaviour.Properties, Block> factory, String modId) {
        return (Registry<Block> registry, Identifier id, Block block, BiConsumer<Identifier, Supplier<Block>> registrar) -> {
            if (filter.test(block)) {
                Identifier identifier = Identifier.fromNamespaceAndPath(modId, id.getNamespace() + "/" + id.getPath());
                registrar.accept(identifier, () -> {
                    BlockBehaviour.Properties properties = BlockConversionHelper.copyBlockProperties(block, identifier);
                    Block newBlock = factory.apply(properties);
                    BLOCK_CONVERSIONS.put(block, newBlock);
                    return newBlock;
                });
            }
        };
    }

    public static BiMap<Block, Block> getBlockConversions() {
        return Maps.unmodifiableBiMap(BLOCK_CONVERSIONS);
    }

    public static AddBlockEntityTypeBlocksCallback onAddBlockEntityTypeBlocks(Holder.Reference<? extends BlockEntityType<?>> blockEntityType) {
        return (BiConsumer<BlockEntityType<?>, Block> consumer) -> {
            for (Map.Entry<Block, Block> entry : BLOCK_CONVERSIONS.entrySet()) {
                consumer.accept(blockEntityType.value(), entry.getValue());
            }
        };
    }

    public static PlayerInteractEvents.UseBlock onUseBlock(TagKey<Block> unalteredBlocks, @Nullable SoundEvent soundEvent, BooleanSupplier convertVanillaBlock) {
        return (Player player, Level level, InteractionHand interactionHand, BlockHitResult hitResult) -> {
            if (!convertVanillaBlock.getAsBoolean()) {
                return EventResultHolder.pass();
            }

            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            Block block = BLOCK_CONVERSIONS.get(blockState.getBlock());
            if (block != null && !blockState.is(unalteredBlocks)) {
                if (level instanceof ServerLevel) {
                    level.destroyBlock(blockPos, false, player);
                    if (soundEvent != null) {
                        level.playSound(null,
                                blockPos,
                                soundEvent,
                                SoundSource.BLOCKS,
                                1.0F,
                                level.random.nextFloat() * 0.1F + 0.9F);
                    }
                }

                level.setBlock(blockPos, block.withPropertiesOf(blockState), Block.UPDATE_ALL);
                return EventResultHolder.interrupt(InteractionResult.SUCCESS);
            } else {
                return EventResultHolder.pass();
            }
        };
    }

    public static TagsUpdatedCallback onTagsUpdated(TagKey<Block> unalteredBlocks, Predicate<Block> filter) {
        return (HolderLookup.Provider registries, boolean client) -> {
            for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
                if (entry.getValue() instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    setItemForBlock(filter, blockItem, block);
                    setBlockForItem(unalteredBlocks, blockItem, block);
                }
            }

            BLOCK_CONVERSIONS.forEach(BlockConversionHelper::copyBoundTags);
        };
    }

    private static void setItemForBlock(Predicate<Block> filter, BlockItem blockItem, Block block) {
        if (filter.test(block)) {
            BlockConversionHelper.setItemForBlock(BLOCK_CONVERSIONS.get(block), blockItem);
        }
    }

    private static void setBlockForItem(TagKey<Block> tagKey, BlockItem blockItem, Block block) {
        Block oldBlock;
        Block newBlock = BLOCK_CONVERSIONS.get(block);
        if (newBlock != null) {
            oldBlock = block;
        } else {
            oldBlock = BLOCK_CONVERSIONS.inverse().get(block);
            if (oldBlock != null) {
                newBlock = block;
            } else {
                return;
            }
        }

        if (oldBlock.builtInRegistryHolder().is(tagKey)) {
            BlockConversionHelper.setBlockForItem(blockItem, oldBlock);
        } else {
            BlockConversionHelper.setBlockForItem(blockItem, newBlock);
        }
    }

    @Nullable
    public static BlockState convertToVanillaBlock(@Nullable BlockState blockState) {
        // mod replacement block coming in, we need to forward the original
        return applyBlockConversion(blockState, true);
    }

    @Nullable
    public static BlockState convertFromVanillaBlock(@Nullable BlockState blockState) {
        // original block coming in, we need to convert back to our replacement
        return applyBlockConversion(blockState, false);
    }

    @Nullable
    private static BlockState applyBlockConversion(@Nullable BlockState blockState, boolean inverse) {
        if (blockState != null) {
            return BLOCK_STATE_CONVERSIONS_CACHE.computeIfAbsent(blockState, applyBlockConversion(inverse));
        } else {
            return null;
        }
    }

    private static UnaryOperator<BlockState> applyBlockConversion(boolean inverse) {
        return (BlockState blockState) -> {
            BiMap<Block, Block> blockConversions = inverse ? BLOCK_CONVERSIONS.inverse() : BLOCK_CONVERSIONS;
            if (blockState != null && blockConversions.containsKey(blockState.getBlock())) {
                Block block = blockConversions.get(blockState.getBlock());
                return copyAllProperties(blockState, block.defaultBlockState());
            } else {
                return blockState;
            }
        };
    }

    private static <T extends Comparable<T>, V extends T> BlockState copyAllProperties(BlockState oldBlockState, BlockState newBlockState) {
        for (Map.Entry<Property<?>, Comparable<?>> entry : oldBlockState.getValues().entrySet()) {
            newBlockState = newBlockState.trySetValue((Property<T>) entry.getKey(), (V) entry.getValue());
        }

        return newBlockState;
    }
}
