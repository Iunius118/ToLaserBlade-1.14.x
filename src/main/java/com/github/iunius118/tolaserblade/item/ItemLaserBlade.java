package com.github.iunius118.tolaserblade.item;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.github.iunius118.tolaserblade.ToLaserBladeConfig;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAbstractSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

import javax.annotation.Nullable;

public class ItemLaserBlade extends ItemSword {
    private final IItemTier tier;
    private final float attackDamage;
    private final float attackSpeed;
    public static Item.Properties properties = (new Item.Properties()).setNoRepair().group(ItemGroup.TOOLS);

    private static final IItemPropertyGetter BLOCKING_GETTER = (stack, world, entity) -> {
        return entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F;
    };

    public ItemLaserBlade() {
        super(new ItemTierLaser(), 3, -1.2F, properties);

        tier = getTier();
        attackDamage = 3.0F + tier.getAttackDamage();
        attackSpeed = -1.2F;

        addPropertyOverride(new ResourceLocation("blocking"), BLOCKING_GETTER);
    }

    /* Handle item events */

    public void onCriticalHit(CriticalHitEvent event) {
        Entity target = event.getTarget();
        LaserBlade laserBlade = LaserBlade.create(event.getEntityPlayer().getHeldItemMainhand());

        if (event.isVanillaCritical()) {
            if (target instanceof EntityWither || laserBlade.getAttack() > LaserBlade.MOD_ATK_CLASS_4) {
                event.setDamageModifier(LaserBlade.MOD_CRITICAL_VS_WITHER);
            }
        }
    }

    public void onCrafting(ItemCraftedEvent event) {
        if (event.getPlayer().world.isRemote) {
            return;
        }

        ItemStack stackOut = event.getCrafting();
        LaserBlade laserBlade = LaserBlade.create(stackOut);

        if (laserBlade.hasCraftingTag()) {
            // Crafting on crafting table
            laserBlade.removeCraftingTag();
            return;
        }

        laserBlade.changeColorsByBiome(event.getPlayer()).getItemStack();
    }

    public void onAnvilRepair(AnvilRepairEvent event) {
        ItemStack right = event.getIngredientInput();

        if (right.isEmpty()) {
            ItemStack output = event.getItemResult();
            String name = output.getDisplayName().getString();

            // Use GIFT code
            if ("GIFT".equals(name) || /* "おたから" */ "\u304A\u305F\u304B\u3089".equals(name)) {
                LaserBlade.create(output).setAttackIfLess(LaserBlade.MOD_ATK_CLASS_2).enchantIfLessLevel(Enchantments.SMITE, 2).getItemStack();
                output.clearCustomName();
            }
        } else if (right.getItem() == Items.IRON_AXE) {
            // EXTRACT CORE
            // Consume Iron Axe damage
            ItemStack ironAxe = right.copy();
            EntityPlayer player = event.getEntityPlayer();
            ironAxe.damageItem(10, player);
            player.addItemStackToInventory(ironAxe);
        }
    }

    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        Item itemRight = right.getItem();
        String name = event.getName();
        LaserBlade laserBlade = LaserBlade.create(left.copy());

