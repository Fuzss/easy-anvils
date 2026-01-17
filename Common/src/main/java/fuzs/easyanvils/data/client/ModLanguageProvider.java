package fuzs.easyanvils.data.client;

import fuzs.easyanvils.client.gui.components.FormattingGuideWidget;
import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(FormattingGuideWidget.CHAT_FORMATTING_FORMAT_KEY, "§%s - %s");
        for (ChatFormatting chatFormatting : ChatFormatting.values()) {
            String chatFormattingName = FormattingGuideWidget.getChatFormattingName(chatFormatting);
            builder.add(FormattingGuideWidget.getChatFormattingKey(chatFormatting), chatFormattingName);
        }

        builder.add(ModRegistry.UNALTERED_ANVILS_BLOCK_TAG, "Unaltered Anvils");
    }

    @Override
    protected boolean mustHaveTranslationKey(Holder.Reference<?> holder, String translationKey) {
        return !(holder.value() instanceof Block) && super.mustHaveTranslationKey(holder, translationKey);
    }
}
