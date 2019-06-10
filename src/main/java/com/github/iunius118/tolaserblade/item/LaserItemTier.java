package com.github.iunius118.tolaserblade.item;

import com.github.iunius118.tolaserblade.ToLaserBladeConfig;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class LaserItemTier implements IItemTier {
    @Override
    public int getHarvestLevel() {
        return 3;
    }

    @Override
    public int getMaxUses() {
        return LaserBlade.MAX_USES;
    }

    @Override
    public float getEfficiency() {
        return ToLaserBladeConfig.COMMON.laserBladeEfficiencyInServer.get();
    }

    @Override
    public float getAttackDamage() {
        return 3.0F;
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public Ingredient getRepairMaterial() {
        Tag<Item> tag = ItemTags.getCollection().get(new ResourceLocation("forge", "ingots/iron"));

        if (tag != null) {
            return Ingredient.fromTag(tag);
        } else {
            return Ingredient.fromItems(Items.IRON_INGOT);
        }
    }
}
