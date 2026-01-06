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

public class HelmetProfile implements IUpgradeableProfile {

    private static final double ARMOR_PER_LEVEL = 0.5;
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("anvilupgrade", "helmet_armor");

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
                EquipmentSlotGroup.HEAD
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
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("cleansing")
        );
    }

    private String getBaseName(ItemStack item) {
        return switch (item.getType().name().toLowerCase()) {
            case "leather_helmet" -> "가죽 투구";
            case "chainmail_helmet" -> "사슬 투구";
            case "iron_helmet" -> "철 투구";
            case "golden_helmet" -> "금 투구";
            case "diamond_helmet" -> "다이아몬드 투구";
            case "netherite_helmet" -> "네더라이트 투구";
            case "turtle_helmet" -> "거북 등껍질";
            default -> "투구";
        };
    }
}
