package fuzs.easyanvils.data;

import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagAppender;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ModBlockTagsProvider extends AbstractTagProvider<Block> {
    private static final List<String> UNALTERED_ANVILS = List.of("betterend:aeternium_anvil",
            "betterend:terminite_anvil",
            "betterend:thallasium_anvil",
            "betternether:cincinnasite_anvil");

    public ModBlockTagsProvider(DataProviderContext context) {
        super(Registries.BLOCK, context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        AbstractTagAppender<Block> tagAppender = this.tag(ModRegistry.UNALTERED_ANVILS_BLOCK_TAG);
        UNALTERED_ANVILS.forEach(tagAppender::addOptional);
    }
}
