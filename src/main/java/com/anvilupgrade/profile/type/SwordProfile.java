package com.anvilupgrade.profile.type;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import com.anvilupgrade.profile.IUpgradeableProfile;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
 * 검 강화 프로필
 * 레벨당 공격 속도 +0.3 증가
 * 10강: 최대 공격 속도 (1024) + 검무 능력
 */
public class SwordProfile implements IUpgradeableProfile {

    private static final double ATTACK_SPEED_PER_LEVEL = 0.3;
    private static final double BASE_ATTACK_SPEED = -2.4; // 검 기본 공격 속도
    
    private static final NamespacedKey SPEED_KEY = new NamespacedKey("anvilupgrade", "sword_attack_speed");
    private static final NamespacedKey DAMAGE_KEY = new NamespacedKey("anvilupgrade", "sword_base_damage");
    private static final NamespacedKey BASE_SPEED_KEY = new NamespacedKey("anvilupgrade", "sword_base_speed");

    @Override
    public String generateDisplayName(ItemStack originalItem, int level) {
        String baseName = getBaseName(originalItem);
        if (level <= 0) return baseName;
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 " + baseName;
        return ChatColor.YELLOW + "+" + level + " " + baseName;
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {
        Attribute attackSpeed = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_speed"));
        Attribute attackDamage = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_damage"));
        
        // 모든 기존 modifier 제거 (우리 것만)
        if (attackSpeed != null) {
            removeOurModifier(meta, attackSpeed, SPEED_KEY);
            removeOurModifier(meta, attackSpeed, BASE_SPEED_KEY);
        }
        if (attackDamage != null) {
            removeOurModifier(meta, attackDamage, DAMAGE_KEY);
        }
        
        if (level <= 0) return;

        // 기본 공격력 추가 (ItemMeta를 통해 Material 정보를 얻을 수 없으므로 강화 시 전달받은 값 사용)
        // 여기서는 기본값을 사용하고, 실제 값은 UpgradeManager에서 설정
        if (attackDamage != null) {
            // 기존에 우리가 아닌 modifier가 있는지 확인
            boolean hasVanillaModifier = false;
            Collection<AttributeModifier> existingDamage = meta.getAttributeModifiers(attackDamage);
            if (existingDamage != null) {
                for (AttributeModifier mod : existingDamage) {
                    if (!mod.getKey().getNamespace().equals("anvilupgrade")) {
                        hasVanillaModifier = true;
                        break;
                    }
                }
            }
            
            // vanilla modifier가 없으면 추가하지 않음 (기본 속성 사용)
            // vanilla modifier가 있으면 이미 있으니 건드리지 않음
        }

        // 공격 속도만 추가 (기본 속성은 건드리지 않음)
        if (attackSpeed != null) {
            // 강화 보너스 공격 속도만 추가
            double speedBonus = (level >= 10) ? 1024.0 : ATTACK_SPEED_PER_LEVEL * level;
            AttributeModifier speedMod = new AttributeModifier(
                    SPEED_KEY, speedBonus,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND
            );
            meta.addAttributeModifier(attackSpeed, speedMod);
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
        Attribute attackSpeed = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_speed"));
        if (attackSpeed == null) return Map.of();
        return Map.of(attackSpeed, ATTACK_SPEED_PER_LEVEL);
    }

    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("sword_dance")
        );
    }

    private String getBaseName(ItemStack item) {
        return switch (item.getType().name().toLowerCase()) {
            case "wooden_sword" -> "나무 검";
            case "stone_sword" -> "돌 검";
            case "iron_sword" -> "철 검";
            case "golden_sword" -> "금 검";
            case "diamond_sword" -> "다이아몬드 검";
            case "netherite_sword" -> "네더라이트 검";
            default -> "검";
        };
    }
}
