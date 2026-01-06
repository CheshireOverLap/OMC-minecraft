package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AbsorptionAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public AbsorptionAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "absorption"; }

    @Override
    public String getDisplayName() { return "§e흡수"; }

    @Override
    public String getDescription() { return "§7일시적인 보호막을 얻습니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.absorption", 90.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        // 흡수 4 (8 추가 체력) 30초
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 3));
        // 저항 2 10초
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        player.sendMessage("§e보호막이 활성화되었습니다!");
        return true;
    }
}
