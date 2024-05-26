package fuzs.easyanvils.neoforge.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.EasyAnvilsClient;
import fuzs.easyanvils.data.client.ModLanguageProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = EasyAnvils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EasyAnvilsNeoForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvilsClient::new);
        DataProviderHelper.registerDataProviders(EasyAnvils.MOD_ID, ModLanguageProvider::new);
    }
}