        if (itemRight == this || itemRight == ToLaserBlade.Items.LASER_BLADE_CORE || itemRight == Items.ENCHANTED_BOOK) {
            // MIX LASER BLADES
            LaserBlade rightLaserBlade = LaserBlade.create(right.copy());
            laserBlade.mixLaserBlade(rightLaserBlade);
            laserBlade.changeDisplayName(name);
            int costLevel = laserBlade.getCost();

            if (costLevel > 0) {
                event.setCost(costLevel);
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            } else {
                event.setCanceled(true);
            }

        } else if (isContainedInItemTag(itemRight, "forge:dusts/glowstone")) {
            // UPGRADE TO CLASS 2
            if ((laserBlade.getAttack() < LaserBlade.MOD_ATK_CLASS_2) || (laserBlade.getEnchantmentLavel(Enchantments.SMITE) < LaserBlade.LVL_SMITE_CLASS_2)) {
                laserBlade.setAttackIfLess(LaserBlade.MOD_ATK_CLASS_2);
                laserBlade.enchantIfLessLevel(Enchantments.SMITE, LaserBlade.LVL_SMITE_CLASS_2);
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost() + LaserBlade.COST_LVL_CLASS_2);
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;

        } else if (isContainedInItemTag(itemRight, "forge:storage_blocks/diamond")) {
            // ATTACK++
            float attack = laserBlade.getAttack();

            if (attack > LaserBlade.MOD_ATK_CLASS_1 && attack < LaserBlade.MOD_ATK_CLASS_3) {
                laserBlade.increaseAttack();
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost());
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;

        } else if (isContainedInItemTag(itemRight, "forge:storage_blocks/redstone")) {
            // SPEED++
            if (laserBlade.getSpeed() < LaserBlade.MOD_SPD_CLASS_3) {
                laserBlade.increaseSpeed();
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost());
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;

        } else if (getBlockFromItem(itemRight) == Blocks.GLOWSTONE) {
            // SMITE++
            int smite = laserBlade.getEnchantmentLavel(Enchantments.SMITE);

            if (smite > LaserBlade.LVL_SMITE_CLASS_1 && smite < Enchantments.SMITE.getMaxLevel()) {
                laserBlade.increaseEnchantmentLevel(Enchantments.SMITE);
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost());
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;

        } else if (isContainedInItemTag(itemRight, "forge:rods/blaze")) {
            // FIRE_ASPECT++
            if (!laserBlade.isEnchantmentMaxLavel(Enchantments.FIRE_ASPECT)) {
                laserBlade.increaseEnchantmentLevel(Enchantments.FIRE_ASPECT);
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost());
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;

        } else if (itemRight == Items.ENDER_EYE) {
            // SWEEPING++
            if (!laserBlade.isEnchantmentMaxLavel(Enchantments.SWEEPING)) {
                laserBlade.increaseEnchantmentLevel(Enchantments.SWEEPING);
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost());
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;

        } else if (itemRight == Items.NAUTILUS_SHELL) {
            // LOOTING++
            if (!laserBlade.isEnchantmentMaxLavel(Enchantments.LOOTING)) {
                laserBlade.increaseEnchantmentLevel(Enchantments.LOOTING);
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost());
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;

        } else if (isContainedInItemTag(itemRight, "forge:ingots/iron")) {
            // REPAIR
            if (left.getItem() == ToLaserBlade.Items.LASER_BLADE_CORE && right.getCount() >= 4) {
                // From Core
                ItemStack output = laserBlade.saveTagsToLaserBlade(new ItemStack(ToLaserBlade.Items.LASER_BLADE));
                event.setCost(laserBlade.getCost() + 4);
                event.setMaterialCost(4);
                event.setOutput(output);

            } else {
                int costItem = laserBlade.repairByMaterial(right.getCount());

                if (costItem > 0) {
                    laserBlade.changeDisplayName(name);
                    event.setCost(laserBlade.getCost());
                    event.setMaterialCost(costItem);
                    event.setOutput(laserBlade.getItemStack());
                }
            }

            return;

        } else if (itemRight == Items.NETHER_STAR) {
            // UPGRADE TO CLASS 4
            laserBlade.upgradeWithNetherStar();

            if (laserBlade.getCost() >= LaserBlade.COST_LVL_CLASS_4) {
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost());
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;

        } else if (getBlockFromItem(itemRight) instanceof BlockAbstractSkull) {
            // MORE ATTACK++
            if (laserBlade.getAttack() < LaserBlade.MOD_ATK_CLASS_3) {
                return;
            } else if (itemRight == Items.DRAGON_HEAD && laserBlade.getAttack() <= LaserBlade.MOD_ATK_CLASS_4) {
                // 15 ATTACK DAMAGE
                laserBlade.setAttackIfLess(LaserBlade.MOD_ATK_CLASS_4 + 1.0F);
                laserBlade.changeDisplayName(name);
                event.setCost(LaserBlade.COST_LVL_CLASS_4);
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());

                return;

            } else if (laserBlade.getAttack() > LaserBlade.MOD_ATK_CLASS_4 && laserBlade.getAttack() < LaserBlade.MOD_ATK_MAX) {
                // ATTACK++
                laserBlade.changeDisplayName(name);
                int costNaming = laserBlade.getCost();

                if (getSkullOwnerName(right).equals("Iunius")) {
                    // For debug
                    laserBlade.setAttackIfLess(LaserBlade.MOD_ATK_MAX - 1.0F);
                } else {
                    laserBlade.increaseAttack();
                }

                laserBlade.changeDisplayName(name);
                event.setCost((int) laserBlade.getAttack() / 100 + 40 + costNaming);    // Not delete, but too expensive for survival mode players
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());

