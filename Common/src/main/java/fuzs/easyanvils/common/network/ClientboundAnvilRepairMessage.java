package fuzs.easyanvils.common.network;

import fuzs.puzzleslib.common.api.network.v4.codec.ExtraStreamCodecs;
import fuzs.puzzleslib.common.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.common.api.network.v4.message.play.ClientboundPlayMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public record ClientboundAnvilRepairMessage(BlockPos blockPos,
                                            BlockState blockState) implements ClientboundPlayMessage {
    public static final StreamCodec<ByteBuf, ClientboundAnvilRepairMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ClientboundAnvilRepairMessage::blockPos,
            ExtraStreamCodecs.BLOCK_STATE,
            ClientboundAnvilRepairMessage::blockState,
            ClientboundAnvilRepairMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                // play repair sound
                context.level().levelEvent(LevelEvent.SOUND_ANVIL_USED, ClientboundAnvilRepairMessage.this.blockPos, 0);
                // show block breaking particles for anvil without playing breaking sound
                context.level()
                        .addDestroyBlockEffect(ClientboundAnvilRepairMessage.this.blockPos,
                                ClientboundAnvilRepairMessage.this.blockState);
            }
        };
    }
}
