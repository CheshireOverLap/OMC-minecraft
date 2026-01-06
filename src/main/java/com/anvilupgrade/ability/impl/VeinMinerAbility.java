package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 3x3 블록 파괴 (패시브) - 도구용
 * 실제 로직은 VeinMinerListener에서 처리
 */
public class VeinMinerAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public VeinMinerAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "vein_miner"; }

    @Override
    public String getDisplayName() { return "§6광역 채굴"; }

    @Override
    public String getDescription() { return "§7블록을 3x3으로 파괴합니다. (패시브)"; }

    @Override
    public double getCooldownSeconds() {
        return 0; // 패시브 능력이므로 쿨다운 없음
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        // 패시브 능력이므로 직접 발동하지 않음
        // VeinMinerListener에서 처리
        player.sendMessage("§6[광역 채굴] §f이 능력은 자동으로 적용됩니다!");
        return false;
    }
}
