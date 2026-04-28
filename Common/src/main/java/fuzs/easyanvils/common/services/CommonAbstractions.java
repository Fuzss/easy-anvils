package fuzs.easyanvils.common.services;

import fuzs.easyanvils.common.world.inventory.ModAnvilMenu;
import fuzs.easyanvils.common.world.inventory.state.AnvilMenuState;
import fuzs.easyanvils.common.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.common.api.core.v1.ServiceProviderHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = ServiceProviderHelper.load(CommonAbstractions.class);

    ModAnvilMenu createAnvilMenu(int id, Inventory inventory, AnvilBlockEntity blockEntity, ContainerLevelAccess containerLevelAccess);

    AnvilMenuState createVanillaAnvilMenu(Inventory inventory, ContainerLevelAccess containerLevelAccess);
}
