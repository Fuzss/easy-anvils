package fuzs.easyanvils.client.handler;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientAbstractions;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockModelHandler {
    private static final Supplier<Map<ModelResourceLocation, ModelResourceLocation>> MODEL_LOCATIONS;

    static {
        MODEL_LOCATIONS = Suppliers.memoize(() -> {
            return BlockConversionHandler.BLOCK_CONVERSIONS.inverse().entrySet().stream().flatMap(entry -> {
                return convertAllBlockStates(entry.getKey(), entry.getValue()).entrySet().stream();
            }).collect(Util.toMap());
        });
    }

    public static void onLoadComplete() {
        // run a custom implementation here, the appropriate method in client mod constructor runs together with other mods, so we might miss some entries
        for (Map.Entry<Block, Block> entry : BlockConversionHandler.BLOCK_CONVERSIONS.entrySet()) {
            RenderType renderType = ClientAbstractions.INSTANCE.getRenderType(entry.getKey());
            ClientAbstractions.INSTANCE.registerRenderType(entry.getValue(), renderType);
        }
    }

    public static EventResultHolder<UnbakedModel> onModifyUnbakedModel(ModelResourceLocation modelLocation, Supplier<UnbakedModel> unbakedModel, Function<ResourceLocation, UnbakedModel> modelGetter, BiConsumer<ResourceLocation, UnbakedModel> modelAdder) {
        if (MODEL_LOCATIONS.get().containsKey(modelLocation)) {
            ResourceLocation resourceLocation = MODEL_LOCATIONS.get().get(modelLocation).id().withPrefix("block/");
            return EventResultHolder.interrupt(modelGetter.apply(resourceLocation));
        } else {
            return EventResultHolder.pass();
        }
    }

    private static Map<ModelResourceLocation, ModelResourceLocation> convertAllBlockStates(Block oldBlock, Block newBlock) {
        Map<ModelResourceLocation, ModelResourceLocation> modelLocations = Maps.newHashMap();
        for (BlockState oldBlockState : oldBlock.getStateDefinition().getPossibleStates()) {
            BlockState newBlockState = convertBlockState(newBlock.getStateDefinition(), oldBlockState);
            modelLocations.put(BlockModelShaper.stateToModelLocation(oldBlockState), BlockModelShaper.stateToModelLocation(newBlockState));
        }
        return modelLocations;
    }

    private static BlockState convertBlockState(StateDefinition<Block, BlockState> newStateDefinition, BlockState oldBlockState) {
        BlockState newBlockState = newStateDefinition.any();
        for (Map.Entry<Property<?>, Comparable<?>> entry : oldBlockState.getValues().entrySet()) {
            newBlockState = setBlockStateValue(entry.getKey(), entry.getValue(), newStateDefinition::getProperty, newBlockState);
        }
        return newBlockState;
    }

    private static <T extends Comparable<T>, V extends T> BlockState setBlockStateValue(Property<?> oldProperty, Comparable<?> oldValue, Function<String, @Nullable Property<?>> propertyGetter, BlockState blockState) {
        Property<?> newProperty = propertyGetter.apply(oldProperty.getName());
        if (newProperty != null) {
            return blockState.setValue((Property<T>) newProperty, (V) oldValue);
        }
        return blockState;
    }
}
