package com.github.iunius118.tolaserblade;

import com.github.iunius118.tolaserblade.client.ClientEventHandler;
import com.github.iunius118.tolaserblade.client.renderer.entity.LaserTrapEntityRenderer;
import com.github.iunius118.tolaserblade.data.*;
import com.github.iunius118.tolaserblade.enchantment.LightElementEnchantment;
import com.github.iunius118.tolaserblade.enchantment.ModEnchantments;
import com.github.iunius118.tolaserblade.entity.LaserTrapEntity;
import com.github.iunius118.tolaserblade.item.*;
import com.github.iunius118.tolaserblade.network.NetworkHandler;
import com.github.iunius118.tolaserblade.network.ServerConfigMessage;
import com.github.iunius118.tolaserblade.util.ModSoundEvents;
import net.minecraft.data.DataGenerator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(ToLaserBlade.MOD_ID)
@EventBusSubscriber
public class ToLaserBlade {
    public static final String MOD_ID = "tolaserblade";
    public static final String MOD_NAME = "ToLaserBlade";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final ModItems ITEMS = new ModItems();
    public static final ModEnchantments ENCHANTMENTS = new ModEnchantments();
    public static final ModSoundEvents SOUND_EVENTS = new ModSoundEvents();

    public static boolean hasShownUpdate = false;

    // Init network channels
    public static final NetworkHandler NETWORK_HANDLER = new NetworkHandler();

    public ToLaserBlade() {
        // Register lifecycle event listeners
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::initClient);
        modEventBus.register(ToLaserBladeConfig.class);

        // Register config handlers
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ToLaserBladeConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ToLaserBladeConfig.clientSpec);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ItemEventHandler());

        // Register client-side mod event handler
        if (FMLLoader.getDist().isClient()) {
            FMLJavaModLoadingContext.get().getModEventBus().register(new ClientEventHandler());
        }
    }

    private void initClient(final FMLClientSetupEvent event) {
        // Register laser trap entity renderer
        RenderingRegistry.registerEntityRenderingHandler(LaserTrapEntity.class, LaserTrapEntityRenderer::new);
    }

    /*
     * Registry Events
     */

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        // Register items
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                    new DXLaserBladeItem().setRegistryName("dx_laser_blade"),
                    new LaserBladeItem().setRegistryName("laser_blade"),
                    new LBBrokenItem().setRegistryName("lb_broken"),
                    new LBBlueprintItem().setRegistryName("lb_blueprint"),
                    new LBDisassembledItem().setRegistryName("lb_disassembled"),
                    new LBBatteryItem().setRegistryName("lb_battery"),
                    new LBMediumItem().setRegistryName("lb_medium"),
                    new LBEmitterItem().setRegistryName("lb_emitter"),
                    new LBCasingItem().setRegistryName("lb_casing")
            );
        }

        // Register Enchantments
        @SubscribeEvent
        public static void onEnchantmentRegistry(final RegistryEvent.Register<Enchantment> event) {
            event.getRegistry().registerAll(
                    new LightElementEnchantment().setRegistryName(LightElementEnchantment.ID)
            );
        }

        @SubscribeEvent
        public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
            EntityType<LaserTrapEntity> laserTrap = EntityType.Builder
                    .<LaserTrapEntity>create(LaserTrapEntity::new, EntityClassification.MISC)
                    .size(1.0F, 1.0F).immuneToFire()
                    .setTrackingRange(64).setUpdateInterval(4).setShouldReceiveVelocityUpdates(false)
                    .build(LaserTrapEntity.ID.toString());

            event.getRegistry().registerAll(
                    laserTrap.setRegistryName(LaserTrapEntity.ID)
            );
        }

        // Register Sound Events
        @SubscribeEvent
        public static void onSoundEventRegistry(final RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                    new SoundEvent(new ResourceLocation(MOD_ID, "item.dx_laser_blade.swing")).setRegistryName("item_dx_laser_blade_swing")
            );
        }

        @SubscribeEvent
        public static void onRecipeSerializerRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event) {

        }

        // Generate data
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            DataGenerator gen = event.getGenerator();

            if (event.includeServer()) {
                gen.addProvider(new TLBRecipeProvider(gen));    // Recipes
                gen.addProvider(new TLBItemTagsProvider(gen));  // Item tags
                gen.addProvider(new TLBAdvancementProvider(gen));   // Advancements
            }

            if (event.includeClient()) {
                ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

                gen.addProvider(new TLBItemModelProvider(gen, existingFileHelper)); // Item models
                TLBLanguageProvider.addProviders(gen);  // Languages
                gen.addProvider(new TLBSoundProvider(gen)); // Sounds
            }
        }
    }

    /*
     * Remapping Items
     */

    @SubscribeEvent
    public static void remapItems(RegistryEvent.MissingMappings<Item> mappings) {
        final Map<ResourceLocation, Item> remappingItemMap = new HashMap<>();
        // Replace item ID "tolaserblade:tolaserblade.laser_blade" (-1.11.2) with "tolaserblade:laser_blade" (1.12-)
        remappingItemMap.put(new ResourceLocation(MOD_ID, "tolaserblade.laser_blade"), ModItems.LASER_BLADE);

        // Replace item ID "tolaserblade:lasar_blade" (-1.14.4 v2.x) with "tolaserblade:dx_laser_blade" (1.14.4 v3-)
        remappingItemMap.put(new ResourceLocation(MOD_ID, "lasar_blade"), ModItems.DX_LASER_BLADE);

        // Replace item ID "tolaserblade:laser_blade_core" (-1.14.4 v2.x) with "tolaserblade:lb_broken" (1.14.4 v3-)
        remappingItemMap.put(new ResourceLocation(MOD_ID, "laser_blade_core"), ModItems.LB_BROKEN);

        // Replace item IDs
        mappings.getAllMappings().stream()
                .filter(mapping -> mapping.key.getNamespace().equals(MOD_ID) && remappingItemMap.containsKey(mapping.key))
                .forEach(mapping -> mapping.remap(remappingItemMap.get(mapping.key)));
    }

    /*
     * World Events
     */

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Send server-side config to logged-in players
        ToLaserBladeConfig.ServerConfig serverConfig = new ToLaserBladeConfig.ServerConfig();

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
