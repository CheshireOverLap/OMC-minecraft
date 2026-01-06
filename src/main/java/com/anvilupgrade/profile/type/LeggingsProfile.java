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

public class LeggingsProfile implements IUpgradeableProfile {

    private static final double ARMOR_PER_LEVEL = 0.6;
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("anvilupgrade", "leggings_armor");

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
        if (armor == null) return;

        removeOurModifier(meta, armor, MODIFIER_KEY);
        if (level <= 0) return;

        double bonus = ARMOR_PER_LEVEL * level;
        AttributeModifier modifier = new AttributeModifier(
                MODIFIER_KEY, bonus,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.LEGS
        );
        meta.addAttributeModifier(armor, modifier);
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
        if (armor == null) return Map.of();
        return Map.of(armor, ARMOR_PER_LEVEL);
    }

    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("regeneration")
        );
    }

    private String getBaseName(ItemStack item) {
        return switch (item.getType().name().toLowerCase()) {
            case "leather_leggings" -> "가죽 레깅스";
            case "chainmail_leggings" -> "사슬 레깅스";
            case "iron_leggings" -> "철 레깅스";
            case "golden_leggings" -> "금 레깅스";
            case "diamond_leggings" -> "다이아몬드 레깅스";
            case "netherite_leggings" -> "네더라이트 레깅스";
            default -> "레깅스";
        };
    }
}
