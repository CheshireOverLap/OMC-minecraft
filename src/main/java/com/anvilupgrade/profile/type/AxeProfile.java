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

/**
 * 도끼 강화 프로필
 * 레벨당 공격력 +0.5 증가
 * 10강: 기절 능력
 * 기본 속성 보존!
 */
public class AxeProfile implements IUpgradeableProfile {

    private static final double DAMAGE_PER_LEVEL = 0.5;
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("anvilupgrade", "axe_damage");

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        String baseName = getBaseName(item);
        if (level <= 0) return baseName;
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 " + baseName;
        return ChatColor.YELLOW + "+" + level + " " + baseName;
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {
        Attribute attackDamage = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_damage"));
        if (attackDamage == null) return;

        // 우리가 추가한 modifier만 제거 (기본 속성 보존)
        removeOurModifier(meta, attackDamage, MODIFIER_KEY);
        
        if (level <= 0) return;

        // 추가 공격력만 부여 (기본 공격력은 건드리지 않음)
        double bonus = DAMAGE_PER_LEVEL * level;
        AttributeModifier modifier = new AttributeModifier(
                MODIFIER_KEY, bonus,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
        );
        meta.addAttributeModifier(attackDamage, modifier);
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
        Attribute attackDamage = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_damage"));
        if (attackDamage == null) return Map.of();
        return Map.of(attackDamage, DAMAGE_PER_LEVEL);
    }

    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("stun")
        );
    }

    private String getBaseName(ItemStack item) {
        return switch (item.getType().name().toLowerCase()) {
            case "wooden_axe" -> "나무 도끼";
            case "stone_axe" -> "돌 도끼";
            case "iron_axe" -> "철 도끼";
            case "golden_axe" -> "금 도끼";
            case "diamond_axe" -> "다이아몬드 도끼";
            case "netherite_axe" -> "네더라이트 도끼";
            default -> "도끼";
        };
    }
}
