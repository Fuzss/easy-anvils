package fuzs.easyanvils.neoforge.world.inventory;

import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class NeoForgeAnvilMenu extends ModAnvilMenu {

    public NeoForgeAnvilMenu(int id, Inventory inventory) {
        super(id, inventory);
    }

    public NeoForgeAnvilMenu(int id, Inventory inventory, AnvilBlockEntity blockEntity, ContainerLevelAccess containerLevelAccess) {
        super(id, inventory, blockEntity, containerLevelAccess);
    }

    @Override
    protected void createResultInternal() {
        this.createAnvilResult();
    }
}
