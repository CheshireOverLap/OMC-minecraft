package com.anvilupgrade.util;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.manager.SpecialAbilityManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 액션바 쿨다운 표시 관리자
 */
public class ActionBarManager {

    private final AnvilUpgrade plugin;
    private final SpecialAbilityManager abilityManager;

    public ActionBarManager(AnvilUpgrade plugin, SpecialAbilityManager abilityManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
        startActionBarTask();
    }

    private void startActionBarTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateActionBar(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void updateActionBar(Player player) {
        StringBuilder sb = new StringBuilder();
        
        for (var ability : abilityManager.getAllAbilities()) {
            if (!ability.showInActionBar()) continue;
            
            long remaining = abilityManager.getRemainingCooldown(player, ability.getInternalName());
            if (remaining > 0) {
                if (sb.length() > 0) sb.append("  ");
                sb.append("§7").append(ability.getDisplayName())
                  .append("§f: §c").append(String.format("%.1f", remaining / 1000.0)).append("s");
            }
        }

        if (sb.length() > 0) {
            player.sendActionBar(Component.text(sb.toString()));
        }
    }
}
