package com.anvilupgrade.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * 인벤토리 유틸리티
 */
public class InventoryUtils {

    /**
     * 플레이어에게 아이템을 지급하고, 인벤토리가 가득 차면 바닥에 드롭합니다.
     */
    public static void giveOrDropItems(Player player, ItemStack... items) {
        if (items == null) return;
        
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;
            
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            
            // 인벤토리가 가득 찬 경우 바닥에 드롭
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
    }
}
