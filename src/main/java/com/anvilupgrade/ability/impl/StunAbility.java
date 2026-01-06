package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StunAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public StunAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "stun"; }

    @Override
    public String getDisplayName() { return "§c기절"; }

    @Override
    public String getDescription() { return "§7전방의 적을 기절시킵니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.stun", 20.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        // 전방 3블록 내 적에게 기절
        for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
            if (entity instanceof LivingEntity target && entity != player) {
                // 플레이어 시선 방향 체크
                if (player.getLocation().getDirection().dot(
                        target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize()) > 0.5) {
                    
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255)); // 3초 이동불가
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0)); // 3초 실명
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 255)); // 3초 공격불가
                    
                    target.getWorld().spawnParticle(Particle.FLASH, target.getLocation().add(0, 1, 0), 1);
                }
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 0.5f);
        return true;
    }
}
