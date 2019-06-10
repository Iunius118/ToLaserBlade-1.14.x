package com.github.iunius118.tolaserblade.item.crafting;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.github.iunius118.tolaserblade.item.LaserBlade;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

//Recipe Laser Blade - Custom
public class LaserBladeCustomRecipe extends ShapedRecipe {
    public LaserBladeCustomRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ToLaserBlade.RecipeSerializers.CRAFTING_LASER_BLADE_CUSTOM;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        LaserBlade result = LaserBlade.create(getRecipeOutput().copy());

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);

            if (result.changeColorByItem(stack).getCost() > 0) {
                return result.getItemStack();
            }
        }

        return result.getItemStack();
    }

    // tolaserblade:crafting_laser_blade_custom
    public static class Serializer extends ShapedRecipe.Serializer {
        public static final ResourceLocation NAME = new ResourceLocation("tolaserblade", "crafting_laser_blade_custom");

        @Override
        public ShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe recipe = IRecipeSerializer.field_222157_a.read(recipeId, json);    // field_222157_a = ShapedRecipe.Serializer object
            ItemStack itemstack = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "result"), true);

            ItemStack output = LaserBlade.create(itemstack)
                    .setCraftingTag()
                    .getItemStack();

            return new LaserBladeCustomRecipe(recipe.getId(), recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), output);
        }
    }
}
