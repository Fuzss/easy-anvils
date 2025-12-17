package fuzs.easyanvils.world.inventory.state;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class BuiltInAnvilMenu extends AnvilMenu implements AnvilMenuState {

    public BuiltInAnvilMenu(Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(-1, inventory, containerLevelAccess);
    }

    @Override
    public void init(ItemStack leftInput, ItemStack rightInput, String itemName) {
        this.inputSlots.setItem(0, leftInput.copy());
        this.inputSlots.setItem(1, rightInput.copy());
        this.itemName = itemName;
        this.cost.set(0);
        this.repairItemCountCost = 0;
    }

    @Override
    public final void fillResultSlots() {
        this.createResult();
    }

    @Override
    public ItemStack getLeftInput() {
        return this.inputSlots.getItem(0);
    }

    @Override
    public ItemStack getRightInput() {
        return this.inputSlots.getItem(1);
    }

    @Override
    public ItemStack getResult() {
        return this.resultSlots.getItem(0);
    }

    @Override
    public int getRepairItemCountCost() {
        return this.repairItemCountCost;
    }

    @Override
    public String getItemName() {
        return this.itemName;
    }

    @Override
    public int getLevelCost() {
        return this.getCost();
    }
}
