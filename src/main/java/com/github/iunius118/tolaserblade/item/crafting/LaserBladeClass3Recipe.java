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

//Recipe Laser Blade - Class 3
public class LaserBladeClass3Recipe extends ShapedRecipe {
    public LaserBladeClass3Recipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ToLaserBlade.RecipeSerializers.CRAFTING_LASER_BLADE_CLASS_3;
    }

    // tolaserblade:crafting_laser_blade_class_3
    public static class Serializer extends ShapedRecipe.Serializer {
        public static final ResourceLocation NAME = new ResourceLocation("tolaserblade", "crafting_laser_blade_class_3");

        @Override
        public ShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe recipe = IRecipeSerializer.field_222157_a.read(recipeId, json);    // field_222157_a = ShapedRecipe.Serializer object

            ItemStack output = LaserBlade.create(recipe.getRecipeOutput())
                    .setAttack(LaserBlade.MOD_ATK_CLASS_3)
                    .setSpeed(LaserBlade.MOD_SPD_CLASS_3)
                    .enchant(Enchantments.SMITE, LaserBlade.LVL_SMITE_CLASS_3)
                    .setCraftingTag()
                    .getItemStack();

            return new LaserBladeClass3Recipe(recipe.getId(), recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), output);
        }
    }
}