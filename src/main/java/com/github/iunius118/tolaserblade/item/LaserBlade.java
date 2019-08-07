package com.github.iunius118.tolaserblade.item;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.NetherBiome;
import net.minecraft.world.biome.TheEndBiome;
import net.minecraft.world.biome.TheVoidBiome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Map;

public class LaserBlade {
    private ItemStack stack;
    private int damage;
    private float attack;
    private float speed;
    private Map<Enchantment, Integer> mapEnch;
    private int cost;

    private static final Map<Enchantment, Integer> costLevelRates;

    static {
        costLevelRates = Maps.newLinkedHashMap();
        costLevelRates.put(Enchantments.SMITE, 2);
        costLevelRates.put(Enchantments.LOOTING, 4);
        costLevelRates.put(Enchantments.SWEEPING, 4);
        costLevelRates.put(Enchantments.FIRE_ASPECT, 4);
        costLevelRates.put(Enchantments.MENDING, 8);
        costLevelRates.put(Enchantments.VANISHING_CURSE, 8);
    }

    // Blade color table
    public final static int[] colors = {0xFFFF0000, 0xFFD0A000, 0xFF00E000, 0xFF0080FF, 0xFF0000FF, 0xFFA000FF, 0xFFFFFFFF, 0xFF020202, 0xFFA00080};

    public static final int DEFAULT_COLOR_CORE = 0xFFFFFFFF;
    public static final int DEFAULT_COLOR_HALO = 0xFFFF0000;

    public static final String KEY_ATK = "ATK";
    public static final String KEY_SPD = "SPD";
    public static final String KEY_COLOR_CORE = "colorC";
    public static final String KEY_COLOR_HALO = "colorH";
    public static final String KEY_IS_SUB_COLOR_CORE = "isSubC";
    public static final String KEY_IS_SUB_COLOR_HALO = "isSubH";
    public static final String KEY_IS_CRAFTING = "isCrafting";

    public static final float MOD_SPD_CLASS_1 = 0.0F;
    public static final float MOD_SPD_CLASS_3 = 1.2F;

    public static final float MOD_ATK_MIN = -6.0F;
    public static final float MOD_ATK_CLASS_1 = -1.0F;
    public static final float MOD_ATK_CLASS_2 = 0.0F;
    public static final float MOD_ATK_CLASS_3 = 3.0F;
    public static final float MOD_ATK_CLASS_4 = 7.0F;
    public static final float MOD_ATK_MAX = 2041.0F;

    public static final float MOD_CRITICAL_VS_WITHER = 2.0F;

    public static final int LVL_SMITE_CLASS_1 = 1;
    public static final int LVL_SMITE_CLASS_2 = 2;
    public static final int LVL_SMITE_CLASS_3 = 5;
    public static final int LVL_SMITE_CLASS_4 = 10;

    public static final int COST_LVL_CLASS_2 = 5;
    public static final int COST_LVL_CLASS_4 = 30;

    public static final int MAX_USES = 32000;

    public LaserBlade() {
        this(new ItemStack(ToLaserBlade.Items.LASER_BLADE));
    }

    public LaserBlade(@Nonnull ItemStack itemStack) {
        stack = itemStack;
        boolean isLaserBladeStack = isItemLaserBlade();

        damage = itemStack.getDamage();

        CompoundNBT nbt = stack.getOrCreateTag();

        if (isLaserBladeStack) {
            setAttack(nbt.getFloat(KEY_ATK));

        } else {
            attack = MOD_ATK_CLASS_1;
        }

        setSpeed(nbt.getFloat(KEY_SPD));
        mapEnch = EnchantmentHelper.getEnchantments(stack);

        Integer sharpness = mapEnch.getOrDefault(Enchantments.SHARPNESS, 0);
        setAttackIfLess(getAttackFromSharpness(sharpness));
        mapEnch.remove(Enchantments.SHARPNESS);

        Integer unbreaking = mapEnch.getOrDefault(Enchantments.UNBREAKING, 0);
        setSpeedIfLess(getSpeedFromUnbreaking(unbreaking));
        mapEnch.remove(Enchantments.UNBREAKING);

        // Fix speed for old version
        if (isLaserBladeStack && !nbt.contains(KEY_SPD)) {
            int smite = mapEnch.getOrDefault(Enchantments.SMITE, 0);

            if (smite < LVL_SMITE_CLASS_1) {
                enchant(Enchantments.SMITE, LVL_SMITE_CLASS_2);
                EnchantmentHelper.setEnchantments(mapEnch, stack);

            } else if (smite >= LVL_SMITE_CLASS_4) {
                speed = MOD_SPD_CLASS_3;
            }

            nbt.putFloat(KEY_ATK, attack);
            nbt.putFloat(KEY_SPD, speed);

            // Set default colors if they not set
            getCoreColor();
            isCoreSubColor();
            getHaloColor();
            isHaloSubColor();
        }
    }

