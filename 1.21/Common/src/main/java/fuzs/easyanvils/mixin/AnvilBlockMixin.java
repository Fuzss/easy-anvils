package fuzs.easyanvils.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fuzs.easyanvils.handler.BlockConversionHandler;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Run after mods have had a chance to apply Mixins to the method below.
@Mixin(value = AnvilBlock.class, priority = 2000)
abstract class AnvilBlockMixin extends FallingBlock {

    public AnvilBlockMixin(Properties properties) {
        super(properties);
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"))
    private static BlockState damage$0(BlockState blockState) {
        return BlockConversionHandler.convertReplacementToOriginal(blockState);
    }

    @ModifyReturnValue(method = "damage", at = @At("RETURN"))
    private static BlockState damage$1(BlockState blockState) {
        return BlockConversionHandler.convertOriginalToReplacement(blockState);
    }
}
