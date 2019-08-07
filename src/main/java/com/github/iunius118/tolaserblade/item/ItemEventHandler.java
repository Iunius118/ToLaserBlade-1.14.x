package com.github.iunius118.tolaserblade.item;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemEventHandler {
    @SubscribeEvent
    public void onEntityInteract(EntityInteract event) {
        // When player interact with entity
        ItemStack itemStack = event.getItemStack();

        if (itemStack.getItem() == ToLaserBlade.Items.LASER_BLADE) {
            // For stopping duplicate of Laser Blade when player interact with Item Frame
            event.setCanceled(true);
            PlayerEntity player = event.getPlayer();
            ItemStack itemStack1 = itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy();

            if (event.getTarget().processInitialInteract(event.getPlayer(), event.getHand())) {
                if (player.abilities.isCreativeMode && itemStack == event.getItemStack() && itemStack.getCount() < itemStack1.getCount()) {
                    itemStack.setCount(itemStack1.getCount());
                }

                event.setCancellationResult(ActionResultType.SUCCESS);
                return;
            }

            event.setCancellationResult(ActionResultType.PASS);
            return;
        }
    }

    @SubscribeEvent
    public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        // When item destroyed by damage
        PlayerEntity player = event.getPlayer();

        if (!player.getEntityWorld().isRemote) {
            ItemStack original = event.getOriginal();

            if (original.getItem() == ToLaserBlade.Items.LASER_BLADE) {
                LaserBlade laserBlade = LaserBlade.create(original);
                ItemStack core = laserBlade.saveTagsToItemStack(new ItemStack(ToLaserBlade.Items.LASER_BLADE_CORE));

                // Drop Core
                ItemEntity itemEntity = new ItemEntity(player.world, player.posX, player.posY + 0.5, player.posZ, core);
                player.world.addEntity(itemEntity);
            }
        }
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        ItemStack stack = event.getPlayer().getHeldItemMainhand();

        if (stack.getItem() instanceof LaserBladeItem) {
            ((LaserBladeItem) stack.getItem()).onCriticalHit(event);
        }
    }

    @SubscribeEvent
    public void onCrafting(ItemCraftedEvent event) {
        ItemStack stackOut = event.getCrafting();

        if (stackOut.getItem() instanceof LaserBladeItem) {
            ((LaserBladeItem) stackOut.getItem()).onCrafting(event);
        }
    }

    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {
        ItemStack left = event.getItemInput();

        if (left.getItem() instanceof LaserBladeItem) {
            ((LaserBladeItem) left.getItem()).onAnvilRepair(event);
            event.setBreakChance(0.075F);
        } else if (left.getItem() == ToLaserBlade.Items.LASER_BLADE_CORE) {
            event.setBreakChance(0.075F);
        }
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();

        if (left.getItem() == ToLaserBlade.Items.LASER_BLADE || left.getItem() == ToLaserBlade.Items.LASER_BLADE_CORE) {
            ((LaserBladeItem) ToLaserBlade.Items.LASER_BLADE).onAnvilUpdate(event);
        }
    }
}