    public static LaserBlade create() {
        return new LaserBlade();
    }

    public static LaserBlade create(@Nonnull ItemStack itemStack) {
        return new LaserBlade(itemStack);
    }

    public LaserBlade setCraftingTag() {
        CompoundNBT nbt = stack.getTag();
        nbt.putBoolean(KEY_IS_CRAFTING, true);
        return this;
    }

    public LaserBlade removeCraftingTag() {
        CompoundNBT nbt = stack.getTag();
        nbt.remove(KEY_IS_CRAFTING);
        return this;
    }

    public boolean hasCraftingTag() {
        CompoundNBT nbt = stack.getTag();
        return nbt.contains(KEY_IS_CRAFTING);
    }

    private float getAttackFromSharpness(Integer level) {
        if (level > 0) {
            return (level > (int) MOD_ATK_MAX + 1) ? (int) MOD_ATK_MAX + 1 : level - 1;
        }

        return MOD_ATK_CLASS_1;
    }

    private float getSpeedFromUnbreaking(Integer level) {
        if (level > 0) {
            return ((level > 3.0F) ? 3.0F : level) * 0.4F;
        }

        return MOD_SPD_CLASS_1;
    }

    public int getDamage() {
        return damage;
    }

    public float getAttack() {
        return attack;
    }

    public float getSpeed() {
        return speed;
    }

    public int getCoreColor() {
        CompoundNBT nbt = stack.getTag();

        if (nbt.contains(KEY_COLOR_CORE, NBT.TAG_INT)) {
            return nbt.getInt(KEY_COLOR_CORE);
        }

        nbt.putInt(KEY_COLOR_CORE, DEFAULT_COLOR_CORE);
        return DEFAULT_COLOR_CORE;
    }

    public boolean isCoreSubColor() {
        CompoundNBT nbt = stack.getTag();

        if (nbt.contains(KEY_IS_SUB_COLOR_CORE, NBT.TAG_BYTE)) {
            return nbt.getBoolean(KEY_IS_SUB_COLOR_CORE);
        }

        nbt.putBoolean(KEY_IS_SUB_COLOR_CORE, false);
        return false;
    }

    public int getHaloColor() {
        CompoundNBT nbt = stack.getTag();

        if (nbt.contains(KEY_COLOR_HALO, NBT.TAG_INT)) {
            return nbt.getInt(KEY_COLOR_HALO);
        }

        nbt.putInt(KEY_COLOR_HALO, DEFAULT_COLOR_HALO);
        return DEFAULT_COLOR_HALO;
    }

    public boolean isHaloSubColor() {
        CompoundNBT nbt = stack.getTag();

        if (nbt.contains(KEY_IS_SUB_COLOR_HALO, NBT.TAG_BYTE)) {
            return nbt.getBoolean(KEY_IS_SUB_COLOR_HALO);
        }

        nbt.putBoolean(KEY_IS_SUB_COLOR_HALO, false);
        return false;
    }

    public int getEnchantmentLavel(Enchantment enchantment) {
        return mapEnch.getOrDefault(enchantment, 0);
    }

    public boolean isEnchantmentMaxLevel(Enchantment enchantment) {
        return mapEnch.getOrDefault(enchantment, 0) >= enchantment.getMaxLevel();
    }

    public ImmutableMap<Enchantment, Integer> getEnchantmentMap() {
        return ImmutableMap.copyOf(mapEnch);
    }

    public int getCost() {
        return cost;
    }

