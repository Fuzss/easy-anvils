package fuzs.easyanvils.forge.core;

import fuzs.easyanvils.core.CommonAbstractions;
import net.minecraft.world.item.ItemStack;

public final class ForgeAbstractions implements CommonAbstractions {

    @Override
    public boolean isBookEnchantable(ItemStack inputStack, ItemStack bookStack) {
        return inputStack.isBookEnchantable(bookStack);
    }
}
