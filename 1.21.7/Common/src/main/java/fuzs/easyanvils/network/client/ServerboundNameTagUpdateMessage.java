package fuzs.easyanvils.network.client;

import fuzs.easyanvils.util.ComponentDecomposer;
import fuzs.easyanvils.util.FormattedStringDecomposer;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.puzzleslib.api.network.v4.codec.ExtraStreamCodecs;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ServerboundPlayMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record ServerboundNameTagUpdateMessage(InteractionHand interactionHand,
                                              String name) implements ServerboundPlayMessage {
    public static final StreamCodec<ByteBuf, ServerboundNameTagUpdateMessage> STREAM_CODEC = StreamCodec.composite(
            ExtraStreamCodecs.fromEnum(InteractionHand.class),
            ServerboundNameTagUpdateMessage::interactionHand,
            ByteBufCodecs.STRING_UTF8,
            ServerboundNameTagUpdateMessage::name,
            ServerboundNameTagUpdateMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                ItemStack itemInHand = context.player()
                        .getItemInHand(ServerboundNameTagUpdateMessage.this.interactionHand);
                if (itemInHand.is(Items.NAME_TAG)) {
                    String s = FormattedStringDecomposer.filterText(ServerboundNameTagUpdateMessage.this.name);
                    if (ComponentDecomposer.getStringLength(s) <= 50) {
                        ModAnvilMenu.setFormattedItemName(s.trim(), itemInHand);
                    }
                }
            }
        };
    }
}
