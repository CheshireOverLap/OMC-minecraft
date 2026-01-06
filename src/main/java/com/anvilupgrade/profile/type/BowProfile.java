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

import java.util.Map;
import java.util.Optional;

public class BowProfile implements IUpgradeableProfile {

    private static final double DAMAGE_PER_LEVEL = 0.3;
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("anvilupgrade", "bow_damage");

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        if (level <= 0) return "활";
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 활";
        return ChatColor.YELLOW + "+" + level + " 활";
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {
        // 활은 투사체 데미지라 Attribute로 직접 적용 어려움
        // 패시브 리스너에서 처리
    }

    @Override
    public Map<Attribute, Double> getLevelBonuses() {
        Attribute attackDamage = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_damage"));
        if (attackDamage == null) return Map.of();
        return Map.of(attackDamage, DAMAGE_PER_LEVEL);
    }

    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("laser_shot")
        );
    }
}
