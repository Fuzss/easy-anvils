package fuzs.easyanvils.world.inventory;

import fuzs.easyanvils.mixin.accessor.AnvilMenuAccessor;
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
        ((AnvilMenuAccessor) this).easyanvils$setItemName(itemName);
        this.setCost(0);
        ((AnvilMenuAccessor) this).easyanvils$setRepairItemCountCost(0);
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
        return ((AnvilMenuAccessor) this).easyanvils$getRepairItemCountCost();
    }

    @Override
    public String getItemName() {
        return ((AnvilMenuAccessor) this).easyanvils$getItemName();
    }

    @Override
    public int getLevelCost() {
        return this.getCost();
    }

    public void setCost(int cost) {
        this.setData(0, cost);
    }

    public void setRepairItemCountCost(int repairItemCountCost) {
        ((AnvilMenuAccessor) this).easyanvils$setRepairItemCountCost(repairItemCountCost);
    }
}
