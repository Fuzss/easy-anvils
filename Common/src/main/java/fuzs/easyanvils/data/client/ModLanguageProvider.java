package fuzs.easyanvils.data.client;

import fuzs.easyanvils.client.gui.screens.inventory.NameTagEditScreen;
import fuzs.easyanvils.client.handler.NameTagTooltipHandler;
import fuzs.easyanvils.handler.BlockConversionHandler;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(NameTagEditScreen.KEY_NAME_TAG_EDIT, "Edit %s");
        builder.add(NameTagTooltipHandler.KEY_NAME_TAG_DESCRIPTION, "Use %s + %s to set a new name without an anvil.");
        builder.add(BlockConversionHandler.INVALID_BLOCK_COMPONENT, "Unable to open. Break and replace to use.");
    }
}
