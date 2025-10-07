package fuzs.easyanvils.fabric.services;

import fuzs.easyanvils.fabric.world.inventory.FabricAnvilMenu;
import fuzs.easyanvils.services.CommonAbstractions;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.inventory.state.AnvilMenuState;
import fuzs.easyanvils.world.inventory.state.VanillaAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public final class FabricAbstractions implements CommonAbstractions {

    @Override
    public ModAnvilMenu createAnvilMenu(int id, Inventory inventory, AnvilBlockEntity blockEntity, ContainerLevelAccess containerLevelAccess) {
        return new FabricAnvilMenu(id, inventory, blockEntity, containerLevelAccess);
    }

    @Override
    public AnvilMenuState createVanillaAnvilMenu(Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        return new VanillaAnvilMenu(inventory, containerLevelAccess) {
            @Override
            public void createResult() {
                this.createAnvilResult();
            }
        };
    }
}
