package com.anvilupgrade.profile.type;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import com.anvilupgrade.profile.IUpgradeableProfile;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class BootsProfile implements IUpgradeableProfile {

    private static final double ARMOR_PER_LEVEL = 0.3;
    private static final double SPEED_PER_LEVEL = 0.01;
    private static final NamespacedKey ARMOR_KEY = new NamespacedKey("anvilupgrade", "boots_armor");
    private static final NamespacedKey SPEED_KEY = new NamespacedKey("anvilupgrade", "boots_speed");

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        String baseName = getBaseName(item);
        if (level <= 0) return baseName;
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 " + baseName;
        return ChatColor.YELLOW + "+" + level + " " + baseName;
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {
        Attribute armor = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("armor"));
        Attribute speed = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("movement_speed"));
        
        if (armor != null) removeOurModifier(meta, armor, ARMOR_KEY);
        if (speed != null) removeOurModifier(meta, speed, SPEED_KEY);
        
        if (level <= 0) return;

        if (armor != null) {
            double armorBonus = ARMOR_PER_LEVEL * level;
            AttributeModifier armorMod = new AttributeModifier(
                    ARMOR_KEY, armorBonus,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET
            );
            meta.addAttributeModifier(armor, armorMod);
        }

        if (speed != null) {
            double speedBonus = SPEED_PER_LEVEL * level;
            AttributeModifier speedMod = new AttributeModifier(
                    SPEED_KEY, speedBonus,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET
            );
            meta.addAttributeModifier(speed, speedMod);
        }
    }

    private void removeOurModifier(ItemMeta meta, Attribute attribute, NamespacedKey key) {
        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
        if (modifiers == null) return;
        for (AttributeModifier mod : new ArrayList<>(modifiers)) {
            if (mod.getKey().equals(key)) {
                meta.removeAttributeModifier(attribute, mod);
            }
        }
    }

    @Override
    public Map<Attribute, Double> getLevelBonuses() {
        Attribute armor = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("armor"));
        Attribute speed = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("movement_speed"));
        if (armor == null || speed == null) return Map.of();
        return Map.of(armor, ARMOR_PER_LEVEL, speed, SPEED_PER_LEVEL);
    }

    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("double_jump")
        );
    }

    private String getBaseName(ItemStack item) {
        return switch (item.getType().name().toLowerCase()) {
            case "leather_boots" -> "가죽 부츠";
            case "chainmail_boots" -> "사슬 부츠";
            case "iron_boots" -> "철 부츠";
            case "golden_boots" -> "금 부츠";
            case "diamond_boots" -> "다이아몬드 부츠";
            case "netherite_boots" -> "네더라이트 부츠";
            default -> "부츠";
        };
    }
}
