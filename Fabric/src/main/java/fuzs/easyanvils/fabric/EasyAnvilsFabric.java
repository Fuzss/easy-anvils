package fuzs.easyanvils.fabric;

import fuzs.easyanvils.common.EasyAnvils;
import fuzs.easyanvils.fabric.init.FabricModRegistry;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class EasyAnvilsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricModRegistry.bootstrap();
        ModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvils::new);
    }
}
