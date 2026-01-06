package com.anvilupgrade.item;

import com.anvilupgrade.AnvilUpgrade;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

/**
 * 강화석 아이템 관리
 */
public class UpgradeItems {

    private static final NamespacedKey UPGRADE_STONE_KEY = 
        new NamespacedKey(AnvilUpgrade.getInstance(), "upgrade_stone");

    /**
     * 강화석을 생성합니다.
     * @param amount 개수
     * @return 강화석 아이템
     */
    public static ItemStack createUpgradeStone(int amount) {
        ItemStack stone = new ItemStack(Material.ECHO_SHARD, amount);
        ItemMeta meta = stone.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§b강화석");
            meta.setLore(Arrays.asList(
                "§7모루에서 장비를 강화하는 데 사용합니다.",
                "",
                "§7강화 레벨이 높을수록",
                "§7더 많은 강화석이 필요합니다."
            ));
            
            // 빛나는 효과
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            // PDC로 식별
            meta.getPersistentDataContainer().set(
                UPGRADE_STONE_KEY, PersistentDataType.BYTE, (byte) 1
            );
            
            stone.setItemMeta(meta);
        }
        
        return stone;
    }

    /**
     * 강화석 1개를 생성합니다.
     */
    public static ItemStack createUpgradeStone() {
        return createUpgradeStone(1);
    }

    /**
     * 해당 아이템이 강화석인지 확인합니다.
     */
    public static boolean isUpgradeStone(ItemStack item) {
        if (item == null || item.getType() != Material.ECHO_SHARD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer()
            .has(UPGRADE_STONE_KEY, PersistentDataType.BYTE);
    }
}
