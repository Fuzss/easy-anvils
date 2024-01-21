package fuzs.easyanvils.init;

import fuzs.easyanvils.world.level.block.entity.ForgeAnvilBlockEntity;
import fuzs.puzzleslib.api.init.v2.RegistryReference;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ForgeModRegistry {
    public static final RegistryReference<BlockEntityType<BlockEntity>> ANVIL_BLOCK_ENTITY_TYPE = ModRegistry.REGISTRY.registerBlockEntityType("anvil", () -> BlockEntityType.Builder.of(getBlockEntityFactory(), Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL));

    private static BlockEntityType.BlockEntitySupplier<BlockEntity> getBlockEntityFactory() {
//        if (ModLoaderEnvironment.INSTANCE.isModLoadedSafe("apotheosis")) {
//            return new BlockEntityType.BlockEntitySupplier<>() {
//
//                @Override
//                public BlockEntity create(BlockPos blockPos, BlockState blockState) {
//                    return new ApothAnvilBlockEntity(blockPos, blockState);
//                }
//            };
//        }
        return ForgeAnvilBlockEntity::new;
    }

    public static void touch() {

    }
}
