package fuzs.easyanvils.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class EasyAnvilsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvilsClient::new);
    }
}
