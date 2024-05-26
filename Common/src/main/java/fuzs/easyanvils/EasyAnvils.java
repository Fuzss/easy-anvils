package fuzs.easyanvils;

import fuzs.easyanvils.config.ClientConfig;
import fuzs.easyanvils.config.CommonConfig;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.data.DynamicRecipeProvider;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.handler.ItemInteractionHandler;
import fuzs.easyanvils.handler.NameTagDropHandler;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.network.S2CAnvilRepairMessage;
import fuzs.easyanvils.network.S2COpenNameTagEditorMessage;
import fuzs.easyanvils.network.client.C2SNameTagUpdateMessage;
import fuzs.easyanvils.network.client.C2SRenameItemMessage;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.event.v1.RegistryEntryAddedCallback;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingDropsCallback;
import fuzs.puzzleslib.api.event.v1.entity.player.AnvilEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
import fuzs.puzzleslib.api.network.v2.NetworkHandlerV2;
import fuzs.puzzleslib.api.resources.v1.DynamicPackResources;
import fuzs.puzzleslib.api.resources.v1.PackResourcesHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyAnvils implements ModConstructor {
    public static final String MOD_ID = "easyanvils";
    public static final String MOD_NAME = "Easy Anvils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandlerV2 NETWORK = NetworkHandlerV2.build(MOD_ID, false);
    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).client(ClientConfig.class).common(
            CommonConfig.class).server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerMessages();
        registerHandlers();
    }

    private static void registerMessages() {
        NETWORK.registerClientbound(S2COpenNameTagEditorMessage.class, S2COpenNameTagEditorMessage::new);
        NETWORK.registerServerbound(C2SNameTagUpdateMessage.class, C2SNameTagUpdateMessage::new);
        NETWORK.registerClientbound(S2CAnvilRepairMessage.class, S2CAnvilRepairMessage::new);
        NETWORK.registerServerbound(C2SRenameItemMessage.class, C2SRenameItemMessage::new);
    }

    private static void registerHandlers() {
        PlayerInteractEvents.USE_ITEM.register(ItemInteractionHandler::onUseItem);
        PlayerInteractEvents.USE_BLOCK.register(ItemInteractionHandler::onUseBlock);
        AnvilEvents.USE.register(ItemInteractionHandler::onAnvilUse);
        RegistryEntryAddedCallback.registryEntryAdded(Registries.BLOCK).register(BlockConversionHandler::onRegistryEntryAdded);
        PlayerInteractEvents.USE_BLOCK.register(BlockConversionHandler::onUseBlock);
        TagsUpdatedCallback.EVENT.register(EventPhase.FIRST, BlockConversionHandler::onTagsUpdated);
        LivingDropsCallback.EVENT.register(NameTagDropHandler::onLivingDrops);
    }

    @Override
    public void onCommonSetup() {
        DispenserBlock.registerBehavior(Items.IRON_BLOCK, new OptionalDispenseItemBehavior() {

            @Override
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.anvilRepairing) {
                    return super.execute(source, itemStack);
                } else {
                    Direction direction = source.state().getValue(DispenserBlock.FACING);
                    BlockPos pos = source.pos().relative(direction);
                    Level level = source.level();
                    BlockState state = level.getBlockState(pos);
                    this.setSuccess(true);
                    if (state.is(BlockTags.ANVIL)) {
                        if (ItemInteractionHandler.tryRepairAnvil(level, pos, state)) {
                            itemStack.shrink(1);
                        } else {
                            this.setSuccess(false);
                        }
                        return itemStack;
                    } else {
                        return super.execute(source, itemStack);
                    }
                }
            }
        });
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        if (!CONFIG.get(CommonConfig.class).nameTagCraftingRecipe) return;
        context.addRepositorySource(PackResourcesHelper.buildServerPack(id("name_tag_recipe"), DynamicPackResources.create(
                DynamicRecipeProvider::new), true));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
