package fuzs.easyanvils.network;

import fuzs.puzzleslib.api.network.v2.MessageV2;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public class S2CAnvilRepairMessage implements MessageV2<S2CAnvilRepairMessage> {
    private BlockPos pos;
    private int stateId;

    public S2CAnvilRepairMessage() {

    }

    public S2CAnvilRepairMessage(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.stateId = Block.getId(state);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.stateId);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.stateId = buf.readInt();
    }

    @Override
    public MessageHandler<S2CAnvilRepairMessage> makeHandler() {
        return new MessageHandler<>() {
            @Override
            public void handle(S2CAnvilRepairMessage message, Player player, Object gameInstance) {
                // play repair sound
                player.level.levelEvent(LevelEvent.SOUND_ANVIL_USED, message.pos, 0);
                // show block breaking particles for anvil without playing breaking sound
                ((Minecraft) gameInstance).particleEngine.destroy(message.pos, Block.stateById(message.stateId));
            }
        };
    }
}
