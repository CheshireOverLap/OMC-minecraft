package com.anvilupgrade.profile.type;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import com.anvilupgrade.profile.IUpgradeableProfile;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Optional;

public class PickaxeProfile implements IUpgradeableProfile {

    // 원본 인챈트 저장용 PDC 키
    private static final NamespacedKey ORIGINAL_EFFICIENCY_KEY = new NamespacedKey("anvilupgrade", "original_efficiency");
    private static final NamespacedKey ORIGINAL_FORTUNE_KEY = new NamespacedKey("anvilupgrade", "original_fortune");

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        String baseName = getBaseName(item);
        if (level <= 0) return baseName;
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 " + baseName;
        return ChatColor.YELLOW + "+" + level + " " + baseName;
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {
        // 인챈트는 applyEnchantments에서 처리
    }
    
    @Override
    public void applyEnchantments(ItemMeta meta, int level) {
        // 원본 인챈트 저장 (최초 1회)
        if (!meta.getPersistentDataContainer().has(ORIGINAL_EFFICIENCY_KEY, PersistentDataType.INTEGER)) {
            int originalEfficiency = meta.getEnchantLevel(Enchantment.EFFICIENCY);
            int originalFortune = meta.getEnchantLevel(Enchantment.FORTUNE);
            meta.getPersistentDataContainer().set(ORIGINAL_EFFICIENCY_KEY, PersistentDataType.INTEGER, originalEfficiency);
            meta.getPersistentDataContainer().set(ORIGINAL_FORTUNE_KEY, PersistentDataType.INTEGER, originalFortune);
        }
        
        // 원본 인챈트 불러오기
        int originalEfficiency = meta.getPersistentDataContainer().getOrDefault(ORIGINAL_EFFICIENCY_KEY, PersistentDataType.INTEGER, 0);
        int originalFortune = meta.getPersistentDataContainer().getOrDefault(ORIGINAL_FORTUNE_KEY, PersistentDataType.INTEGER, 0);
        
        if (level <= 0) {
            // 0강이면 원본으로 복구
            if (originalEfficiency > 0) {
                meta.addEnchant(Enchantment.EFFICIENCY, originalEfficiency, true);
            } else {
                meta.removeEnchant(Enchantment.EFFICIENCY);
            }
            if (originalFortune > 0) {
                meta.addEnchant(Enchantment.FORTUNE, originalFortune, true);
            } else {
                meta.removeEnchant(Enchantment.FORTUNE);
            }
            return;
        }
        
        // 원본 + 강화 레벨
        int newEfficiency = originalEfficiency + level;
        meta.addEnchant(Enchantment.EFFICIENCY, Math.min(newEfficiency, 255), true);
        
        // 5강부터 행운 추가
        if (level >= 5) {
            int fortuneBonus = level - 4; // 5강=1, 6강=2, ... 10강=6
            int newFortune = originalFortune + fortuneBonus;
            meta.addEnchant(Enchantment.FORTUNE, Math.min(newFortune, 255), true);
        } else {
            // 5강 미만이면 원본 행운으로
            if (originalFortune > 0) {
                meta.addEnchant(Enchantment.FORTUNE, originalFortune, true);
            } else {
                meta.removeEnchant(Enchantment.FORTUNE);
            }
        }
    }

    @Override
    public Map<Attribute, Double> getLevelBonuses() {
        return Map.of();
    }
    
    @Override
    public Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.ofNullable(
            AnvilUpgrade.getInstance().getSpecialAbilityManager().getRegisteredAbility("vein_miner")
        );
    }

    private String getBaseName(ItemStack item) {
        return switch (item.getType().name().toLowerCase()) {
            case "wooden_pickaxe" -> "나무 곡괭이";
            case "stone_pickaxe" -> "돌 곡괭이";
            case "iron_pickaxe" -> "철 곡괭이";
            case "golden_pickaxe" -> "금 곡괭이";
            case "diamond_pickaxe" -> "다이아몬드 곡괭이";
            case "netherite_pickaxe" -> "네더라이트 곡괭이";
            default -> "곡괭이";
        };
    }
}
