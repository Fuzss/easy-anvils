package fuzs.easyanvils.fabric.init;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.fabric.world.inventory.FabricAnvilMenu;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.MenuType;

public class FabricModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(EasyAnvils.MOD_ID);
    public static final Holder.Reference<MenuType<ModAnvilMenu>> ANVIL_MENU_TYPE = REGISTRIES.registerMenuType("repair",
            () -> FabricAnvilMenu::new);

    public static void bootstrap() {
        // NO-OP
    }
}
