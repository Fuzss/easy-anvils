package fuzs.easyanvils.neoforge.core;

import fuzs.easyanvils.core.CommonAbstractions;
import net.minecraft.world.item.ItemStack;

public final class NeoForgeAbstractions implements CommonAbstractions {

    @Override
    public boolean isBookEnchantable(ItemStack inputStack, ItemStack bookStack) {
        return inputStack.isBookEnchantable(bookStack);
    }
}
