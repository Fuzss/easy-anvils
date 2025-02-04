package fuzs.easyanvils.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.screens.inventory.ModAnvilScreen;
import fuzs.easyanvils.client.handler.BlockStateTranslator;
import fuzs.easyanvils.client.handler.NameTagTooltipHandler;
import fuzs.easyanvils.client.renderer.blockentity.AnvilRenderer;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientAbstractions;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.BlockStateResolverContext;
import fuzs.puzzleslib.api.client.core.v1.context.MenuScreensContext;
import fuzs.puzzleslib.api.client.event.v1.ClientStartedCallback;
import fuzs.puzzleslib.api.client.event.v1.gui.ItemTooltipCallback;
import fuzs.puzzleslib.api.client.util.v1.ModelLoadingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class EasyAnvilsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientStartedCallback.EVENT.register((Minecraft minecraft) -> {
            // run a custom implementation here, the appropriate method in client mod constructor runs together with other mods, so we might miss some entries
            for (Map.Entry<Block, Block> entry : BlockConversionHandler.getBlockConversions().entrySet()) {
                RenderType renderType = ClientAbstractions.INSTANCE.getRenderType(entry.getKey());
                ClientAbstractions.INSTANCE.registerRenderType(entry.getValue(), renderType);
            }
        });
        ItemTooltipCallback.EVENT.register(NameTagTooltipHandler::onItemTooltip);
    }

    @Override
    public void onRegisterBlockStateResolver(BlockStateResolverContext context) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        BlockStateTranslator blockStateTranslator = new BlockStateTranslator();
        BlockConversionHandler.getBlockConversions().forEach((Block oldBlock, Block newBlock) -> {
            context.registerBlockStateResolver(newBlock, consumer -> {
                BlockStateModelLoader.LoadedModels loadedModels = ModelLoadingHelper.loadBlockState(resourceManager,
                        oldBlock);
                Map<ModelResourceLocation, ModelResourceLocation> modelResourceLocations = blockStateTranslator.convertAllBlockStates(
                        newBlock,
                        oldBlock);
                for (BlockState blockState : newBlock.getStateDefinition().getPossibleStates()) {
                    ModelResourceLocation newModelResourceLocation = BlockModelShaper.stateToModelLocation(blockState);
                    ModelResourceLocation oldModelResourceLocation = modelResourceLocations.get(newModelResourceLocation);
                    UnbakedBlockStateModel model = null;
                    if (oldModelResourceLocation != null) {
                        BlockStateModelLoader.LoadedModel loadedModel = loadedModels.models()
                                .get(oldModelResourceLocation);
                        if (loadedModel != null) {
                            model = loadedModel.model();
                        }
                    }
                    if (model != null) {
                        consumer.accept(blockState, model);
                    } else {
                        EasyAnvils.LOGGER.warn("Missing model for variant: '{}'", newModelResourceLocation);
                        consumer.accept(blockState, ModelLoadingHelper.missingModel());
                    }
                }
            });
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
}
