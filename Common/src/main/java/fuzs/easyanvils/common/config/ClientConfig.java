package fuzs.easyanvils.common.config;

import fuzs.puzzleslib.common.api.config.v3.Config;
import fuzs.puzzleslib.common.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config(description = "Render inventory contents of an anvil.")
    public boolean renderAnvilContents = true;
}
