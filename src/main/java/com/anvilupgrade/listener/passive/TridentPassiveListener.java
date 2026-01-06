package com.anvilupgrade.listener.passive;

import com.anvilupgrade.AnvilUpgrade;
import org.bukkit.event.Listener;

/**
 * TridentPassiveListener - 패시브 효과 리스너 (추후 구현)
 */
public class TridentPassiveListener implements Listener {

    private final AnvilUpgrade plugin;

    public TridentPassiveListener(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }
    
    // TODO: 강화 레벨에 따른 패시브 효과 구현
}
