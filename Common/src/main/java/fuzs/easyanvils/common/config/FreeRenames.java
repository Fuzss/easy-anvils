package fuzs.easyanvils.common.config;

import com.google.common.base.Predicates;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;

public enum FreeRenames {
    NEVER(Predicates.alwaysFalse()),
    ALL_ITEMS(Predicates.alwaysTrue()),
    NAME_TAGS_ONLY((ItemStack itemStack) -> itemStack.is(Items.NAME_TAG));

    public final Predicate<ItemStack> filter;

    FreeRenames(Predicate<ItemStack> filter) {
        this.filter = filter;
    }
}