                return;
            }

        } else if (itemRight == Items.IRON_AXE) {
            // EXTRACT CORE
            if (left.getItem() == this) {
                // Only Laser Blade
                ItemStack core = laserBlade.saveTagsToItemStack(new ItemStack(ToLaserBlade.Items.LASER_BLADE_CORE));
                event.setCost(4);
                event.setMaterialCost(1);
                event.setOutput(core);
            }

        } else {
            // CHANGE BLADE COLORS
            laserBlade.changeColorByItem(right);

            if (laserBlade.getCost() > 0) {
                laserBlade.changeDisplayName(name);
                event.setCost(laserBlade.getCost());
                event.setMaterialCost(1);
                event.setOutput(laserBlade.getItemStack());
            }

            return;
        }
    }

    @Nullable
    private Block getBlockFromItem(Item item) {
        if (item instanceof ItemBlock) {
            return ((ItemBlock) item).getBlock();
        }

        return null;
    }

    private boolean isContainedInItemTag(Item item, String tag) {
        return ItemTags.getCollection().get(new ResourceLocation(tag)).contains(item);
    }

    public String getSkullOwnerName(ItemStack stack) {
        if (stack.getItem() == Items.PLAYER_HEAD && stack.hasTag()) {
            String name = "";
            NBTTagCompound tag = stack.getTag();

            if (tag.contains("SkullOwner", NBT.TAG_STRING)) {
                name = tag.getString("SkullOwner");
            } else if (tag.contains("SkullOwner", NBT.TAG_COMPOUND)) {
                NBTTagCompound tagOwner = tag.getCompound("SkullOwner");

                if (tagOwner.contains("Name", NBT.TAG_STRING)) {
                    name = tagOwner.getString("Name");
                }
            }

            if (name != null) {
                return name;
            }
        }

        return "";
    }

    /* Shield function */

    @Override
    public boolean isShield(ItemStack stack, @Nullable EntityLivingBase entity) {
        return ToLaserBladeConfig.COMMON.isEnabledBlockingWithLaserBladeInServer.get();
    }

    @Override
    public EnumAction getUseAction(ItemStack stack) {
        if (ToLaserBladeConfig.COMMON.isEnabledBlockingWithLaserBladeInServer.get()) {
            return EnumAction.BLOCK;
        } else {
            return EnumAction.NONE;
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (ToLaserBladeConfig.COMMON.isEnabledBlockingWithLaserBladeInServer.get()) {
            return 72000;
        } else {
            return 0;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (ToLaserBladeConfig.COMMON.isEnabledBlockingWithLaserBladeInServer.get()) {
            EnumAction offhandItemAction = playerIn.getHeldItemOffhand().getUseAction();

            if (offhandItemAction != EnumAction.BOW && offhandItemAction != EnumAction.SPEAR) {
                playerIn.setActiveHand(handIn);
            }

            return new ActionResult<>(EnumActionResult.PASS, itemstack);
        } else {
            return new ActionResult<>(EnumActionResult.PASS, itemstack);
        }
    }

    /* Characterizing */

    @Override
    public float getAttackDamage() {
        return attackDamage;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (state.getBlockHardness(worldIn, pos) != 0.0F) {
            stack.damageItem(1, entityLiving);
        }

        return true;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return tier.getEfficiency();
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockIn) {
        return true;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, ToolType tool, EntityPlayer player, IBlockState blockState) {
        return tier.getHarvestLevel();
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

        if (slot == EntityEquipmentSlot.MAINHAND) {
            LaserBlade laserBlade = LaserBlade.create(stack);

            multimap.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", attackDamage + laserBlade.getAttack(), 0));
            multimap.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", attackSpeed + laserBlade.getSpeed(), 0));
        }

        return multimap;
    }

    @OnlyIn(Dist.CLIENT)
    public static class ColorHandler implements IItemColor {
        @Override
        public int getColor(ItemStack stack, int tintIndex) {
            if (ToLaserBladeConfig.CLIENT.isEnabledLaserBlade3DModel.get()) {
                return 0xFFFFFFFF;
            }

            switch (tintIndex) {
                case 1:
                    return getColorFromNBT(stack, LaserBlade.KEY_COLOR_HALO, LaserBlade.KEY_IS_SUB_COLOR_HALO, LaserBlade.DEFAULT_COLOR_HALO);

                case 2:
                    return getColorFromNBT(stack, LaserBlade.KEY_COLOR_CORE, LaserBlade.KEY_IS_SUB_COLOR_CORE, LaserBlade.DEFAULT_COLOR_CORE);

                default:
                    return 0xFFFFFFFF;
            }
        }

        private int getColorFromNBT(ItemStack stack, String keyColor, String keyIsSubColor, int defaultColor) {
            NBTTagCompound nbt = stack.getTag();

            if (nbt != null && nbt.contains(keyColor, NBT.TAG_INT)) {
                int color = nbt.getInt(keyColor);

                if (nbt.getBoolean(keyIsSubColor)) {
                    color = ~color | 0xFF000000;
                }

                return color;
            }

            return defaultColor;
        }
    }
}
