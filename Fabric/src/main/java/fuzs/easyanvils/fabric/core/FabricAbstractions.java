package fuzs.easyanvils.fabric.core;

import fuzs.easyanvils.core.CommonAbstractions;
import net.minecraft.world.item.ItemStack;

public final class FabricAbstractions implements CommonAbstractions {

    @Override
    public boolean isBookEnchantable(ItemStack inputStack, ItemStack bookStack) {
        return true;
    }
}
