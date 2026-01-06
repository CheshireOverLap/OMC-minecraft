package com.anvilupgrade.profile.type;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import com.anvilupgrade.profile.IUpgradeableProfile;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Optional;

public class FishingRodProfile implements IUpgradeableProfile {

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        if (level <= 0) return "낚싯대";
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 낚싯대";
        return ChatColor.YELLOW + "+" + level + " 낚싯대";
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {}

    @Override
    public Map<Attribute, Double> getLevelBonuses() {
        return Map.of();
    }

    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("grappling_hook")
        );
    }
}
