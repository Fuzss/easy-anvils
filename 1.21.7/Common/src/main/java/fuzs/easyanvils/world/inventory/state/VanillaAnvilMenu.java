package fuzs.easyanvils.world.inventory.state;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * A direct copy from the vanilla method to surpass any mixin or event modifications.
 */
public abstract class VanillaAnvilMenu extends BuiltInAnvilMenu {

    public VanillaAnvilMenu(Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(inventory, containerLevelAccess);
    }

    protected final void createAnvilResult() {
        ItemStack itemStack = this.inputSlots.getItem(0);
        this.onlyRenaming = false;
        this.cost.set(1);
        int i = 0;
        long l = 0L;
        int j = 0;
        if (!itemStack.isEmpty() && EnchantmentHelper.canStoreEnchantments(itemStack)) {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = this.inputSlots.getItem(1);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(
                    itemStack2));
            l += (long) itemStack.getOrDefault(DataComponents.REPAIR_COST, 0).intValue() +
                    itemStack3.getOrDefault(DataComponents.REPAIR_COST, 0).intValue();
            this.repairItemCountCost = 0;
            if (!itemStack3.isEmpty()) {
                boolean bl = itemStack3.has(DataComponents.STORED_ENCHANTMENTS);
                if (itemStack2.isDamageableItem() && itemStack.isValidRepairItem(itemStack3)) {
                    int k = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
                    if (k <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }

                    int m;
                    for (m = 0; k > 0 && m < itemStack3.getCount(); m++) {
                        int n = itemStack2.getDamageValue() - k;
                        itemStack2.setDamageValue(n);
                        i++;
                        k = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
                    }

                    this.repairItemCountCost = m;
                } else {
                    if (!bl && (!itemStack2.is(itemStack3.getItem()) || !itemStack2.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }

                    if (itemStack2.isDamageableItem() && !bl) {
                        int kx = itemStack.getMaxDamage() - itemStack.getDamageValue();
                        int m = itemStack3.getMaxDamage() - itemStack3.getDamageValue();
                        int n = m + itemStack2.getMaxDamage() * 12 / 100;
                        int o = kx + n;
                        int p = itemStack2.getMaxDamage() - o;
                        if (p < 0) {
                            p = 0;
                        }

                        if (p < itemStack2.getDamageValue()) {
                            itemStack2.setDamageValue(p);
                            i += 2;
                        }
                    }

                    ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack3);
                    boolean bl2 = false;
                    boolean bl3 = false;

                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                        Holder<Enchantment> holder = (Holder<Enchantment>) entry.getKey();
                        int q = mutable.getLevel(holder);
                        int r = entry.getIntValue();
                        r = q == r ? r + 1 : Math.max(r, q);
                        Enchantment enchantment = holder.value();
                        boolean bl4 = enchantment.canEnchant(itemStack);
                        if (this.player.hasInfiniteMaterials() || itemStack.is(Items.ENCHANTED_BOOK)) {
                            bl4 = true;
                        }

                        for (Holder<Enchantment> holder2 : mutable.keySet()) {
                            if (!holder2.equals(holder) && !Enchantment.areCompatible(holder, holder2)) {
                                bl4 = false;
                                i++;
                            }
                        }

                        if (!bl4) {
                            bl3 = true;
                        } else {
                            bl2 = true;
                            if (r > enchantment.getMaxLevel()) {
                                r = enchantment.getMaxLevel();
                            }

                            mutable.set(holder, r);
                            int s = enchantment.getAnvilCost();
                            if (bl) {
                                s = Math.max(1, s / 2);
                            }

                            i += s * r;
                            if (itemStack.getCount() > 1) {
                                i = 40;
                            }
                        }
                    }

                    if (bl3 && !bl2) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }
                }
            }

            if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
                if (!this.itemName.equals(itemStack.getHoverName().getString())) {
                    j = 1;
                    i += j;
                    itemStack2.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
                }
            } else if (itemStack.has(DataComponents.CUSTOM_NAME)) {
                j = 1;
                i += j;
                itemStack2.remove(DataComponents.CUSTOM_NAME);
            }

            int t = i <= 0 ? 0 : (int) Mth.clamp(l + i, 0L, 2147483647L);
            this.cost.set(t);
            if (i <= 0) {
                itemStack2 = ItemStack.EMPTY;
            }

            if (j == i && j > 0) {
                if (this.cost.get() >= 40) {
                    this.cost.set(39);
                }

                this.onlyRenaming = true;
            }

            if (this.cost.get() >= 40 && !this.player.hasInfiniteMaterials()) {
                itemStack2 = ItemStack.EMPTY;
            }

            if (!itemStack2.isEmpty()) {
                int kxx = itemStack2.getOrDefault(DataComponents.REPAIR_COST, 0);
                if (kxx < itemStack3.getOrDefault(DataComponents.REPAIR_COST, 0)) {
                    kxx = itemStack3.getOrDefault(DataComponents.REPAIR_COST, 0);
                }

                if (j != i || j == 0) {
                    kxx = calculateIncreasedRepairCost(kxx);
                }

                itemStack2.set(DataComponents.REPAIR_COST, kxx);
                EnchantmentHelper.setEnchantments(itemStack2, mutable.toImmutable());
            }

            this.resultSlots.setItem(0, itemStack2);
            this.broadcastChanges();
        } else {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
        }
    }
}
