package fuzs.easyanvils.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fuzs.easyanvils.handler.ItemInteractionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiConsumer;

@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow
    public boolean onlyRenaming;

    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(menuType, containerId, inventory, access, slotDefinition);
    }

    @WrapWithCondition(
            method = "onTake", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"
    )
    )
    protected boolean onTake(ContainerLevelAccess containerLevelAccess, BiConsumer<Level, BlockPos> levelPosConsumer, Player player, ItemStack itemStack) {
        // we cannot inject into the lambda as some required menu context is not available there
        // choose a mixin in favour of NeoForge's AnvilCraftEvent to avoid having to duplicate too much functionality
        ItemInteractionHandler.onTakeAnvilOutputItemStack(containerLevelAccess, player, this.onlyRenaming);
        return false;
    }
}
