package fuzs.easyanvils;

import fuzs.easyanvils.client.util.FormattedStringSplitter;
import fuzs.easyanvils.config.ClientConfig;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.handler.ItemInteractionHandler;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.network.ClientboundAnvilRepairMessage;
import fuzs.easyanvils.network.client.ServerboundRenameItemMessage;
import fuzs.easyanvils.util.FormattedStringUtil;
import fuzs.easyanvils.util.OldFormattedStringDecomposer;
import fuzs.easyanvils.world.level.block.AnvilWithInventoryBlock;
import fuzs.puzzleslib.api.client.event.v1.ClientLifecycleEvents;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.PayloadTypesContext;
import fuzs.puzzleslib.api.event.v1.AddBlockEntityTypeBlocksCallback;
import fuzs.puzzleslib.api.event.v1.RegistryEntryAddedCallback;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
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
            .server(ServerConfig.class);
    public static final Predicate<Block> BLOCK_PREDICATE = (Block block) -> {
        return block instanceof AnvilBlock && !(block instanceof AnvilWithInventoryBlock);
    };

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();

        String s = FormattedStringUtil.deleteLastCharacters("§lText§r", 2);
        String s1 = FormattedStringUtil.deleteLastCharacters("§lText§r", 5);
        String s2 = FormattedStringUtil.deleteLastCharacters("Text", 2);
        int stringLength = FormattedStringUtil.stringLength("§lText§r");
        int stringLength1 = FormattedStringUtil.stringLength("Text");
        int stringLength2 = FormattedStringUtil.stringLength("");
    }

    private static void registerEventHandlers() {
        RegistryEntryAddedCallback.registryEntryAdded(Registries.BLOCK)
                .register(BlockConversionHandler.onRegistryEntryAdded(BLOCK_PREDICATE,
                        AnvilWithInventoryBlock::new,
                        MOD_ID));
        AddBlockEntityTypeBlocksCallback.EVENT.register(BlockConversionHandler.onAddBlockEntityTypeBlocks(ModRegistry.ANVIL_BLOCK_ENTITY_TYPE));
        PlayerInteractEvents.USE_BLOCK.register(BlockConversionHandler.onUseBlock(ModRegistry.UNALTERED_ANVILS_BLOCK_TAG,
                SoundEvents.ANVIL_USE,
                () -> CONFIG.get(ServerConfig.class).convertVanillaAnvilWhenInteracting));
        TagsUpdatedCallback.EVENT.register(EventPhase.FIRST,
                BlockConversionHandler.onTagsUpdated(ModRegistry.UNALTERED_ANVILS_BLOCK_TAG, BLOCK_PREDICATE));
        PlayerInteractEvents.USE_BLOCK.register(ItemInteractionHandler::onUseBlock);
    }

    @Override
    public void onCommonSetup() {
        ClientLifecycleEvents.STARTED.register((minecraft) -> {

            Font font = minecraft.font;
            StringSplitter stringSplitter = font.getSplitter();
            float v1 = FormattedStringSplitter.stringWidth(stringSplitter, "§lTe");
            float v2 = FormattedStringSplitter.stringWidth(stringSplitter, "§lT");
            float v3 = FormattedStringSplitter.stringWidth(stringSplitter, "§l§r§b");
            float v12 = OldFormattedStringDecomposer.stringWidth(font, "§lText§r", 0);
            float v22 = OldFormattedStringDecomposer.stringWidth(font, "§lText§r§b", 0);
            float v32 = OldFormattedStringDecomposer.stringWidth(font, "§l§r§b", 0);
            String v13 = FormattedStringSplitter.plainSubstrByWidth(stringSplitter, "§lText§r", 6, 2);
            String v23 = FormattedStringSplitter.plainSubstrByWidth(stringSplitter, "§lText§r§b", 10, 2);
            String v33 = FormattedStringSplitter.plainSubstrByWidth(stringSplitter, "§l§r§b", 10, 2);
            String v14 = OldFormattedStringDecomposer.plainHeadByWidth(font, "§lText§r", 2, 8, Style.EMPTY);
            String v24 = OldFormattedStringDecomposer.plainHeadByWidth(font, "§lText§r§b", 2, 10, Style.EMPTY);
            String v34 = OldFormattedStringDecomposer.plainHeadByWidth(font, "§l§r§b", 2, 10, Style.EMPTY);
            String substring = FormattedStringUtil.substring("Hello", 0, 3);
            String string = FormattedStringUtil.deleteLastCharacters("Hello", 2);
            String substring2 = FormattedStringUtil.substring("§lHello§r", 0, 3);
            String string2 = FormattedStringUtil.deleteLastCharacters("§lHello§r", 2);
            System.out.println();
        });


        DispenserBlock.registerBehavior(Items.IRON_BLOCK, new OptionalDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.anvilRepairing) {
                    return super.execute(source, itemStack);
                } else {
                    Direction direction = source.state().getValue(DispenserBlock.FACING);
                    BlockPos blockPos = source.pos().relative(direction);
                    Level levelled = source.level();
                    BlockState blockState = levelled.getBlockState(blockPos);
                    this.setSuccess(true);
                    if (blockState.is(BlockTags.ANVIL)) {
                        if (ItemInteractionHandler.tryRepairAnvil(levelled, blockPos, blockState)) {
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
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
