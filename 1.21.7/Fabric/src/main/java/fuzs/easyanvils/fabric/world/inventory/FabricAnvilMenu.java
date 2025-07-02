package fuzs.easyanvils.fabric.world.inventory;

import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class FabricAnvilMenu extends ModAnvilMenu {

    public FabricAnvilMenu(int id, Inventory inventory) {
        super(id, inventory);
    }

    public FabricAnvilMenu(int id, Inventory inventory, AnvilBlockEntity blockEntity, ContainerLevelAccess containerLevelAccess) {
        super(id, inventory, blockEntity, containerLevelAccess);
    }

    @Override
    public void createResult() {
        this.createAnvilResult();
    }
}
