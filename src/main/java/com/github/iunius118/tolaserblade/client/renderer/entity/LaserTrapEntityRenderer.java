package com.github.iunius118.tolaserblade.client.renderer.entity;

import com.github.iunius118.tolaserblade.entity.LaserTrapEntity;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class LaserTrapEntityRenderer extends EntityRenderer<LaserTrapEntity> {
    public LaserTrapEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(LaserTrapEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();

        Vec3d look = entity.getLookVec();
        Direction.Axis axis = Direction.getFacingFromVector((float)look.x, (float)look.y, (float)look.z).getAxis();
        AxisAlignedBB laserBoundingBox = entity.getBoundingBox();

        if (axis == Direction.Axis.Y) {
            laserBoundingBox = laserBoundingBox.grow(-0.4375D, 0.0D, -0.4375D);
        } else if (axis == Direction.Axis.X) {
            laserBoundingBox = laserBoundingBox.grow(0.0D, -0.4375D, -0.4375D);
        } else {
            laserBoundingBox = laserBoundingBox.grow(-0.4375D, -0.4375D, 0.0D);
        }

        renderLaserTrap(laserBoundingBox, x - entity.lastTickPosX, y - entity.lastTickPosY, z - entity.lastTickPosZ, entity.getColor());

        GlStateManager.popMatrix();
    }

    private void renderLaserTrap(AxisAlignedBB boundingBox, double x, double y, double z, int color) {
        float lastBrightnessX = GLX.lastBrightnessX;
        float lastBrightnessY = GLX.lastBrightnessY;

        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240.0F, 240.0F);
        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        setColor(color);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.setTranslation(x, y, z);

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_NORMAL);

        // Down
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        // Up
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        // North
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        // South
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        // West
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        // East
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();

        tessellator.draw();

        bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);

        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, lastBrightnessX, lastBrightnessY);
        GL11.glPopAttrib();
        GlStateManager.enableTexture();
        GlStateManager.enableLighting();
    }

    private void setColor(int color) {
        float b = (float)(color & 0xFF) / 0xFF;
        float g = (float)((color >>> 8) & 0xFF) / 0xFF;
        float r = (float)((color >>> 16) & 0xFF) / 0xFF;
        float a = (float)((color >>> 24) & 0xFF) / 0xFF;
        GlStateManager.color4f(r, g, b, a);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(LaserTrapEntity entity) {
        return null;
    }
}
