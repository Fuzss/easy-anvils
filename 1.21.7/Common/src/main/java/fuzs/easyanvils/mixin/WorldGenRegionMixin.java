package fuzs.easyanvils.mixin;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.CommonConfig;
import fuzs.easyanvils.handler.BlockConversionHandler;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldGenRegion.class)
abstract class WorldGenRegionMixin {

    @ModifyVariable(method = "setBlock", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    public BlockState setBlock(BlockState blockState) {
        if (EasyAnvils.CONFIG.get(CommonConfig.class).convertVanillaAnvilDuringWorldGen) {
            return BlockConversionHandler.convertFromVanillaBlock(blockState);
        } else {
            return blockState;
        }
    }
}
