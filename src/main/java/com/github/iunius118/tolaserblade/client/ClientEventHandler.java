package com.github.iunius118.tolaserblade.client;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.github.iunius118.tolaserblade.ToLaserBlade.Items;
import com.github.iunius118.tolaserblade.ToLaserBladeConfig;
import com.github.iunius118.tolaserblade.client.model.LaserBladeModel;
import com.github.iunius118.tolaserblade.client.renderer.LaserBladeItemRenderer;
import com.github.iunius118.tolaserblade.item.LaserBladeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;

public class ClientEventHandler {
    public static void setTEISR() {
        LaserBladeItem.properties = LaserBladeItem.properties.setTEISR(() -> () -> new LaserBladeItemRenderer());
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
            ITextComponent modNameHighlighted = new StringTextComponent(ToLaserBlade.MOD_NAME);
            modNameHighlighted.getStyle().setColor(TextFormatting.YELLOW);

            ITextComponent newVersionHighlighted = new StringTextComponent(result.target.toString());
            newVersionHighlighted.getStyle().setColor(TextFormatting.YELLOW);

            ITextComponent message = new TranslationTextComponent("tolaserblade.update.newversion", modNameHighlighted).appendText(": ")
                    .appendSibling(newVersionHighlighted);
            message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result.url));

            Minecraft.getInstance().field_71456_v.getChatGUI().printChatMessage(message);  // field_71456_v = ingameGUI
        }
    }

    // Model bakery
    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) {
        ModelLoader modelLoader = event.getModelLoader();
        LaserBladeModel laserBladeModel;

        if (ToLaserBladeConfig.CLIENT.laserBladeRenderingMode.get() == 1) {
            // Rendering Mode 1: Disable Blending
            laserBladeModel = new LaserBladeModel(bakeModel(modelLoader, ToLaserBlade.RL_OBJ_ITEM_LASER_BLADE_1), event.getModelRegistry().get(ToLaserBlade.MRL_ITEM_LASER_BLADE));
        } else {
            // Rendering Mode 0: Default
            laserBladeModel = new LaserBladeModel(bakeModel(modelLoader, ToLaserBlade.RL_OBJ_ITEM_LASER_BLADE), event.getModelRegistry().get(ToLaserBlade.MRL_ITEM_LASER_BLADE));
        }

        event.getModelRegistry().put(ToLaserBlade.MRL_ITEM_LASER_BLADE, laserBladeModel);
    }

    @SubscribeEvent
    public void onItemColorHandlerEvent(ColorHandlerEvent.Item event) {
        event.getItemColors().register(new LaserBladeItem.ColorHandler(), Items.LASER_BLADE);
    }

    public IBakedModel bakeModel(ModelLoader modelLoader, ResourceLocation location) {
        try {
            IUnbakedModel model = ModelLoaderRegistry.getModelOrMissing(location);
            // logger.info("Loaded obj model: " + model.hashCode());  // for debug
            return model.bake(modelLoader, ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        // Register texture for obj model
        // event.getMap().registerSprite(Minecraft.getInstance().getResourceManager(), ToLaserBlade.RL_TEXTURE_ITEM_LASER_BLADE);
    }
}