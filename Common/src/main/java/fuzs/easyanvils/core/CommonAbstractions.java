package fuzs.easyanvils.core;

import fuzs.puzzleslib.api.core.v1.ServiceProviderHelper;
import net.minecraft.world.item.ItemStack;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = ServiceProviderHelper.load(CommonAbstractions.class);

    boolean isBookEnchantable(ItemStack inputStack, ItemStack bookStack);
}
