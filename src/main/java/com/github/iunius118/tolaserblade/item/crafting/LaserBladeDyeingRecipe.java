package com.github.iunius118.tolaserblade.item.crafting;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

// Recipe Laser Blade - dyeing
public class LaserBladeDyeingRecipe extends ShapelessRecipe {
    public LaserBladeDyeingRecipe(ResourceLocation idIn, String groupIn, ItemStack recipeOutputIn, NonNullList<Ingredient> recipeItemsIn) {
        super(idIn, groupIn, recipeOutputIn, recipeItemsIn);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ToLaserBlade.RecipeSerializers.CRAFTING_LASER_BLADE_DYEING;
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        // Force simple matching
        RecipeItemHelper recipeitemhelper = new RecipeItemHelper();
        int i = 0;

        for (int j = 0; j < inv.getHeight(); ++j) {
            for (int k = 0; k < inv.getWidth(); ++k) {
                ItemStack itemstack = inv.getStackInSlot(k + j * inv.getWidth());
                if (!itemstack.isEmpty()) {
                    ++i;
                    recipeitemhelper.accountStack(new ItemStack(itemstack.getItem()));
                }
            }
        }

        return i == getIngredients().size() && recipeitemhelper.canCraft(this, null);
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        for (int i = 0; i < inv.getHeight(); ++i) {
            for (int j = 0; j < inv.getWidth(); ++j) {
                ItemStack itemstack = inv.getStackInSlot(j + i * inv.getWidth());

                if (itemstack.getItem() == ToLaserBlade.Items.LASER_BLADE) {
                    return itemstack.copy();
                }
            }
        }

        return getRecipeOutput().copy();
    }

    @Override
    public boolean isDynamic() {
        // Hide this recipe
        return true;
    }

    // tolaserblade:crafting_laser_blade_dyeing
    public static class Serializer extends ShapelessRecipe.Serializer {
        public static final ResourceLocation NAME = new ResourceLocation("tolaserblade", "crafting_laser_blade_dyeing");

        @Override
        public ShapelessRecipe read(ResourceLocation recipeId, JsonObject json) {
            ShapelessRecipe recipe = IRecipeSerializer.CRAFTING_SHAPELESS.read(recipeId, json);
            return new LaserBladeDyeingRecipe(recipe.getId(), recipe.getGroup(), recipe.getRecipeOutput(), recipe.getIngredients());
        }
    }
}
