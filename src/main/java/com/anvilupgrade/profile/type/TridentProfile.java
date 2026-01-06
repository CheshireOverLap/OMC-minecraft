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
 * 삼지창 강화 프로필
 * 레벨당 공격력 +0.5 증가
 * 10강: 번개창 능력
 * 기본 속성 보존!
 */
public class TridentProfile implements IUpgradeableProfile {

    private static final double DAMAGE_PER_LEVEL = 0.5;
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("anvilupgrade", "trident_damage");

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        if (level <= 0) return "삼지창";
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 삼지창";
        return ChatColor.YELLOW + "+" + level + " 삼지창";
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {
        Attribute attackDamage = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_damage"));
        if (attackDamage == null) return;

        // 우리가 추가한 modifier만 제거 (기본 속성 보존)
        removeOurModifier(meta, attackDamage, MODIFIER_KEY);
        
        if (level <= 0) return;

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
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("lightning_spear")
        );
    }
}
