package fuzs.easyanvils.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config(description = "Render inventory contents of an anvil.")
    public boolean renderAnvilContents = true;
    @Config(description = "Add a tooltip to name tag items explaining how the change the name on the fly.")
    public boolean nameTagTooltip = true;
}
