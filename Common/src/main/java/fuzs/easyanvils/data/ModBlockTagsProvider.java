package fuzs.easyanvils.data;

import fuzs.easyanvils.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ModBlockTagsProvider extends AbstractTagProvider.Blocks {
    private static final List<String> UNALTERED_ANVILS = List.of("betterend:aeternium_anvil", "betterend:terminite_anvil", "betterend:thallasium_anvil", "betternether:cincinnasite_anvil");

    public ModBlockTagsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        IntrinsicTagAppender<Block> tagAppender = this.tag(ModRegistry.UNALTERED_ANVILS_BLOCK_TAG);
        UNALTERED_ANVILS.stream().map(ResourceLocation::new).forEach(tagAppender::addOptional);
    }
}
