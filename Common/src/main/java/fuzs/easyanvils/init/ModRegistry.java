package fuzs.easyanvils.init;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.api.core.v1.ModLoader;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.BoundTagFactory;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {
    static final RegistryManager REGISTRY = RegistryManager.from(EasyAnvils.MOD_ID);
    public static final Holder.Reference<BlockEntityType<AnvilBlockEntity>> ANVIL_BLOCK_ENTITY_TYPE = REGISTRY.whenNotOn(ModLoader.FORGE).registerBlockEntityType("anvil", () -> {
        BlockEntityType.Builder<AnvilBlockEntity> builder = BlockEntityType.Builder.of(AnvilBlockEntity::new);
        builder.validBlocks = BlockConversionHandler.BLOCK_CONVERSIONS.values();
        return builder;
    });
    public static final Holder.Reference<MenuType<ModAnvilMenu>> ANVIL_MENU_TYPE = REGISTRY.registerMenuType("repair", () -> ModAnvilMenu::new);

    static final BoundTagFactory TAGS = BoundTagFactory.make(EasyAnvils.MOD_ID);
    public static final TagKey<Block> UNALTERED_ANVILS_BLOCK_TAG = TAGS.registerBlockTag("unaltered_anvils");

    public static void touch() {

    }
}
