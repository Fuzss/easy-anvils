package fuzs.easyanvils.network;

import fuzs.easyanvils.client.gui.screens.inventory.NameTagEditScreen;
import fuzs.puzzleslib.api.network.v4.codec.ExtraStreamCodecs;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

public record ClientboundOpenNameTagEditorMessage(InteractionHand interactionHand,
                                                  Component title) implements ClientboundPlayMessage {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenNameTagEditorMessage> STREAM_CODEC = StreamCodec.composite(
            ExtraStreamCodecs.fromEnum(InteractionHand.class),
            ClientboundOpenNameTagEditorMessage::interactionHand,
            ComponentSerialization.TRUSTED_STREAM_CODEC,
            ClientboundOpenNameTagEditorMessage::title,
            ClientboundOpenNameTagEditorMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                context.client()
                        .setScreen(new NameTagEditScreen(ClientboundOpenNameTagEditorMessage.this.interactionHand,
                                ClientboundOpenNameTagEditorMessage.this.title));
            }
        };
    }
}
