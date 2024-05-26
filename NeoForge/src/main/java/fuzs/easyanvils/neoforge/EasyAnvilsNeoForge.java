package fuzs.easyanvils.neoforge;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.data.ModBlockTagsProvider;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.puzzleslib.neoforge.api.init.v3.capability.NeoForgeCapabilityHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(EasyAnvils.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EasyAnvilsNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvils::new);
        DataProviderHelper.registerDataProviders(EasyAnvils.MOD_ID, ModBlockTagsProvider::new);
    }

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        NeoForgeCapabilityHelper.registerShulkerBoxLikeBlockEntity(ModRegistry.ANVIL_BLOCK_ENTITY_TYPE.value());
    }
}
