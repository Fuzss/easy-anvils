//package fuzs.easyanvils.integration.apotheosis;
//
//import fuzs.easyanvils.init.ModRegistry;
//import fuzs.easyanvils.world.inventory.ModAnvilMenu;
//import fuzs.easyanvils.world.level.block.entity.ContainerImpl;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.core.NonNullList;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.world.*;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.ContainerLevelAccess;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraftforge.common.capabilities.ForgeCapabilities;
//import net.minecraftforge.common.util.LazyOptional;
//import net.minecraftforge.items.IItemHandler;
//import net.minecraftforge.items.wrapper.SidedInvWrapper;
//import shadows.apotheosis.ench.anvil.AnvilTile;
//
//import javax.annotation.Nullable;
//
//public class ApothAnvilBlockEntity extends AnvilTile implements MenuProvider, Nameable, WorldlyContainer, ContainerImpl {
//    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
//    private LockCode lockKey = LockCode.NO_LOCK;
//    private Component name;
//    // use this instead of dedicated single sided one as the other one doesn't check restrictions for inputting into the container
//    LazyOptional<? extends IItemHandler> handler = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.UP));
//
//    public ApothAnvilBlockEntity(BlockPos pos, BlockState state) {
//        super(pos, state);
//    }
//
//    @Override
//    public BlockEntityType<?> getType() {
//        return ModRegistry.ANVIL_BLOCK_ENTITY_TYPE.get();
//    }
//
//    @Override
//    public void load(CompoundTag tag) {
//        super.load(tag);
//        this.lockKey = LockCode.fromTag(tag);
//        if (tag.contains("CustomName", 8)) {
//            this.name = Component.Serializer.fromJson(tag.getString("CustomName"));
//        }
//        this.inventory.clear();
//        ContainerHelper.loadAllItems(tag, this.inventory);
//    }
//
//    @Override
//    public void saveAdditional(CompoundTag tag) {
//        super.saveAdditional(tag);
//        this.lockKey.addToTag(tag);
//        if (this.name != null) {
//            tag.putString("CustomName", Component.Serializer.toJson(this.name));
//        }
//        ContainerHelper.saveAllItems(tag, this.inventory, true);
//    }
//
//    @Override
//    @Nullable
//    public ClientboundBlockEntityDataPacket getUpdatePacket() {
//        return ClientboundBlockEntityDataPacket.create(this);
//    }
//
//    @Override
//    public CompoundTag getUpdateTag() {
//        return this.saveWithoutMetadata();
//    }
//
//    @Override
//    public void setChanged() {
//        super.setChanged();
//        if (this.level != null) {
//            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
//        }
//    }
//
//    @Override
//    public NonNullList<ItemStack> items() {
//        return this.inventory;
//    }
//
//    @Override
//    public boolean stillValid(Player player) {
//        // anvil might break to weaker version, no reason to close the screen though, therefore instanceof check instead of reference comparison
//        if (this.level != null && !(this.level.getBlockEntity(this.worldPosition) instanceof ApothAnvilBlockEntity)) {
//            return false;
//        } else {
//            return !(player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) > 64.0);
//        }
//    }
//
//    @Override
//    public int[] getSlotsForFace(Direction side) {
//        return side != Direction.DOWN ? new int[]{0, 1} : new int[0];
//    }
//
//    @Override
//    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
//        return this.canPlaceItem(index, itemStackIn);
//    }
//
//    @Override
//    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
//        return false;
//    }
//
//    public void setCustomName(Component name) {
//        this.name = name;
//    }
//
//    @Override
//    public Component getName() {
//        return this.name != null ? this.name : this.getDefaultName();
//    }
//
//    @Override
//    public Component getDisplayName() {
//        return this.getName();
//    }
//
//    @org.jetbrains.annotations.Nullable
//    @Override
//    public Component getCustomName() {
//        return this.name;
//    }
//
//    public boolean canOpen(Player player) {
//        return canUnlock(player, this.lockKey, this.getDisplayName());
//    }
//
//    public static boolean canUnlock(Player player, LockCode code, Component displayName) {
//        if (!player.isSpectator() && !code.unlocksWith(player.getMainHandItem())) {
//            player.displayClientMessage(Component.translatable("container.isLocked", displayName), true);
//            player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    @org.jetbrains.annotations.Nullable
//    @Override
//    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
//        return this.canOpen(player) ? this.createMenu(i, inventory) : null;
//    }
//
//    protected Component getDefaultName() {
//        return Component.translatable("container.repair");
//    }
//
//    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
//        return new ModAnvilMenu(id, playerInventory, this, ContainerLevelAccess.create(this.level, this.worldPosition));
//    }
//
//    @Override
//    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable Direction facing) {
//        if (!this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER) {
//            if (facing != Direction.UP) {
//                return this.handler.cast();
//            }
//        }
//        return super.getCapability(capability, facing);
//    }
//
//    @Override
//    public void invalidateCaps() {
//        super.invalidateCaps();
//        this.handler.invalidate();
//    }
//
//    @Override
//    public void reviveCaps() {
//        super.reviveCaps();
//        this.handler = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.UP));
//    }
//}
