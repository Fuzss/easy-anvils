package fuzs.easyanvils.network;

import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public record ClientboundAnvilRepairMessage(BlockPos blockPos,
                                            BlockState blockState) implements ClientboundPlayMessage {
    @Deprecated(forRemoval = true)
    static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_STREAM_CODEC = ByteBufCodecs.VAR_INT.map(Block::stateById,
            Block::getId);
    public static final StreamCodec<ByteBuf, ClientboundAnvilRepairMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ClientboundAnvilRepairMessage::blockPos,
            BLOCK_STATE_STREAM_CODEC,
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
                context.client().particleEngine.destroy(ClientboundAnvilRepairMessage.this.blockPos,
                        ClientboundAnvilRepairMessage.this.blockState);
            }
        };
    }
}
