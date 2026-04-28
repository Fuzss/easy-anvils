package fuzs.easyanvils.common.client.handler;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public class BlockStateTranslator {
    public static final BlockStateTranslator INSTANCE = new BlockStateTranslator();

    public Map<BlockState, BlockState> convertAllBlockStates(Block newBlock, Block oldBlock) {
        return newBlock.getStateDefinition()
                .getPossibleStates()
                .stream()
                .collect(ImmutableMap.toImmutableMap(Function.identity(), blockState -> {
                    return this.convertBlockState(oldBlock.getStateDefinition(), blockState);
                }));
    }

    private BlockState convertBlockState(StateDefinition<Block, BlockState> newStateDefinition, BlockState oldBlockState) {
        BlockState newBlockState = newStateDefinition.any();
        for (Property.Value<?> value : oldBlockState.getValues().toList()) {
            newBlockState = this.setBlockStateValue(value.property(),
                    value.value(),
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
        } else {
            return blockState;
        }
    }

    protected Comparable<?> getNewPropertyValue(Property<?> oldProperty, Property<?> newProperty, Comparable<?> oldValue) {
        return oldValue;
    }
}
