package fuzs.easyanvils.forge.init;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.forge.world.level.block.entity.ForgeAnvilBlockEntity;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ForgeModRegistry {
    static final RegistryManager REGISTRY = RegistryManager.from(EasyAnvils.MOD_ID);
    public static final Holder.Reference<BlockEntityType<AnvilBlockEntity>> ANVIL_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityType("anvil", () -> {
        BlockEntityType.Builder<AnvilBlockEntity> builder = BlockEntityType.Builder.of(ForgeAnvilBlockEntity::new);
        builder.validBlocks = BlockConversionHandler.BLOCK_CONVERSIONS.values();
        return builder;
    });

    public static void touch() {

    }
}
