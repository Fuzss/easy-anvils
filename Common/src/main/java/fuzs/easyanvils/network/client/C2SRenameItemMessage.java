package fuzs.easyanvils.network.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.puzzleslib.api.network.v2.WritableMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;

public class C2SRenameItemMessage implements WritableMessage<C2SRenameItemMessage> {
    private final String name;

    public C2SRenameItemMessage(String name) {
        this.name = name;
    }

    public C2SRenameItemMessage(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.name);
    }

    @Override
    public MessageHandler<C2SRenameItemMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(C2SRenameItemMessage message, Player player, Object gameInstance) {
                if (player.containerMenu instanceof AnvilMenu anvilmenu) {
                    if (!anvilmenu.stillValid(player)) {
                        EasyAnvils.LOGGER.debug("Player {} interacted with invalid menu {}", player, anvilmenu);
                        return;
                    }

                    anvilmenu.setItemName(message.name);
                }
            }
        };
    }
}
