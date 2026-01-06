package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class CleansingAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;
    private static final List<PotionEffectType> DEBUFFS = Arrays.asList(
            PotionEffectType.POISON, PotionEffectType.WITHER,
            PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS,
            PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA,
            PotionEffectType.HUNGER, PotionEffectType.MINING_FATIGUE
    );

    public CleansingAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "cleansing"; }

    @Override
    public String getDisplayName() { return "§a정화"; }

    @Override
    public String getDescription() { return "§7모든 디버프를 제거합니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.cleansing", 60.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        boolean hadDebuff = false;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (DEBUFFS.contains(effect.getType())) {
                player.removePotionEffect(effect.getType());
                hadDebuff = true;
            }
        }

        if (!hadDebuff) {
            player.sendMessage("§a제거할 디버프가 없습니다.");
            return false;
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 2.0f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage("§a모든 디버프가 정화되었습니다!");
        return true;
    }
}
