package com.github.iunius118.tolaserblade.item.crafting;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.github.iunius118.tolaserblade.item.LaserBlade;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

//Recipe Laser Blade - Class 2
public class LaserBladeClass2Recipe extends ShapedRecipe {
    public LaserBladeClass2Recipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ToLaserBlade.RecipeSerializers.CRAFTING_LASER_BLADE_CLASS_2;
    }

    // tolaserblade:crafting_laser_blade_class_2
    public static class Serializer extends ShapedRecipe.Serializer {
        public static final ResourceLocation NAME = new ResourceLocation("tolaserblade", "crafting_laser_blade_class_2");

        @Override
        public ShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe recipe = IRecipeSerializer.field_222157_a.read(recipeId, json);    // field_222157_a = ShapedRecipe.Serializer object

            ItemStack output = LaserBlade.create(recipe.getRecipeOutput())
                    .enchant(Enchantments.SMITE, LaserBlade.LVL_SMITE_CLASS_2)
                    .setCraftingTag()
                    .getItemStack();

            return new LaserBladeClass2Recipe(recipe.getId(), recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), output);
        }
    }
}
