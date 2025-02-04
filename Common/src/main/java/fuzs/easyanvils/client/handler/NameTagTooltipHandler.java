package fuzs.easyanvils.client.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ClientConfig;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.puzzleslib.api.core.v1.Proxy;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NameTagTooltipHandler {
    public static final String KEY_NAME_TAG_DESCRIPTION = "easyanvils.item.name_tag.description";

    public static void onItemTooltip(ItemStack itemStack, List<Component> lines, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
        if (!EasyAnvils.CONFIG.get(ClientConfig.class).nameTagTooltip) return;
        if (!EasyAnvils.CONFIG.getHolder(ServerConfig.class).isAvailable() ||
                !EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.editNameTagsNoAnvil) {
            return;
        }
        if (itemStack.is(Items.NAME_TAG)) {
            Component sneakComponent = Component.keybind("key.sneak").withStyle(ChatFormatting.LIGHT_PURPLE);
            Component useComponent = Component.keybind("key.use").withStyle(ChatFormatting.LIGHT_PURPLE);
            Component component = Component.translatable(KEY_NAME_TAG_DESCRIPTION, sneakComponent, useComponent)
                    .withStyle(ChatFormatting.GRAY);
            List<Component> components = Proxy.INSTANCE.splitTooltipLines(component);
            if (tooltipFlag.isAdvanced()) {
                lines.addAll(lines.size() - (!itemStack.getComponents().isEmpty() ? 2 : 1), components);
            } else {
                lines.addAll(components);
            }
        }
    }
}
