package com.anvilupgrade.profile.type;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import com.anvilupgrade.profile.IUpgradeableProfile;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Optional;

public class CrossbowProfile implements IUpgradeableProfile {

    private static final double DAMAGE_PER_LEVEL = 0.4;

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        if (level <= 0) return "쇠뇌";
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 쇠뇌";
        return ChatColor.YELLOW + "+" + level + " 쇠뇌";
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {}

    @Override
    public Map<Attribute, Double> getLevelBonuses() {
        Attribute attackDamage = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_damage"));
        if (attackDamage == null) return Map.of();
        return Map.of(attackDamage, DAMAGE_PER_LEVEL);
    }

    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("supercharge")
        );
    }
}