    public ItemStack getItemStack() {
        if (!isItemLaserBlade()) {
            return saveTagsToItemStack(stack);
        }

        CompoundNBT nbt = stack.getTag();

        nbt.putFloat(KEY_ATK, attack);
        nbt.putFloat(KEY_SPD, speed);
        EnchantmentHelper.setEnchantments(mapEnch, stack);

        stack.setDamage(damage);

        return stack;
    }

    public boolean isItemLaserBlade() {
        return stack.getItem() == ToLaserBlade.Items.LASER_BLADE;
    }

    public LaserBlade setDefaultColors() {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt(KEY_COLOR_CORE, DEFAULT_COLOR_CORE);
        nbt.putInt(KEY_COLOR_HALO, DEFAULT_COLOR_HALO);
        nbt.putBoolean(KEY_IS_SUB_COLOR_CORE, false);
        nbt.putBoolean(KEY_IS_SUB_COLOR_HALO, false);
        return this;
    }

    public LaserBlade setCoreColor(int color) {
        CompoundNBT nbt = stack.getTag();
        nbt.putInt(KEY_COLOR_CORE, color);
        return this;
    }

    public LaserBlade setCoreSubColor(boolean isSubColor) {
        CompoundNBT nbt = stack.getTag();
        nbt.putBoolean(KEY_IS_SUB_COLOR_CORE, isSubColor);
        return this;
    }

    public LaserBlade flipCoreSubColor() {
        CompoundNBT nbt = stack.getOrCreateTag();
        boolean b = nbt.getBoolean(KEY_IS_SUB_COLOR_CORE);
        nbt.putBoolean(KEY_IS_SUB_COLOR_CORE, !b);
        return this;
    }

    public LaserBlade setHaloColor(int color) {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt(KEY_COLOR_HALO, color);
        return this;
    }

    public LaserBlade setHaloSubColor(boolean isSubColor) {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putBoolean(KEY_IS_SUB_COLOR_HALO, isSubColor);
        return this;
    }

    public LaserBlade flipHaloSubColor() {
        CompoundNBT nbt = stack.getOrCreateTag();
        boolean b = nbt.getBoolean(KEY_IS_SUB_COLOR_HALO);
        nbt.putBoolean(KEY_IS_SUB_COLOR_HALO, !b);
        return this;
    }

    public LaserBlade changeColorsByBiome(PlayerEntity player) {
        World world = player.world;
        Biome biome = world.getBiome(player.getPosition());

        // Dyeing by Biome type or Biome temperature
        if (world.getDimension().getType() == DimensionType.THE_NETHER || biome instanceof NetherBiome) {
            // Nether
            flipCoreSubColor();
        } else if (world.getDimension().getType() == DimensionType.THE_END || biome instanceof TheEndBiome) {
            // The End
            flipHaloSubColor();
        } else if (biome instanceof TheVoidBiome) {
            // The Void
            setCoreColor(colors[7]);
            setHaloColor(colors[7]);
        } else {
            // Biomes on Overworld or the other dimensions
            float temp = biome.getDefaultTemperature();

            if (temp > 1.5F) {
                // t > 1.5
                setHaloColor(colors[5]);
            } else if (temp > 1.0F) {
                // 1.5 >= t > 1.0
                setHaloColor(colors[8]);
            } else if (temp > 0.8F) {
                // 1.0 >= t > 0.8
                setHaloColor(colors[1]);
            } else if (temp >= 0.5F) {
                // 0.8 >= t >= 0.5
                setHaloColor(colors[0]);
            } else if (temp >= 0.2F) {
                // 0.5 > t >= 0.2
                setHaloColor(colors[2]);
            } else if (temp >= -0.25F) {
                // 0.2 > t >= -0.25
                setHaloColor(colors[3]);
            } else {
                // -0.25 > t
                setHaloColor(colors[4]);
            }
        }

        return this;
    }

    public LaserBlade changeColorByItem(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof DyeItem) {
            int color = ((DyeItem) stack.getItem()).getDyeColor().getMapColor().colorValue | 0xFF000000;

            if (getCoreColor() != color) {
                setCoreColor(color);
                cost++;
            }
        } else if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();

