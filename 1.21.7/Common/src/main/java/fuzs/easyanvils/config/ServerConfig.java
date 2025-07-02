package fuzs.easyanvils.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ServerConfig implements ConfigCore {
    @Config
    public final PriorWorkPenaltyConfig priorWorkPenalty = new PriorWorkPenaltyConfig();
    @Config
    public final CostsConfig costs = new CostsConfig();
    @Config
    public final MiscellaneousConfig miscellaneous = new MiscellaneousConfig();

    public static class PriorWorkPenaltyConfig implements ConfigCore {
        @Config(description = {"Controls how working an item in the anvil multiple times affects the cost of future operations.", "LIMITED: Penalty doubles every time an item is worked, but every increase cannot exceed a given limit.", "VANILLA: Penalty doubles every time an item is worked.", "NONE: Penalty is disabled by staying at 0 and does not increase."})
        public PriorWorkPenalty priorWorkPenalty = PriorWorkPenalty.LIMITED;
        @Config(description = "Value to use when \"prior_work_penalty\" is set to \"LIMITED\". Every subsequent operation will increase at most by this value in levels.")
        @Config.IntRange(min = 1)
        public int maximumPriorWorkPenaltyIncrease = 4;
        @Config(description = {"FIXED: When renaming / repairing, ignore any prior work penalty on the item. Makes prior work penalty only relevant when new enchantments are added.", "LIMITED: When renaming / repairing cost exceeds max anvil repair cost, limit cost just below max cost.", "VANILLA: Renaming / repairing increase with prior work penalty and will no longer be possible when max cost is exceeded."})
        public RenameAndRepairCost renameAndRepairCosts = RenameAndRepairCost.FIXED;
        @Config(description = "Prevents the prior work penalty from increasing when the item has only been renamed or repaired.")
        public boolean penaltyFreeRenamesAndRepairs = true;
        @Config(description = "Prevents the prior work penalty from increasing when combining two enchanted books.")
        public boolean penaltyFreeEnchantsForBooks = true;
    }
    
    public static class CostsConfig implements ConfigCore {
        @Config(description = {"Max cost of enchantment level allowed to be spent in an anvil. Every operation exceeding the limit will show as 'Too Expensive!' and will be disallowed.", "If set to '-1' the limit is disabled.", "Set to '40' enchantment levels in vanilla."})
        @Config.IntRange(min = -1)
        public int tooExpensiveLimit = -1;
        @Config(description = "Renaming any item in an anvil no longer costs any enchantment levels at all. Can be restricted to only name tags.")
        public FreeRenames freeRenames = FreeRenames.ALL_ITEMS;
        @Config(description = "Costs for applying enchantments from enchanted books are halved.")
        public boolean halvedBookCosts = true;
        @Config(description = "The additional cost in levels for each valid repair material an item is repaired with.")
        @Config.IntRange(min = 0)
        public int repairWithMaterialUnitCost = 1;
        @Config(description = "Restored percentage of full durability for an item after repairing with a single valid repair material.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double repairWithMaterialRestoredDurability = 0.25;
        @Config(description = "The additional cost in levels for combining an item with another item of the same kind when the first item is not fully repaired.")
        @Config.IntRange(min = 0)
        public int repairWithOtherItemCost = 2;
        @Config(description = "Percentage of full durability given as a bonus for an item after combining an item with another item of the same kind.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double repairWithOtherItemBonusDurability = 0.12;
    }

    public static class MiscellaneousConfig implements ConfigCore {
        @Config(description = "Allow using iron blocks to repair an anvil by one damage stage. Can be automated using dispensers.")
        public boolean anvilRepairing = true;
        @Config(description = "Edit name tags without cost nor anvil, simply by sneak + right-clicking.")
        public boolean editNameTagsNoAnvil = true;
        @Config(description = "Chance the anvil will break into chipped or damaged variant, or break completely after using. Value is set to 0.12 in vanilla.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double anvilBreakChance = 0.05;
        @Config(description = "Solely renaming items in an anvil will never cause the anvil to break.")
        public boolean riskFreeAnvilRenaming = true;
        @Config(description = "The naming field in anvils and the name tag gui will support formatting codes for setting custom text colors and styles.")
        public boolean renamingSupportsFormatting = true;
        @Config(description = "Mobs that have a custom name drop a name tag with that name on death.")
        public boolean nameTagsDropFromMobs = false;
    }
}
