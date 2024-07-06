package fuzs.easyanvils.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class CommonConfig implements ConfigCore {
    @Config(description = "Enable a crafting recipe for name tags.")
    public boolean nameTagCraftingRecipe = false;
}
