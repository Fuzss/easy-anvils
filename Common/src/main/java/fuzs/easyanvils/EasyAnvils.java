package fuzs.easyanvils;

import fuzs.easyanvils.config.ClientConfig;
import fuzs.easyanvils.config.CommonConfig;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.handler.ItemInteractionHandler;
import fuzs.easyanvils.handler.NameTagDropHandler;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.network.ClientboundAnvilRepairMessage;
import fuzs.easyanvils.network.ClientboundOpenNameTagEditorMessage;
import fuzs.easyanvils.network.client.ServerboundNameTagUpdateMessage;
import fuzs.easyanvils.network.client.ServerboundRenameItemMessage;
import fuzs.easyanvils.world.level.block.AnvilWithInventoryBlock;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.PayloadTypesContext;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.AddBlockEntityTypeBlocksCallback;
import fuzs.puzzleslib.api.event.v1.RegistryEntryAddedCallback;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingDropsCallback;
import fuzs.puzzleslib.api.event.v1.entity.player.AnvilEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
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
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class EasyAnvils implements ModConstructor {
    public static final String MOD_ID = "easyanvils";
    public static final String MOD_NAME = "Easy Anvils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID)
            .client(ClientConfig.class)
            .common(CommonConfig.class)
            .server(ServerConfig.class);
    public static final Predicate<Block> BLOCK_PREDICATE = (Block block) -> {
        return block instanceof AnvilBlock && !(block instanceof AnvilWithInventoryBlock);
    };

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        RegistryEntryAddedCallback.registryEntryAdded(Registries.BLOCK)
                .register(BlockConversionHandler.onRegistryEntryAdded(BLOCK_PREDICATE,
                        AnvilWithInventoryBlock::new,
                        MOD_ID));
        AddBlockEntityTypeBlocksCallback.EVENT.register(BlockConversionHandler.onAddBlockEntityTypeBlocks(ModRegistry.ANVIL_BLOCK_ENTITY_TYPE));
        PlayerInteractEvents.USE_BLOCK.register(BlockConversionHandler.onUseBlock(ModRegistry.UNALTERED_ANVILS_BLOCK_TAG,
                () -> CONFIG.get(CommonConfig.class).disableVanillaAnvil));
        TagsUpdatedCallback.EVENT.register(EventPhase.FIRST,
                BlockConversionHandler.onTagsUpdated(ModRegistry.UNALTERED_ANVILS_BLOCK_TAG, BLOCK_PREDICATE));
        PlayerInteractEvents.USE_ITEM.register(ItemInteractionHandler::onUseItem);
        PlayerInteractEvents.USE_BLOCK.register(ItemInteractionHandler::onUseBlock);
        AnvilEvents.USE.register(ItemInteractionHandler::onAnvilUse);
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
    public void onRegisterPayloadTypes(PayloadTypesContext context) {
        context.playToClient(ClientboundAnvilRepairMessage.class, ClientboundAnvilRepairMessage.STREAM_CODEC);
        context.playToServer(ServerboundRenameItemMessage.class, ServerboundRenameItemMessage.STREAM_CODEC);
        context.playToClient(ClientboundOpenNameTagEditorMessage.class,
                ClientboundOpenNameTagEditorMessage.STREAM_CODEC);
        context.playToServer(ServerboundNameTagUpdateMessage.class, ServerboundNameTagUpdateMessage.STREAM_CODEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}
