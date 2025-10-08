package fuzs.easyanvils.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class AnvilRenderState extends BlockEntityRenderState {
    public List<ItemStackRenderState> items = new ArrayList<>();
    public int itemLightCoords;
    public Direction direction = Direction.NORTH;
}
