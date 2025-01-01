package fuzs.easyanvils.fabric.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.EasyAnvilsClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class EasyAnvilsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvilsClient::new);
    }
}
