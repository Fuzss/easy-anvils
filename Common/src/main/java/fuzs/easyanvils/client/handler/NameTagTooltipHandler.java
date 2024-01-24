package fuzs.easyanvils.client.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NameTagTooltipHandler {
    public static final String KEY_NAME_TAG_DESCRIPTION = "easyanvils.item.name_tag.description";

    public static void onItemTooltip(ItemStack stack, @Nullable Player player, List<Component> lines, TooltipFlag context) {
        if (!EasyAnvils.CONFIG.getHolder(ServerConfig.class).isAvailable() || !EasyAnvils.CONFIG.get(ServerConfig.class).editNameTagsNoAnvil) {
            return;
        }
        if (stack.is(Items.NAME_TAG)) {
            Component sneakComponent = Component.keybind("key.sneak").withStyle(ChatFormatting.LIGHT_PURPLE);
            Component useComponent = Component.keybind("key.use").withStyle(ChatFormatting.LIGHT_PURPLE);
            Component component = Component.translatable(KEY_NAME_TAG_DESCRIPTION, sneakComponent, useComponent).withStyle(ChatFormatting.GRAY);
            if (context.isAdvanced()) {
                lines.add(lines.size() - (stack.hasTag() ? 2 : 1), component);
            } else {
                lines.add(component);
            }
        }
    }
}
