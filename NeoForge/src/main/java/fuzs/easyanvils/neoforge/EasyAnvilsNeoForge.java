package fuzs.easyanvils.neoforge;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.data.ModBlockTagsProvider;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.neoforge.init.NeoForgeModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.puzzleslib.neoforge.api.init.v3.capability.NeoForgeCapabilityHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(EasyAnvils.MOD_ID)
public class EasyAnvilsNeoForge {

    public EasyAnvilsNeoForge() {
        NeoForgeModRegistry.bootstrap();
        ModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvils::new);
        DataProviderHelper.registerDataProviders(EasyAnvils.MOD_ID, ModBlockTagsProvider::new);
    }

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        NeoForgeCapabilityHelper.registerRestrictedBlockEntityContainer(ModRegistry.ANVIL_BLOCK_ENTITY_TYPE);
    }
}
