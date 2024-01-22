package fuzs.easyanvils.world.inventory;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.core.CommonAbstractions;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.util.ComponentDecomposer;
import fuzs.easyanvils.util.FormattedStringDecomposer;
import fuzs.easyanvils.world.inventory.state.AnvilMenuState;
import fuzs.easyanvils.world.inventory.state.BuiltInAnvilMenu;
import fuzs.easyanvils.world.inventory.state.VanillaAnvilMenu;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.Objects;

public class ModAnvilMenu extends AnvilMenu implements ContainerListener {
    private final AnvilMenuState builtInAnvilState;
    private final AnvilMenuState vanillaAnvilState;

    public ModAnvilMenu(int id, Inventory inventory) {
        // this constructor may not override inputSlots as vanilla adds a listener to it which is necessary client-side for the renaming edit box
        // so let this go to super instead of the main constructor to skip any inputSlots shenanigans
        super(id, inventory);
        this.builtInAnvilState = new BuiltInAnvilMenu(inventory, ContainerLevelAccess.NULL);
        this.vanillaAnvilState = new VanillaAnvilMenu(inventory, ContainerLevelAccess.NULL);
    }

    public ModAnvilMenu(int id, Inventory inventory, Container inputSlots, ContainerLevelAccess containerLevelAccess) {
        super(id, inventory, containerLevelAccess);
        this.updateInputSlots(inputSlots);
        this.addSlotListener(this);
        this.builtInAnvilState = new BuiltInAnvilMenu(inventory, containerLevelAccess);
        this.vanillaAnvilState = new VanillaAnvilMenu(inventory, containerLevelAccess);
    }

    private void updateInputSlots(Container inputSlots) {
        this.inputSlots = inputSlots;
        // we could also replace slots directly in the slots list as we have direct access to it,
        // but the Ledger mod hooks into AbstractContainerMenu::addSlot which breaks that mod as we wouldn't be using AbstractContainerMenu::addSlot
        // (they store the menu as a @NotNull value, leads to an NPE)
        this.slots.get(0).container = inputSlots;
        this.slots.get(1).container = inputSlots;
    }

