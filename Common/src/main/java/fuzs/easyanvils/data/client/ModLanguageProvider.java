package fuzs.easyanvils.data.client;

import fuzs.easyanvils.client.gui.screens.inventory.NameTagEditScreen;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(NameTagEditScreen.DESCRIPTION_COMPONENT, "Use %s + %s to set a new name without an anvil.");
        builder.add(NameTagEditScreen.EDIT_TRANSLATION_KEY, "Edit %s");
        builder.add(BlockConversionHandler.INVALID_BLOCK_COMPONENT, "Unable to open. Break and replace to use.");
        for (ChatFormatting chatFormatting : ChatFormatting.values()) {
            String translationValue = Stream.of(chatFormatting.getName().split("_"))
                    .map((String s) -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                    .collect(Collectors.joining(" "));
            builder.add("chat.formatting." + chatFormatting.getName(), translationValue);
        }
        builder.add(ModRegistry.UNALTERED_ANVILS_BLOCK_TAG, "Unaltered Anvils");
    }

    @Override
    protected boolean mustHaveTranslationKey(Holder.Reference<?> holder, String translationKey) {
        return !(holder.value() instanceof Block) && super.mustHaveTranslationKey(holder, translationKey);
    }
}
