package fuzs.easyanvils.neoforge.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.EasyAnvilsClient;
import fuzs.easyanvils.data.client.ModLanguageProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = EasyAnvils.MOD_ID, dist = Dist.CLIENT)
public class EasyAnvilsNeoForgeClient {

    public EasyAnvilsNeoForgeClient() {
        ClientModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvilsClient::new);
        DataProviderHelper.registerDataProviders(EasyAnvils.MOD_ID, ModLanguageProvider::new);
    }
}
