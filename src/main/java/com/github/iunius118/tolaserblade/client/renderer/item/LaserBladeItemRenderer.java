package com.github.iunius118.tolaserblade.client.renderer.item;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.github.iunius118.tolaserblade.ToLaserBladeConfig;
import com.github.iunius118.tolaserblade.client.model.LaserBladeItemModel;
import com.github.iunius118.tolaserblade.item.ModItems;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class LaserBladeItemRenderer extends ItemStackTileEntityRenderer {
    public static final ResourceLocation LASER_BLADE_TEXTURE_LOCATION = new ResourceLocation(ToLaserBlade.MOD_ID, "textures/item/laser_blade.png");

    public float brightnessX;
    public float brightnessY;

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

        switch(ToLaserBladeConfig.CLIENT.laserBladeRenderingMode.get()) {
            case 1:
                renderLaserBladeMode1(itemStackIn, renderer);
                break;

            case 2:
                renderLaserBladeMode2(itemStackIn, renderer);
                break;

            default:
                renderLaserBladeMode0(itemStackIn, renderer);
        }

        GlStateManager.popMatrix();

        if (!isEnabledCull) {
            GlStateManager.disableCull();
        }

        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
    }

    private void renderLaserBladeMode0(ItemStack itemStack, BufferBuilder renderer) {
        int gripColor = ModItems.LASER_BLADE.checkGamingColor(ModItems.LASER_BLADE.getGripColor(itemStack));

        Pair<Integer, Boolean> bladeColor = ModItems.LASER_BLADE.getBladeInnerColor(itemStack);
        int innerColor = ModItems.LASER_BLADE.checkGamingColor(bladeColor.getLeft());
        boolean isInnerSubColor = bladeColor.getRight();

        bladeColor = ModItems.LASER_BLADE.getBladeOuterColor(itemStack);
        int outerColor = ModItems.LASER_BLADE.checkGamingColor(bladeColor.getLeft());
        boolean isOuterSubColor = bladeColor.getRight();

        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT), gripColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_0_2), gripColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_NO_TINT), -1);

        renderAsEmittingPart(true);

        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_BRIGHT), -1);

        // Enable additive blending
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        if (!isInnerSubColor) GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        else GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.BLADE_IN), innerColor);

        if (!isOuterSubColor) GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        else GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.BLADE_MID_0), outerColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.BLADE_OUT_0), outerColor);

        // Disable Add-color
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);

        renderAsEmittingPart(false);
    }

    private void renderLaserBladeMode1(ItemStack itemStack, BufferBuilder renderer) {
        int gripColor = ModItems.LASER_BLADE.checkGamingColor(ModItems.LASER_BLADE.getGripColor(itemStack));

        Pair<Integer, Boolean> bladeColor = ModItems.LASER_BLADE.getBladeOuterColor(itemStack);
        int outerColor = ModItems.LASER_BLADE.checkGamingColor(bladeColor.getLeft());
        outerColor = (bladeColor.getRight() ? ~outerColor : outerColor) | 0xFF000000;

        bladeColor = ModItems.LASER_BLADE.getBladeInnerColor(itemStack);
        int innerColor = ModItems.LASER_BLADE.checkGamingColor(bladeColor.getLeft());
        innerColor = (bladeColor.getRight() ? ~innerColor : innerColor) | 0xFF000000;

        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT), gripColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_NO_TINT), -1);

        renderAsEmittingPart(true);

        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_BRIGHT), -1);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.BLADE_OUT_1), outerColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.BLADE_IN_1), innerColor);

        renderAsEmittingPart(false);
    }

    private void renderLaserBladeMode2(ItemStack itemStack, BufferBuilder renderer) {
        int gripColor = ModItems.LASER_BLADE.checkGamingColor(ModItems.LASER_BLADE.getGripColor(itemStack));

        Pair<Integer, Boolean> bladeColor = ModItems.LASER_BLADE.getBladeInnerColor(itemStack);
        int innerColor = ModItems.LASER_BLADE.checkGamingColor(bladeColor.getLeft());
        boolean isInnerSubColor = bladeColor.getRight();

        bladeColor = ModItems.LASER_BLADE.getBladeOuterColor(itemStack);
        int outerColor = ModItems.LASER_BLADE.checkGamingColor(bladeColor.getLeft());
        boolean isOuterSubColor = bladeColor.getRight();

        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT), gripColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_0_2), gripColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_NO_TINT), -1);

        renderAsEmittingPart(true);

        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.HILT_BRIGHT), -1);

        // Enable additive blending
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        if (!isOuterSubColor) GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        else GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.BLADE_OUT_2), outerColor);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.BLADE_MID_2), outerColor);

        if (!isInnerSubColor) GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        else GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
        renderQuads(renderer, getBakedQuads(LaserBladeItemModel.Part.BLADE_IN), innerColor);

        // Disable Add-color
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);

        renderAsEmittingPart(false);
    }

    public void renderAsEmittingPart(boolean flag) {
        if (flag) {
            // Enable bright rendering
            GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
            RenderHelper.disableStandardItemLighting();
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240.0F, 240.0F);
        } else {
            // Disable bright rendering
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, brightnessX, brightnessY);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopAttrib();

        }
    }

    public List<BakedQuad> getBakedQuads(LaserBladeItemModel.Part part) {
        return LaserBladeItemModel.parts.getOrDefault(part, Collections.emptyList());
    }

    public void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, int color) {
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

        // Render Quads
        for (BakedQuad quad : quads) {
            LightUtil.renderQuadColor(renderer, quad, color);
            Vec3i vec3i = quad.getFace().getDirectionVec();
            renderer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
        }

        Tessellator.getInstance().draw();
    }
}
