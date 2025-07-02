package fuzs.easyanvils.neoforge.services;

import fuzs.easyanvils.neoforge.world.inventory.NeoForgeAnvilMenu;
import fuzs.easyanvils.services.CommonAbstractions;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.inventory.state.AnvilMenuState;
import fuzs.easyanvils.world.inventory.state.VanillaAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public final class NeoForgeAbstractions implements CommonAbstractions {

    @Override
    public ModAnvilMenu createAnvilMenu(int id, Inventory inventory, AnvilBlockEntity blockEntity, ContainerLevelAccess containerLevelAccess) {
        return new NeoForgeAnvilMenu(id, inventory, blockEntity, containerLevelAccess);
    }

    @Override
    public AnvilMenuState createVanillaAnvilMenu(Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        // this is mostly useless to have on NeoForge now, as the event runs afterward which we cannot prevent,
        // however, this will handle mods injecting their custom anvil behaviour via mixin
        return new VanillaAnvilMenu(inventory, containerLevelAccess) {
            @Override
            public void createResultInternal() {
                this.createAnvilResult();
            }
        };
    }
}
