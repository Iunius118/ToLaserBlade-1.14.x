package com.github.iunius118.tolaserblade.client;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.github.iunius118.tolaserblade.ToLaserBlade.Items;
import com.github.iunius118.tolaserblade.ToLaserBladeConfig;
import com.github.iunius118.tolaserblade.client.model.ModelLaserBlade;
import com.github.iunius118.tolaserblade.client.renderer.ItemLaserBladeRenderer;
import com.github.iunius118.tolaserblade.item.ItemLaserBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;

public class ClientEventHandler {
    public static void setTEISR() {
        ItemLaserBlade.properties = ItemLaserBlade.properties.setTEISR(() -> () -> new ItemLaserBladeRenderer());
    }

    public static void checkUpdate() {
        // Check update and Notify client
        CheckResult result = VersionChecker.getResult(ModList.get().getModFileById(ToLaserBlade.MOD_ID).getMods().get(0));
        Status status = result.status;

        if (status == Status.PENDING) {
            // Failed to get update information
            return;
        }

        if (status == Status.OUTDATED || status == Status.BETA_OUTDATED) {
            ITextComponent modNameHighlighted = new TextComponentString(ToLaserBlade.MOD_NAME);
            modNameHighlighted.getStyle().setColor(TextFormatting.YELLOW);

            ITextComponent newVersionHighlighted = new TextComponentString(result.target.toString());
            newVersionHighlighted.getStyle().setColor(TextFormatting.YELLOW);

            ITextComponent message = new TextComponentTranslation("tolaserblade.update.newversion", modNameHighlighted).appendText(": ")
                    .appendSibling(newVersionHighlighted);
            message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result.url));

            Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(message);
        }
    }

    // Model bakery
    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) {
        ModelLaserBlade modelLaserBlade;

        if (ToLaserBladeConfig.CLIENT.laserBladeRenderingMode.get() == 1) {
            // Rendering Mode 1: Disable Blending
            modelLaserBlade = new ModelLaserBlade(bakeModel(ToLaserBlade.RL_OBJ_ITEM_LASER_BLADE_1), event.getModelRegistry().get(ToLaserBlade.MRL_ITEM_LASER_BLADE));
        } else {
            // Rendering Mode 0: Default
            modelLaserBlade = new ModelLaserBlade(bakeModel(ToLaserBlade.RL_OBJ_ITEM_LASER_BLADE), event.getModelRegistry().get(ToLaserBlade.MRL_ITEM_LASER_BLADE));
        }

        event.getModelRegistry().put(ToLaserBlade.MRL_ITEM_LASER_BLADE, modelLaserBlade);
    }

    @SubscribeEvent
    public void onItemColorHandlerEvent(ColorHandlerEvent.Item event) {
        event.getItemColors().register(new ItemLaserBlade.ColorHandler(), Items.LASER_BLADE);
    }

    public IBakedModel bakeModel(ResourceLocation location) {
        try {
            IUnbakedModel model = ModelLoaderRegistry.getModel(location);
            // logger.info("Loaded obj model: " + model.hashCode());  // for debug
            IBakedModel bakedModel = model.bake(ModelLoader.defaultModelGetter(), ModelLoader.defaultTextureGetter(), model.getDefaultState(), false, DefaultVertexFormats.ITEM);
            return bakedModel;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        // Register texture for obj model
        event.getMap().registerSprite(Minecraft.getInstance().getResourceManager(), ToLaserBlade.RL_TEXTURE_ITEM_LASER_BLADE);
    }
}
