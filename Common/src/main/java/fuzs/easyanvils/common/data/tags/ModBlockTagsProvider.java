package fuzs.easyanvils.common.data.tags;

import fuzs.easyanvils.common.init.ModRegistry;
import fuzs.puzzleslib.common.api.data.v3.core.DataProviderContext;
import fuzs.puzzleslib.common.api.data.v3.tags.AbstractTagAppender;
import fuzs.puzzleslib.common.api.data.v3.tags.AbstractTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ModBlockTagsProvider extends AbstractTagsProvider<Block> {
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
