package fuzs.easyanvils.forge.world.level.block.entity;

import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

public class ForgeAnvilBlockEntity extends AnvilBlockEntity {
    // use this instead of dedicated single sided one as the other one doesn't check restrictions for inputting into the container
    LazyOptional<? extends IItemHandler> handler = LazyOptional.of(() -> new SidedInvWrapper(this, null));

    public ForgeAnvilBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER) {
            if (facing != Direction.UP) {
                return this.handler.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.handler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.handler = LazyOptional.of(() -> new SidedInvWrapper(this, null));
    }
}
