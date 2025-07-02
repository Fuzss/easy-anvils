package fuzs.easyanvils.services;

import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.world.inventory.state.AnvilMenuState;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.api.core.v1.ServiceProviderHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = ServiceProviderHelper.load(CommonAbstractions.class);

    ModAnvilMenu createAnvilMenu(int id, Inventory inventory, AnvilBlockEntity blockEntity, ContainerLevelAccess containerLevelAccess);

    AnvilMenuState createVanillaAnvilMenu(Inventory inventory, ContainerLevelAccess containerLevelAccess);
}
