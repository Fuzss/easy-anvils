package fuzs.easyanvils.data;

import fuzs.puzzleslib.api.data.v1.AbstractLanguageProvider;
import net.minecraft.data.PackOutput;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(PackOutput packOutput, String modId) {
        super(packOutput, modId);
    }

    @Override
    protected void addTranslations() {
        this.add("easyanvils.name_tag.edit", "Edit %s");
        this.add("easyanvils.item.name_tag.description", "Use %s + %s to set a new name without an anvil.");
    }
}
