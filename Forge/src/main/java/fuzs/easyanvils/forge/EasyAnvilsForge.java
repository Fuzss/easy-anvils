package fuzs.easyanvils.forge;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.forge.init.ForgeModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(EasyAnvils.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EasyAnvilsForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ForgeModRegistry.touch();
        ModConstructor.construct(EasyAnvils.MOD_ID, EasyAnvils::new);
    }
}
