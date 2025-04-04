package fuzs.easyanvils.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.screens.inventory.ModAnvilScreen;
import fuzs.easyanvils.client.gui.screens.inventory.NameTagEditScreen;
import fuzs.easyanvils.client.handler.BlockStateTranslator;
import fuzs.easyanvils.client.renderer.blockentity.AnvilRenderer;
import fuzs.easyanvils.config.ClientConfig;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.BlockStateResolverContext;
import fuzs.puzzleslib.api.client.core.v1.context.MenuScreensContext;
import fuzs.puzzleslib.api.client.event.v1.ClientLifecycleEvents;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.ItemTooltipRegistry;
import fuzs.puzzleslib.api.client.renderer.v1.RenderTypeHelper;
import fuzs.puzzleslib.api.client.renderer.v1.model.ModelLoadingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EasyAnvilsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientLifecycleEvents.STARTED.register((Minecraft minecraft) -> {
            // run a custom implementation here, the appropriate method in client mod constructor runs together with other mods, so we might miss some entries
            for (Map.Entry<Block, Block> entry : BlockConversionHandler.getBlockConversions().entrySet()) {
                RenderType renderType = RenderTypeHelper.getRenderType(entry.getKey());
                RenderTypeHelper.registerRenderType(entry.getValue(), renderType);
            }
        });
    }

    @Override
    public void onClientSetup() {
        ItemTooltipRegistry.registerItemTooltip(Items.NAME_TAG,
                (Item item, ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, Consumer<Component> tooltipLineConsumer) -> {
                    if (!EasyAnvils.CONFIG.get(ClientConfig.class).nameTagTooltip) return;
                    if (!EasyAnvils.CONFIG.getHolder(ServerConfig.class).isAvailable() ||
                            !EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.editNameTagsNoAnvil) {
                        return;
                    }
                    tooltipLineConsumer.accept(NameTagEditScreen.DESCRIPTION_COMPONENT);
                });
    }

    @Override
    public void onRegisterBlockStateResolver(BlockStateResolverContext context) {
        BlockConversionHandler.getBlockConversions().forEach((Block oldBlock, Block newBlock) -> {
            context.registerBlockStateResolver(newBlock,
                    (ResourceManager resourceManager, Executor executor) -> {
                        return ModelLoadingHelper.loadBlockState(resourceManager, oldBlock, executor);
                    },
                    (BlockStateModelLoader.LoadedModels loadedModels, BiConsumer<BlockState, BlockStateModel.UnbakedRoot> blockStateConsumer) -> {
                        Map<BlockState, BlockState> blockStates = BlockStateTranslator.INSTANCE.convertAllBlockStates(
                                newBlock,
                                oldBlock);
                        for (BlockState blockState : newBlock.getStateDefinition().getPossibleStates()) {
                            BlockStateModel.UnbakedRoot model = loadedModels.models().get(blockStates.get(blockState));
                            if (model != null) {
                                blockStateConsumer.accept(blockState, model);
                            } else {
                                EasyAnvils.LOGGER.warn("Missing model for variant: '{}'", blockState);
                                blockStateConsumer.accept(blockState, ModelLoadingHelper.missingModel());
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
