package fuzs.easyanvils.network;

import fuzs.easyanvils.client.gui.screens.inventory.NameTagEditScreen;
import fuzs.puzzleslib.api.network.v2.MessageV2;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class S2COpenNameTagEditorMessage implements MessageV2<S2COpenNameTagEditorMessage> {
    private InteractionHand hand;
    private Component title;

    public S2COpenNameTagEditorMessage() {

    }

    public S2COpenNameTagEditorMessage(InteractionHand hand, Component title) {
        this.hand = hand;
        this.title = title;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeComponent(this.title);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);
        this.title = buf.readComponent();
    }

    @Override
    public MessageHandler<S2COpenNameTagEditorMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2COpenNameTagEditorMessage message, Player player, Object gameInstance) {
                ((Minecraft) gameInstance).setScreen(new NameTagEditScreen(message.hand, message.title));
            }
        };
    }
}
