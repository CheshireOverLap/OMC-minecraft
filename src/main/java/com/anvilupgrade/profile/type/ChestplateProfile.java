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

public class ChestplateProfile implements IUpgradeableProfile {

    private static final double ARMOR_PER_LEVEL = 0.8;
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("anvilupgrade", "chestplate_armor");

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
                EquipmentSlotGroup.CHEST
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
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("absorption")
        );
    }

    private String getBaseName(ItemStack item) {
        return switch (item.getType().name().toLowerCase()) {
            case "leather_chestplate" -> "가죽 흉갑";
            case "chainmail_chestplate" -> "사슬 흉갑";
            case "iron_chestplate" -> "철 흉갑";
            case "golden_chestplate" -> "금 흉갑";
            case "diamond_chestplate" -> "다이아몬드 흉갑";
            case "netherite_chestplate" -> "네더라이트 흉갑";
            default -> "흉갑";
        };
    }
}
