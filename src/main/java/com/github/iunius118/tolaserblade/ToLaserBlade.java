package com.github.iunius118.tolaserblade;

import com.github.iunius118.tolaserblade.client.ClientEventHandler;
import com.github.iunius118.tolaserblade.item.ItemEventHandler;
import com.github.iunius118.tolaserblade.item.LasarBladeItem;
import com.github.iunius118.tolaserblade.item.LaserBladeItem;
import com.github.iunius118.tolaserblade.item.crafting.*;
import com.github.iunius118.tolaserblade.network.NetworkHandler;
import com.github.iunius118.tolaserblade.network.ServerConfigMessage;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ToLaserBlade.MOD_ID)
public class ToLaserBlade {
    public static final String MOD_ID = "tolaserblade";
    public static final String MOD_NAME = "ToLaserBlade";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final String NAME_ITEM_LASAR_BLADE = "lasar_blade";

    public static final String NAME_ITEM_LASER_BLADE = "laser_blade";
    public static final ModelResourceLocation MRL_ITEM_LASER_BLADE = new ModelResourceLocation(MOD_ID + ":" + NAME_ITEM_LASER_BLADE, "inventory");
    public static final ResourceLocation RL_OBJ_ITEM_LASER_BLADE = new ResourceLocation(MOD_ID, "item/laser_blade.obj");
    public static final ResourceLocation RL_OBJ_ITEM_LASER_BLADE_1 = new ResourceLocation(MOD_ID, "item/laser_blade_1.obj");

    public static final String NAME_ITEM_LASER_BLADE_CORE = "laser_blade_core";

    public static boolean hasShownUpdate = false;

    // Init network channels
    public static final NetworkHandler NETWORK_HANDLER = new NetworkHandler();

    public ToLaserBlade() {
        // Register lifecycle event listeners
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::initServer);
        modEventBus.addListener(this::initClient);
        modEventBus.addListener(this::postInit);

        // Register config handlers
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ToLaserBladeConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ToLaserBladeConfig.clientSpec);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ItemEventHandler());
    }


    public void preInit(final FMLCommonSetupEvent event) {

    }

    private void initServer(final FMLDedicatedServerSetupEvent event) {

    }

    private void initClient(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        OBJLoader.INSTANCE.addDomain(MOD_ID);
    }

    public void postInit(InterModProcessEvent event) {

    }

    @ObjectHolder(MOD_ID)
    public static class Items {
        public static final Item LASAR_BLADE = null;
        public static final Item LASER_BLADE = null;
        public static final Item LASER_BLADE_CORE = null;
    }

    @ObjectHolder(MOD_ID)
    public static class RecipeSerializers {
        public static final IRecipeSerializer CRAFTING_LASER_BLADE_DYEING = null;
        public static final IRecipeSerializer CRAFTING_LASER_BLADE_CLASS_1 = null;
        public static final IRecipeSerializer CRAFTING_LASER_BLADE_CLASS_2 = null;
        public static final IRecipeSerializer CRAFTING_LASER_BLADE_CLASS_3 = null;
        public static final IRecipeSerializer CRAFTING_LASER_BLADE_CUSTOM = null;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            if (FMLLoader.getDist().isClient()) {
                ClientEventHandler.setTEISR();
            }

            event.getRegistry().registerAll(
                    new LasarBladeItem().setRegistryName(NAME_ITEM_LASAR_BLADE),
                    new LaserBladeItem().setRegistryName(NAME_ITEM_LASER_BLADE),
                    new Item((new Item.Properties()).group(ItemGroup.MATERIALS)).setRegistryName(NAME_ITEM_LASER_BLADE_CORE)
            );
        }

        @SubscribeEvent
        public static void onRecipeSerializerRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().registerAll(
                    new LaserBladeDyeingRecipe.Serializer().setRegistryName(LaserBladeDyeingRecipe.Serializer.NAME),
                    new LaserBladeClass1Recipe.Serializer().setRegistryName(LaserBladeClass1Recipe.Serializer.NAME),
                    new LaserBladeClass2Recipe.Serializer().setRegistryName(LaserBladeClass2Recipe.Serializer.NAME),
                    new LaserBladeClass3Recipe.Serializer().setRegistryName(LaserBladeClass3Recipe.Serializer.NAME),
                    new LaserBladeCustomRecipe.Serializer().setRegistryName(LaserBladeCustomRecipe.Serializer.NAME)
            );
        }
    }

    @SubscribeEvent
    public static void remapItems(RegistryEvent.MissingMappings<Item> mappings) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : mappings.getAllMappings()) {
            if (!mapping.key.getNamespace().equals(MOD_ID)) {
                continue;
            }

            String name = mapping.key.getPath();
            if (name.equals(MOD_ID + "." + NAME_ITEM_LASER_BLADE)) {
                // Replace item ID "tolaserblade:tolaserblade.laser_blade" (-1.11.2) with "tolaserblade:laser_blade" (1.12-)
                mapping.remap(ToLaserBlade.Items.LASER_BLADE);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        ToLaserBladeConfig.ServerConfig serverConfig = new ToLaserBladeConfig.ServerConfig();
        serverConfig.isEnabledBlockingWithLaserBladeInServer = ToLaserBladeConfig.COMMON.isEnabledBlockingWithLaserBlade.get();
        serverConfig.laserBladeEfficiencyInServer = ToLaserBladeConfig.COMMON.laserBladeEfficiency.get();

        NETWORK_HANDLER.getConfigChannel().sendTo(
                new ServerConfigMessage(serverConfig),
                ((ServerPlayerEntity) event.getPlayer()).connection.getNetworkManager(),
                NetworkDirection.PLAY_TO_CLIENT);
    }

    @SubscribeEvent
    public void onEntityJoiningInWorld(final EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote && event.getEntity() instanceof PlayerEntity) {
            if (!hasShownUpdate) {
                ClientEventHandler.checkUpdate();
                hasShownUpdate = true;
            }
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        /*
        // For debug
        String str = event.getSource().getDamageType() + " caused " + event.getAmount() + " point damage to " + event.getEntityLiving().getName().getFormattedText() + "!";

        if (FMLLoader.getDist().isClient()) {
            Minecraft.getInstance().ingameGUI.addChatMessage(ChatType.SYSTEM, new StringTextComponent(str));
        } else {
            LOGGER.info(str);
        }
        // */
    }
}
