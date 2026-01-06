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

public class ShieldProfile implements IUpgradeableProfile {

    private static final double KNOCKBACK_RESIST_PER_LEVEL = 0.05;
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("anvilupgrade", "shield_knockback");

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        if (level <= 0) return "방패";
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 방패";
        return ChatColor.YELLOW + "+" + level + " 방패";
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {
        Attribute knockbackResist = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("knockback_resistance"));
        if (knockbackResist == null) return;

        removeOurModifier(meta, knockbackResist, MODIFIER_KEY);
        if (level <= 0) return;

        double bonus = KNOCKBACK_RESIST_PER_LEVEL * level;
        AttributeModifier modifier = new AttributeModifier(
                MODIFIER_KEY, bonus,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.OFFHAND
        );
        meta.addAttributeModifier(knockbackResist, modifier);
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
        Attribute knockbackResist = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("knockback_resistance"));
        if (knockbackResist == null) return Map.of();
        return Map.of(knockbackResist, KNOCKBACK_RESIST_PER_LEVEL);
    }

    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("shield_bash")
        );
    }
}
