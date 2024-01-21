package fuzs.easyanvils.config;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

public class ServerConfig implements ConfigCore {
    @Config(category = "prior_work_penalty", description = {"Controls how working an item in the anvil multiple times affects the cost of future operations.", "FIXED: A constant value is added every time the item is worked.", "VANILLA: Penalty doubles every time an item is worked.", "DISABLED: Penalty stays at 0 and does not increase."})
    public PriorWorkPenalty priorWorkPenalty = PriorWorkPenalty.FIXED;
    @Config(category = "prior_work_penalty", description = "Constant to use when \"prior_work_penalty\" is set to \"FIXED\". Every subsequent operation will increase by this value in levels.")
    @Config.IntRange(min = 1)
    public int priorWorkPenaltyConstant = 4;
    @Config(category = "costs", description = {"Max cost of enchantment level allowed to be spent in an anvil. Every operation exceeding the limit will show as 'Too Expensive!' and will be disallowed.", "If set to '-1' the limit is disabled.", "Set to '40' enchantment levels in vanilla."})
    @Config.IntRange(min = -1)
    public int tooExpensiveLimit = -1;
    @Config(category = "miscellaneous", description = "Allow using iron blocks to repair an anvil by one damage stage. Can be automated using dispensers.")
    public boolean anvilRepairing = true;
    @Config(category = "costs", description = "Renaming any item in an anvil no longer costs any enchantment levels at all. Can be restricted to only name tags.")
    public FreeRenames freeRenames = FreeRenames.ALL_ITEMS;
    @Config(category = "miscellaneous", description = "Edit name tags without cost nor anvil, simply by sneak + right-clicking.")
    public boolean editNameTagsNoAnvil = true;
    @Config(category = "miscellaneous", description = "Chance the anvil will break into chipped or damaged variant, or break completely after using. Value is set to 0.12 in vanilla.")
    @Config.DoubleRange(min = 0.0, max = 1.0)
    public double anvilBreakChance = 0.05;
    @Config(category = "miscellaneous", description = "Solely renaming items in an anvil will never cause the anvil to break.")
    public boolean riskFreeAnvilRenaming = true;
    @Config(category = "miscellaneous", description = {"The naming field in anvils and the name tag gui will support formatting codes for setting custom text colors and styles.", "Check out the Minecraft Wiki for all available formatting codes and their usage: https://minecraft.fandom.com/wiki/Formatting_codes#Usage"})
    public boolean renamingSupportsFormatting = true;
    @Config(category = "prior_work_penalty", description = {"FIXED: When renaming / repairing, ignore any prior work penalty on the item. Makes prior work penalty only relevant when new enchantments are added.", "LIMITED: When renaming / repairing cost exceeds max anvil repair cost, limit cost just below max cost.", "VANILLA: Renaming / repairing increase with prior work penalty and will no longer be possible when max cost is exceeded."})
    public RenameAndRepairCost renameAndRepairCosts = RenameAndRepairCost.FIXED;
    @Config(category = "prior_work_penalty", description = "Prevents the prior work penalty from increasing when the item has only been renamed or repaired.")
    public boolean penaltyFreeRenamesAndRepairs = true;
    @Config(description = "Most default value represents vanilla behavior.")
    public final CostsConfig costs = new CostsConfig();
    
    public static class CostsConfig implements ConfigCore {
        @Config(description = "Multiplier for each level of a common enchantment being applied.")
        public int commonEnchantmentMultiplier = 1;
        @Config(description = "Multiplier for each level of a uncommon enchantment being applied.")
        @Config.IntRange(min = 1)
        public int uncommonEnchantmentMultiplier = 2;
        @Config(description = "Multiplier for each level of a rare enchantment being applied.")
        @Config.IntRange(min = 1)
        public int rareEnchantmentMultiplier = 4;
        @Config(description = "Multiplier for each level of a very rare enchantment being applied.")
        @Config.IntRange(min = 1)
        public int veryRareEnchantmentMultiplier = 8;
        @Config(description = "Costs for applying enchantments from enchanted books are halved.")
        public boolean halvedBookCosts = true;
        @Config(description = "The additional cost in levels for each valid repair material an item is repaired with in an anvil.")
        public int repairWithMaterialUnitCost = 1;
        @Config(description = "The additional cost in levels for combining an item with another item of the same kind when the first item is not fully repaired.")
        public int repairWithOtherItemCost = 2;
    }
    
    public enum RenameAndRepairCost {
        VANILLA, FIXED, LIMITED
    }

    public enum FreeRenames {
        OFF(stack -> false),
        ALL_ITEMS(stack -> true),
        NAME_TAGS_ONLY(stack -> stack.is(Items.NAME_TAG));

        public final Predicate<ItemStack> filter;

        FreeRenames(Predicate<ItemStack> filter) {
            this.filter = filter;
        }
    }

    public enum PriorWorkPenalty {
        DISABLED(i -> 0),
        VANILLA(IntUnaryOperator.identity()),
        FIXED(repairCost -> ModAnvilMenu.repairCostToRepairs(repairCost) * EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenaltyConstant);

        public final IntUnaryOperator operator;

        PriorWorkPenalty(IntUnaryOperator operator) {
            this.operator = operator;
        }
    }
}
