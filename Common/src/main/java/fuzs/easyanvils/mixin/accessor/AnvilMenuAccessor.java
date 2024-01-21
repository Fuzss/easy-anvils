package fuzs.easyanvils.mixin.accessor;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilMenu.class)
public interface AnvilMenuAccessor {

    @Accessor("itemName")
    String easyanvils$getItemName();

    @Accessor("itemName")
    void easyanvils$setItemName(String itemName);

    @Accessor("repairItemCountCost")
    int easyanvils$getRepairItemCountCost();

    @Accessor("repairItemCountCost")
    void easyanvils$setRepairItemCountCost(int repairItemCountCost);
}
