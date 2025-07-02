package fuzs.easyanvils.network.client;

import fuzs.easyanvils.EasyAnvils;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ServerboundPlayMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AnvilMenu;

public record ServerboundRenameItemMessage(String name) implements ServerboundPlayMessage {
    public static final StreamCodec<ByteBuf, ServerboundRenameItemMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
            ServerboundRenameItemMessage::name,
            ServerboundRenameItemMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                if (context.player().containerMenu instanceof AnvilMenu anvilmenu) {
                    if (!anvilmenu.stillValid(context.player())) {
                        EasyAnvils.LOGGER.debug("Player {} interacted with invalid menu {}",
                                context.player(),
                                anvilmenu);
                    } else {
                        anvilmenu.setItemName(ServerboundRenameItemMessage.this.name);
                    }
                }
            }
        };
    }
}
