package com.github.iunius118.tolaserblade.client.renderer;

import com.github.iunius118.tolaserblade.client.model.LaserBladeItemModel;
import com.github.iunius118.tolaserblade.item.ModItems;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class LBBrokenItemRenderer extends LaserBladeItemRenderer  {
    @Override
    public void renderByItem(ItemStack itemStackIn) {
        Minecraft.getInstance().getTextureManager().bindTexture(LASER_BLADE_TEXTURE_LOCATION);
        brightnessX = GLX.lastBrightnessX;
        brightnessY = GLX.lastBrightnessY;
        boolean isEnabledCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        if (!isEnabledCull) {
            GlStateManager.enableCull();
        }

        GlStateManager.pushMatrix();
        BufferBuilder renderer = Tessellator.getInstance().getBuffer();

        int gripColor = ModItems.LASER_BLADE.checkGamingColor(ModItems.LASER_BLADE.getGripColor(itemStackIn));

        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT), gripColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_2), gripColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_NO_TINT), -1);

        renderAsEmittingPart(true);

        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_BRIGHT), -1);

        renderAsEmittingPart(false);

        GlStateManager.popMatrix();

        if (!isEnabledCull) {
            GlStateManager.disableCull();
        }

        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
    }
}
