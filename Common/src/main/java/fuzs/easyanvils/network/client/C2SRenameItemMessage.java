package fuzs.easyanvils.network.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.puzzleslib.api.network.v2.MessageV2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;

public class C2SRenameItemMessage implements MessageV2<C2SRenameItemMessage> {
    private String name;

    public C2SRenameItemMessage() {

    }

    public C2SRenameItemMessage(String name) {
        this.name = name;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.name);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
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
