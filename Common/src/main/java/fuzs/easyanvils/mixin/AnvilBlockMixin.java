package fuzs.easyanvils.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fuzs.easyanvils.handler.BlockConversionHandler;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(value = AnvilBlock.class, priority = 2000)
abstract class AnvilBlockMixin extends FallingBlock {

    public AnvilBlockMixin(Properties properties) {
        super(properties);
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"))
    private static BlockState damage$0(BlockState blockState) {
        // mod replacement block coming in, we need to forward the original
        if (BlockConversionHandler.BLOCK_CONVERSIONS.containsValue(blockState.getBlock())) {
            Block block = BlockConversionHandler.BLOCK_CONVERSIONS.inverse().get(blockState.getBlock());
            return easyanvils$copy(block.defaultBlockState(), blockState.getValues());
        } else {
            return blockState;
        }
    }

    @ModifyReturnValue(method = "damage", at = @At("RETURN"))
    private static BlockState damage$1(BlockState blockState) {
        // original block coming in, we need to convert back to our replacement
        if (BlockConversionHandler.BLOCK_CONVERSIONS.containsKey(blockState.getBlock())) {
            Block block = BlockConversionHandler.BLOCK_CONVERSIONS.get(blockState.getBlock());
            return easyanvils$copy(block.defaultBlockState(), blockState.getValues());
        } else {
            return blockState;
        }
    }

    @Unique
    private static <T extends Comparable<T>, V extends T> BlockState easyanvils$copy(BlockState blockState, Map<Property<?>, Comparable<?>> values) {
        for (Map.Entry<Property<?>, Comparable<?>> entry : values.entrySet()) {
            blockState = blockState.trySetValue((Property<T>) entry.getKey(), (V) entry.getValue());
        }
        return blockState;
    }
}