            if (block instanceof StainedGlassBlock) {
                int color = ((StainedGlassBlock) block).getColor().getMapColor().colorValue | 0xFF000000;

                if (getHaloColor() != color) {
                    setHaloColor(color);
                    cost++;
                }
            }
        }

        return this;
    }

    public LaserBlade changeDisplayName(String name) {
        // Set or Clear display name
        if (StringUtils.isBlank(name)) {
            if (stack.hasDisplayName()) {
                stack.clearCustomName();
                cost += 1;
            }
        } else {
            if (!name.equals(stack.getDisplayName().getString())) {
                stack.setDisplayName(new StringTextComponent(name));
                cost += 1;
            }
        }

        return this;
    }

    public boolean isUpgradedWithNetherStar() {
        return mapEnch.getOrDefault(Enchantments.SMITE, 0) >= LVL_SMITE_CLASS_4
                && mapEnch.getOrDefault(Enchantments.MENDING, 0) >= 1
                && !(attack < MOD_ATK_CLASS_4)
                && !(speed < MOD_SPD_CLASS_3);
    }

    public LaserBlade upgradeWithNetherStar() {
        if (!isUpgradedWithNetherStar()) {
            enchantIfLessLevel(Enchantments.SMITE, LVL_SMITE_CLASS_4);
            enchantIfLessLevel(Enchantments.MENDING, 1);
            setAttackIfLess(MOD_ATK_CLASS_4);
            setSpeedIfLess(MOD_SPD_CLASS_3);
            cost += COST_LVL_CLASS_4;
        }

        return this;
    }

    public LaserBlade setAttack(float f) {
        if (f > MOD_ATK_MAX) {
            attack = MOD_ATK_MAX;
        } else {
            attack = Math.max(f, MOD_ATK_MIN);
        }

        return this;
    }

    public LaserBlade setAttackIfLess(float f) {
        if (attack < f) {
            setAttack(f);
        }

        return this;
    }

    public LaserBlade setSpeed(float f) {
        if (f > MOD_SPD_CLASS_3) {
            speed = MOD_SPD_CLASS_3;
        } else {
            speed = Math.max(f, MOD_SPD_CLASS_1);
        }

        return this;
    }

    public LaserBlade setSpeedIfLess(float f) {
        if (speed < f) {
            setSpeed(f);
        }

        return this;
    }

    public LaserBlade enchant(Enchantment enchantment, int level) {
        mapEnch.put(enchantment, level);
        return this;
    }

    public LaserBlade enchantIfLessLevel(Enchantment enchantment, int level) {
        if (mapEnch.getOrDefault(enchantment, 0) < level) {
            enchant(enchantment, level);
        }

        return this;
    }

    public LaserBlade increaseAttack() {
        attack++;
        cost += ((int) attack + 1) * 2;

        return this;
    }

    public LaserBlade increaseSpeed() {
        if (speed < MOD_SPD_CLASS_1) {
            speed = MOD_SPD_CLASS_1;
        }

        int level = (int) (speed / 0.4F);
        level++;

        if (level > 3) {
            speed = MOD_SPD_CLASS_3;
            return this;
        }

        speed = level * 0.4F;
        cost += level * 2;

        return this;
    }

    public LaserBlade increaseEnchantmentLevel(Enchantment enchantment) {
        int level = mapEnch.getOrDefault(enchantment, 0);

        if (level >= enchantment.getMaxLevel()) {
            return this;
        }

        level++;
        enchant(enchantment, level);
        cost += level * costLevelRates.get(enchantment);

        return this;
    }


    /**
     * Save Laser Blade spec to a Laser Blade stack
     *
     * @param itemStack Input Laser Blade stack
     * @return Saved Laser Blade stack
     */
    public ItemStack saveTagsToLaserBlade(ItemStack itemStack) {
        CompoundNBT newNBT = itemStack.getOrCreateTag();
        CompoundNBT oldNBT = stack.getTag();

        newNBT.putFloat(KEY_ATK, attack);
        newNBT.putFloat(KEY_SPD, speed);
        copyNBTInt(newNBT, oldNBT, KEY_COLOR_CORE, DEFAULT_COLOR_CORE);
        copyNBTInt(newNBT, oldNBT, KEY_COLOR_HALO, DEFAULT_COLOR_HALO);
        copyNBTBoolean(newNBT, oldNBT, KEY_IS_SUB_COLOR_CORE, false);
        copyNBTBoolean(newNBT, oldNBT, KEY_IS_SUB_COLOR_HALO, false);

        EnchantmentHelper.setEnchantments(mapEnch, itemStack);

        itemStack.setDamage(damage);

        return itemStack;
    }

    /**
     * Save Laser Blade spec to a item stack
     *
     * @param itemStack Input stack like Laser Blade Core
     * @return Saved input stack
     */
    public ItemStack saveTagsToItemStack(ItemStack itemStack) {
        CompoundNBT newNBT = itemStack.getOrCreateTag();
        CompoundNBT oldNBT = stack.getTag();

        copyNBTInt(newNBT, oldNBT, KEY_COLOR_CORE, DEFAULT_COLOR_CORE);
        copyNBTInt(newNBT, oldNBT, KEY_COLOR_HALO, DEFAULT_COLOR_HALO);
        copyNBTBoolean(newNBT, oldNBT, KEY_IS_SUB_COLOR_CORE, false);
        copyNBTBoolean(newNBT, oldNBT, KEY_IS_SUB_COLOR_HALO, false);

        Map<Enchantment, Integer> map = Maps.newLinkedHashMap();
        setSharpnessFromAttack(map);
        setUnbreakingFromSpeed(map);
        mapEnch.forEach(map::put);
        EnchantmentHelper.setEnchantments(map, itemStack);

        return itemStack;
    }

    private void copyNBTInt(CompoundNBT newNBT, CompoundNBT oldNBT, String key, int defaultValue) {
        if (oldNBT.contains(key, NBT.TAG_INT)) {
            newNBT.putInt(key, oldNBT.getInt(key));
        } else {
            newNBT.putInt(key, defaultValue);
        }
    }

    private void copyNBTBoolean(CompoundNBT newNBT, CompoundNBT oldNBT, String key, boolean defaultValue) {
        if (oldNBT.contains(key)) {
            newNBT.putBoolean(key, oldNBT.getBoolean(key));
        } else {
            newNBT.putBoolean(key, defaultValue);
        }
    }

    private void setSharpnessFromAttack(Map<Enchantment, Integer> map) {
        if (attack >= MOD_ATK_CLASS_2) {
            map.put(Enchantments.SHARPNESS, (int) attack + 1);
        }
    }

    private void setUnbreakingFromSpeed(Map<Enchantment, Integer> map) {
        if (speed > MOD_SPD_CLASS_1) {
            int level = Math.min(3, (int) (speed / 0.4F));
            map.put(Enchantments.UNBREAKING, level);
        }
    }

    public LaserBlade mixLaserBlade(LaserBlade other) {
        if (attack < other.getAttack()) {
            attack = other.getAttack();
            cost++;
        }

        if (speed < other.getSpeed()) {
            speed = other.getSpeed();
            cost++;
        }

        // Mix Enchantments
        Map<Enchantment, Integer> mapOtherEnch = other.getEnchantmentMap();
        costLevelRates.forEach((ench, rate) -> mixEnchantment(mapOtherEnch, ench, rate));

        // Repair
        if (other.isItemLaserBlade()) {
            int repairValue = Math.min(damage, MAX_USES - other.getDamage());
            damage = Math.max(0, damage - repairValue - (int) (MAX_USES * 0.12F));
            cost += (int) Math.ceil(repairValue / (double) (MAX_USES / 4));
        }

        return this;
    }

    public int repairByMaterial(int stackSize) {
        int costItem = Math.min(stackSize, (int) Math.ceil(damage / (double) (MAX_USES / 4)));
        damage = Math.max(0, damage - costItem * (MAX_USES / 4));
        cost += costItem;
        return costItem;
    }

    private void mixEnchantment(Map<Enchantment, Integer> otherEnchantments, Enchantment enchantment, int costRate) {
        int thisLevel = mapEnch.getOrDefault(enchantment, 0);
        int otherLevel = otherEnchantments.getOrDefault(enchantment, 0);

        if (thisLevel > 0 && thisLevel == otherLevel && thisLevel < enchantment.getMaxLevel()) {
            thisLevel++;
            enchant(enchantment, thisLevel);
            cost += thisLevel * costRate;

        } else if (thisLevel < otherLevel) {
            enchant(enchantment, otherLevel);
            cost++;
        }
    }
}