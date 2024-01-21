package fuzs.easyanvils.client;

import fuzs.easyanvils.client.gui.screens.inventory.ModAnvilScreen;
import fuzs.easyanvils.client.handler.NameTagTooltipHandler;
import fuzs.easyanvils.client.renderer.blockentity.AnvilRenderer;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.event.v1.ItemTooltipCallback;
import fuzs.puzzleslib.api.core.v1.context.ModLifecycleContext;
import net.minecraft.client.gui.screens.MenuScreens;

public class EasyAnvilsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        ItemTooltipCallback.EVENT.register(NameTagTooltipHandler::onItemTooltip);
    }

    @Override
    public void onClientSetup(ModLifecycleContext context) {
        MenuScreens.register(ModRegistry.ANVIL_MENU_TYPE.get(), ModAnvilScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ANVIL_BLOCK_ENTITY_TYPE.get(), AnvilRenderer::new);
    }
}
