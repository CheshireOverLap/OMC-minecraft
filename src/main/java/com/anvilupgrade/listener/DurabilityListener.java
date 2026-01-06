package com.anvilupgrade.listener;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.manager.UpgradeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 강화 아이템 내구도 리스너
 * 10강 아이템은 내구도가 닳지 않음
 */
public class DurabilityListener implements Listener {

    private final AnvilUpgrade plugin;

    public DurabilityListener(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        int level = plugin.getUpgradeManager().getUpgradeLevel(item);
        
        // 10강 아이템은 내구도 감소 무시
        if (level >= UpgradeManager.MAX_UPGRADE_LEVEL) {
            event.setCancelled(true);
        }
    }
}
