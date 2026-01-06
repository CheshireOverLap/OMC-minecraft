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

public class ShovelProfile implements IUpgradeableProfile {

    private static final NamespacedKey ORIGINAL_EFFICIENCY_KEY = new NamespacedKey("anvilupgrade", "original_efficiency");

    @Override
    public String generateDisplayName(ItemStack item, int level) {
        String baseName = getBaseName(item);
        if (level <= 0) return baseName;
        if (level >= 10) return ChatColor.LIGHT_PURPLE + "전설의 " + baseName;
        return ChatColor.YELLOW + "+" + level + " " + baseName;
    }

    @Override
    public void applyAttributes(ItemMeta meta, int level) {
    }
    
    @Override
    public void applyEnchantments(ItemMeta meta, int level) {
        // 원본 인챈트 저장 (최초 1회)
        if (!meta.getPersistentDataContainer().has(ORIGINAL_EFFICIENCY_KEY, PersistentDataType.INTEGER)) {
            int originalEfficiency = meta.getEnchantLevel(Enchantment.EFFICIENCY);
            meta.getPersistentDataContainer().set(ORIGINAL_EFFICIENCY_KEY, PersistentDataType.INTEGER, originalEfficiency);
        }
        
        int originalEfficiency = meta.getPersistentDataContainer().getOrDefault(ORIGINAL_EFFICIENCY_KEY, PersistentDataType.INTEGER, 0);
        
        if (level <= 0) {
            if (originalEfficiency > 0) {
                meta.addEnchant(Enchantment.EFFICIENCY, originalEfficiency, true);
            } else {
                meta.removeEnchant(Enchantment.EFFICIENCY);
            }
            return;
        }
        
        int newEfficiency = originalEfficiency + level;
        meta.addEnchant(Enchantment.EFFICIENCY, Math.min(newEfficiency, 255), true);
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
            case "wooden_shovel" -> "나무 삽";
            case "stone_shovel" -> "돌 삽";
            case "iron_shovel" -> "철 삽";
            case "golden_shovel" -> "금 삽";
            case "diamond_shovel" -> "다이아몬드 삽";
            case "netherite_shovel" -> "네더라이트 삽";
            default -> "삽";
        };
    }
}
