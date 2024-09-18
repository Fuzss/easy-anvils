package fuzs.easyanvils.world.level.block.entity;

import fuzs.easyanvils.init.ModRegistry;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.puzzleslib.api.container.v1.ContainerMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AnvilBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final MutableComponent REPAIR_COMPONENT = Component.translatable("container.repair");

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> result = NonNullList.withSize(1, ItemStack.EMPTY);

    public AnvilBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.ANVIL_BLOCK_ENTITY_TYPE.value(), blockPos, blockState);
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(compoundTag, this.items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        ContainerHelper.saveAllItems(compoundTag, this.items, true, registries);
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        ContainerMenuHelper.copyItemsIntoContainer(items, this);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction != Direction.DOWN ? new int[]{0, 1} : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        return false;
    }

    @Override
    protected Component getDefaultName() {
        return REPAIR_COMPONENT;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ModAnvilMenu(id, inventory, this, ContainerLevelAccess.create(this.level, this.worldPosition));
    }

    public NonNullList<ItemStack> getResult() {
        return this.result;
    }
}
