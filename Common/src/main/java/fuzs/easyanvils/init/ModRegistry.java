package fuzs.easyanvils.init;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collections;

public class ModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(EasyAnvils.MOD_ID);
    public static final Holder.Reference<BlockEntityType<AnvilBlockEntity>> ANVIL_BLOCK_ENTITY_TYPE = REGISTRIES.registerBlockEntityType(
            "anvil",
            AnvilBlockEntity::new,
            Collections::emptySet);
    public static final Holder.Reference<MenuType<ModAnvilMenu>> ANVIL_MENU_TYPE = REGISTRIES.registerMenuType("repair",
            () -> ModAnvilMenu::new);

    static final TagFactory TAGS = TagFactory.make(EasyAnvils.MOD_ID);
    public static final TagKey<Block> UNALTERED_ANVILS_BLOCK_TAG = TAGS.registerBlockTag("unaltered_anvils");

    public static void bootstrap() {
        // NO-OP
    }
}
