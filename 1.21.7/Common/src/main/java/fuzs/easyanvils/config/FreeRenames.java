package fuzs.easyanvils.config;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;

public enum FreeRenames {
    NEVER(itemStack -> false),
    ALL_ITEMS(itemStack -> true),
    NAME_TAGS_ONLY(itemStack -> itemStack.is(Items.NAME_TAG));

    public final Predicate<ItemStack> filter;

    FreeRenames(Predicate<ItemStack> filter) {
        this.filter = filter;
    }
}
