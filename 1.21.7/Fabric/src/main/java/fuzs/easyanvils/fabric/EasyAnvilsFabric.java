package fuzs.easyanvils.fabric;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.fabric.init.FabricModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class EasyAnvilsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricModRegistry.bootstrap();
        ModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvils::new);
    }
}
