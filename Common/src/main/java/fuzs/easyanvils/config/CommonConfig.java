package fuzs.easyanvils.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class CommonConfig implements ConfigCore {
    @Config(description = "Leftover vanilla anvils in a world become unusable until they are broken and replaced.")
    public boolean disableVanillaAnvil = true;
    @Config(description = "Replace vanilla anvils created in structures during world generation. Does not affect already generated blocks.")
    public boolean convertVanillaAnvilDuringWorldGen = true;
}
