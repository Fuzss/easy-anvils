package fuzs.easyanvils.handler;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;

public class NameTagDropHandler {

    public static EventResult onLivingDrops(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.nameTagsDropFromMobs) return EventResult.PASS;
        if (!(entity instanceof Player) && entity.hasCustomName()) {
            ItemStack itemStack = new ItemStack(Items.NAME_TAG);
            itemStack.setHoverName(entity.getCustomName());
            ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), itemStack);
            itemEntity.setDefaultPickUpDelay();
            drops.add(itemEntity);
        }

        return EventResult.PASS;
    }
}
