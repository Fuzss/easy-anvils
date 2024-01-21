package fuzs.easyanvils.init;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.api.core.v1.ModLoader;
import fuzs.puzzleslib.api.init.v2.RegistryManager;
import fuzs.puzzleslib.api.init.v2.RegistryReference;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {
    static final RegistryManager REGISTRY = RegistryManager.instant(EasyAnvils.MOD_ID);
    public static final RegistryReference<BlockEntityType<BlockEntity>> ANVIL_BLOCK_ENTITY_TYPE = REGISTRY.whenNotOn(ModLoader.FORGE).registerBlockEntityType("anvil", () -> BlockEntityType.Builder.of(AnvilBlockEntity::new, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL));
    public static final RegistryReference<MenuType<ModAnvilMenu>> ANVIL_MENU_TYPE = REGISTRY.registerMenuType("repair", () -> ModAnvilMenu::new);

    public static void touch() {

    }
}
