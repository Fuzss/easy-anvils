package fuzs.easyanvils.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.screens.inventory.ModAnvilScreen;
import fuzs.easyanvils.client.handler.NameTagTooltipHandler;
import fuzs.easyanvils.client.renderer.blockentity.AnvilRenderer;
import fuzs.easyanvils.data.client.DynamicModelProvider;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientAbstractions;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.MenuScreensContext;
import fuzs.puzzleslib.api.client.event.v1.gui.ItemTooltipCallback;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.event.v1.LoadCompleteCallback;
import fuzs.puzzleslib.api.resources.v1.DynamicPackResources;
import fuzs.puzzleslib.api.resources.v1.PackResourcesHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class EasyAnvilsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        ItemTooltipCallback.EVENT.register(NameTagTooltipHandler::onItemTooltip);
        LoadCompleteCallback.EVENT.register(() -> {
            // run a custom implementation here, the appropriate method in client mod constructor runs together with other mods, so we might miss some entries
            for (Map.Entry<Block, Block> entry : BlockConversionHandler.BLOCK_CONVERSIONS.entrySet()) {
                RenderType renderType = ClientAbstractions.INSTANCE.getRenderType(entry.getKey());
                ClientAbstractions.INSTANCE.registerRenderType(entry.getValue(), renderType);
            }
        });
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.ANVIL_MENU_TYPE.value(), ModAnvilScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ANVIL_BLOCK_ENTITY_TYPE.value(), AnvilRenderer::new);
    }

    @Override
    public void onAddResourcePackFinders(PackRepositorySourcesContext context) {
        context.addRepositorySource(PackResourcesHelper.buildClientPack(EasyAnvils.id("default_block_models"), DynamicPackResources.create(DynamicModelProvider::new), true));
    }
}
