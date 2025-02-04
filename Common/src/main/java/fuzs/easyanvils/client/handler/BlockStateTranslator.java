package fuzs.easyanvils.client.handler;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BlockStateTranslator {

    public Map<ModelResourceLocation, ModelResourceLocation> convertAllBlockStates(Block oldBlock, Block newBlock) {
        Map<ModelResourceLocation, ModelResourceLocation> modelLocations = new HashMap<>();
        for (BlockState oldBlockState : oldBlock.getStateDefinition().getPossibleStates()) {
            BlockState newBlockState = this.convertBlockState(newBlock.getStateDefinition(), oldBlockState);
            modelLocations.put(BlockModelShaper.stateToModelLocation(oldBlockState),
                    BlockModelShaper.stateToModelLocation(newBlockState));
        }
        return modelLocations;
    }

    private BlockState convertBlockState(StateDefinition<Block, BlockState> newStateDefinition, BlockState oldBlockState) {
        BlockState newBlockState = newStateDefinition.any();
        for (Map.Entry<Property<?>, Comparable<?>> entry : oldBlockState.getValues().entrySet()) {
            newBlockState = this.setBlockStateValue(entry.getKey(),
                    entry.getValue(),
                    newStateDefinition::getProperty,
                    newBlockState);
        }
        return newBlockState;
    }

    private <T extends Comparable<T>, V extends T> BlockState setBlockStateValue(Property<?> oldProperty, Comparable<?> oldValue, Function<String, @Nullable Property<?>> propertyGetter, BlockState blockState) {
        Property<?> newProperty = propertyGetter.apply(oldProperty.getName());
        if (newProperty != null) {
            Comparable<?> newValue = this.getNewPropertyValue(oldProperty, newProperty, oldValue);
            return blockState.setValue((Property<T>) newProperty, (V) newValue);
        }
        return blockState;
    }

    protected Comparable<?> getNewPropertyValue(Property<?> oldProperty, Property<?> newProperty, Comparable<?> oldValue) {
        return oldValue;
    }
}