    @Override
    protected void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        this.access.execute((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof AnvilBlockEntity blockEntity) {
                this.updateInputSlots(blockEntity);
            }
        });
    }

    @Override
    public MenuType<?> getType() {
        return ModRegistry.ANVIL_MENU_TYPE.value();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inputSlots.stillValid(player);
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasStack) {
        // change cost requirement from > 0 to >= 0 to allow for free name tag renames
        return (player.getAbilities().instabuild || player.experienceLevel >= this.getCost()) && this.getCost() >= 0;
    }

    @Override
    public void createResult() {
        // this is called during <init> when these aren't populated yet
        if (this.builtInAnvilState == null || this.vanillaAnvilState == null) return;
        // to not break custom anvil recipes from other mods we compare the outcome from the vanilla anvil logic and
        // the actual current anvil logic (with possible alterations from mods via Mixin or the Forge event)
        // if the result is not equal we do nothing and let the interfering mod take the upper hand
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);
        this.builtInAnvilState.init(left, right, this.itemName);
        this.vanillaAnvilState.init(left, right, this.itemName);
        this.builtInAnvilState.fillResultSlots();
        this.vanillaAnvilState.fillResultSlots();
        if (!AnvilMenuState.equals(this.builtInAnvilState, this.vanillaAnvilState)) {
            super.createResult();
        } else {
            this.createResult(left, right, this.itemName);
        }
    }

    private void createResult(ItemStack left, ItemStack right, String itemName) {
        this.setCost(1);
        if (left.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.setCost(0);
        } else {
            ItemStack output = left.copy();
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(output);
            int baseRepairCost = left.getBaseRepairCost() + (right.isEmpty() ? 0 : right.getBaseRepairCost());
            // no prior work penalty, or fixed
            baseRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.operator.applyAsInt(baseRepairCost);
            this.repairItemCountCost = 0;
            boolean isBook = false;

            int repairOperationCost = 0;
            int enchantOperationCost = 0;
            int renameOperationCost = 0;
            final int maxAnvilRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).tooExpensiveLimit;
            if (!right.isEmpty()) {
                isBook = right.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(right).isEmpty();
                if (output.isDamageableItem() && output.getItem().isValidRepairItem(left, right)) {
                    int l2 = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
                    if (l2 <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }

                    int repairMaterials;
                    for (repairMaterials = 0; l2 > 0 && repairMaterials < right.getCount(); ++repairMaterials) {
                        int j3 = output.getDamageValue() - l2;
                        output.setDamageValue(j3);
                        repairOperationCost += EasyAnvils.CONFIG.get(ServerConfig.class).costs.repairWithMaterialUnitCost;
                        l2 = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
                    }

                    this.repairItemCountCost = repairMaterials;
                } else {
                    if (!isBook && (!output.is(right.getItem()) || !output.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }

                    if (output.isDamageableItem() && !isBook) {
                        int l = left.getMaxDamage() - left.getDamageValue();
                        int i1 = right.getMaxDamage() - right.getDamageValue();
                        int j1 = i1 + output.getMaxDamage() * 12 / 100;
                        int k1 = l + j1;
                        int l1 = output.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < output.getDamageValue()) {
                            output.setDamageValue(l1);
                            repairOperationCost += EasyAnvils.CONFIG.get(ServerConfig.class).costs.repairWithOtherItemCost;
                        }
                    }

                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(right);
                    boolean flag2 = false;
                    boolean flag3 = false;

                    for (Enchantment enchantment1 : map1.keySet()) {
                        if (enchantment1 != null) {
                            int otherEnchantmentLevel = map.getOrDefault(enchantment1, 0);
                            int enchantmentLevel = map1.get(enchantment1);
                            enchantmentLevel = otherEnchantmentLevel == enchantmentLevel ? enchantmentLevel + 1 : Math.max(enchantmentLevel, otherEnchantmentLevel);
                            boolean compatibleWithItem = enchantment1.canEnchant(left);
                            if (this.player.getAbilities().instabuild || left.is(Items.ENCHANTED_BOOK)) {
                                compatibleWithItem = true;
                            }

                            for (Enchantment enchantment : map.keySet()) {
                                if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
                                    compatibleWithItem = false;
                                    ++enchantOperationCost;
                                }
                            }

                            if (!compatibleWithItem) {
                                // allow using items with just incompatible enchantments as repair material
                                if (repairOperationCost > 0) continue;
                                flag3 = true;
                            } else {
                                flag2 = true;
                                if (enchantmentLevel > enchantment1.getMaxLevel()) {
                                    enchantmentLevel = enchantment1.getMaxLevel();
                                }

                                // prevent a level higher than max level from being lowered to max value
                                int maxLevel = Math.max(map.getOrDefault(enchantment1, 0), map1.get(enchantment1));
                                maxLevel = Math.max(maxLevel, enchantmentLevel);
                                if (maxLevel != enchantmentLevel) {
                                    enchantmentLevel = maxLevel;
                                }

                                int rarityCostMultiplier = switch (enchantment1.getRarity()) {
                                    case COMMON -> EasyAnvils.CONFIG.get(ServerConfig.class).costs.commonEnchantmentMultiplier;
                                    case UNCOMMON -> EasyAnvils.CONFIG.get(ServerConfig.class).costs.uncommonEnchantmentMultiplier;
                                    case RARE -> EasyAnvils.CONFIG.get(ServerConfig.class).costs.rareEnchantmentMultiplier;
                                    case VERY_RARE -> EasyAnvils.CONFIG.get(ServerConfig.class).costs.veryRareEnchantmentMultiplier;
                                };

                                if (isBook && EasyAnvils.CONFIG.get(ServerConfig.class).costs.halvedBookCosts) {
                                    rarityCostMultiplier = Math.max(1, rarityCostMultiplier / 2);
                                }

                                // don't increase repair cost when an enchantment is already present and the level does not change (already at max level probably)
                                Integer oldEnchantmentLevel = map.put(enchantment1, enchantmentLevel);
                                if (oldEnchantmentLevel == null || oldEnchantmentLevel != enchantmentLevel) {
                                    enchantOperationCost += rarityCostMultiplier * enchantmentLevel;
                                }

                                // different implementation for showing 'Too Expensive!' client-side from vanilla
                                if (left.getCount() > 1 && !this.player.getAbilities().instabuild) {
                                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                                    this.setCost(-1);
                                    return;
                                }
                            }
                        }
                    }

                    if (flag3 && !flag2) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.setCost(0);
                        return;
                    }
                }
            }

            boolean hasRenamedItem = false;
            if (ComponentDecomposer.getStringLength(itemName) == 0) {
                if (left.hasCustomHoverName()) {
                    renameOperationCost = EasyAnvils.CONFIG.get(ServerConfig.class).freeRenames.filter.test(left) ? 0 : 1;
                    hasRenamedItem = true;
                    output.resetHoverName();
                }
            } else if (!itemName.equals(ComponentDecomposer.toFormattedString(left.getHoverName()))) {
                renameOperationCost = EasyAnvils.CONFIG.get(ServerConfig.class).freeRenames.filter.test(left) ? 0 : 1;
                hasRenamedItem = true;
                output.setHoverName(ComponentDecomposer.toFormattedComponent(itemName));
            }

            if (isBook && !CommonAbstractions.INSTANCE.isBookEnchantable(output, right)) {
                output = ItemStack.EMPTY;
            }

            int allOperationsCost = enchantOperationCost + repairOperationCost + renameOperationCost;
            if (allOperationsCost == 0) {
                this.setCost(0);
                // when renaming is free make sure to let the item stack pass without being cleared
                if (!hasRenamedItem) {
                    output = ItemStack.EMPTY;
                }
            } else if (enchantOperationCost == 0 && EasyAnvils.CONFIG.get(ServerConfig.class).renameAndRepairCosts == ServerConfig.RenameAndRepairCost.FIXED) {
                this.setCost(allOperationsCost);
            } else {
                this.setCost(baseRepairCost + allOperationsCost);
            }

            if (enchantOperationCost == 0 && this.getCost() >= maxAnvilRepairCost && EasyAnvils.CONFIG.get(ServerConfig.class).renameAndRepairCosts == ServerConfig.RenameAndRepairCost.LIMITED) {
                // we have removed the max repair limit, so just use the vanilla limit here
                if (maxAnvilRepairCost == -1) {
                    this.setCost(39);
                } else {
                    this.setCost(maxAnvilRepairCost - 1);
                }
            }

            // allow for custom max enchantment levels limit
            if (this.getCost() >= maxAnvilRepairCost && maxAnvilRepairCost != -1 && !this.player.getAbilities().instabuild) {
                output = ItemStack.EMPTY;
            }

            if (!output.isEmpty()) {
                int outputRepairCost = output.getBaseRepairCost();
                if (!right.isEmpty() && outputRepairCost < right.getBaseRepairCost()) {
                    outputRepairCost = right.getBaseRepairCost();
                }

                if (allOperationsCost > 0 && (enchantOperationCost > 0 || !EasyAnvils.CONFIG.get(ServerConfig.class).penaltyFreeRenamesAndRepairs)) {
                    outputRepairCost = AnvilMenu.calculateIncreasedRepairCost(outputRepairCost);
                }

                // don't add tag when there is no repair cost
                if (outputRepairCost > 0) {
                    output.setRepairCost(outputRepairCost);
                }
                EnchantmentHelper.setEnchantments(map, output);
            }

            this.resultSlots.setItem(0, output);
            this.broadcastChanges();
        }
    }

    public void setCost(int cost) {
        this.setData(0, cost);
    }

    @Override
    public void removed(Player player) {
        // copied from container super method
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack itemstack = this.getCarried();
            if (!itemstack.isEmpty()) {
                if (player.isAlive() && !serverPlayer.hasDisconnected()) {
                    player.getInventory().placeItemBackInInventory(itemstack);
                } else {
                    player.drop(itemstack, false);
                }
                this.setCarried(ItemStack.EMPTY);
            }
        }
        this.removeSlotListener(this);
    }

    @Override
    public boolean setItemName(String newName) {
        newName = FormattedStringDecomposer.filterText(newName);
        if (ComponentDecomposer.getStringLength(newName) <= 50 && !Objects.equals(newName, this.itemName)) {
            this.itemName = newName;
            if (this.getSlot(2).hasItem()) {
                ItemStack itemStack = this.getSlot(2).getItem();
                setFormattedItemName(newName, itemStack);
            }

            this.createResult();
            return true;
        }

        return false;
    }

    public static void setFormattedItemName(String newName, ItemStack itemStack) {
        Component component = ComponentDecomposer.toFormattedComponent(newName);
        if (component.getString().isEmpty()) {
            itemStack.resetHoverName();
        } else {
            itemStack.setHoverName(component);
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        // this is only ever run server side
        if (containerToSend == this) {
            if (dataSlotIndex >= 0 && dataSlotIndex < 2) {
                this.slotsChanged(this.inputSlots);
            }
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        // NO-OP
    }

    public static int repairCostToRepairs(int repairCost) {
        repairCost++;
        int repairs = 0;
        while (repairCost >= 2) {
            repairCost /= 2;
            repairs++;
        }
        return repairs;
    }
}
