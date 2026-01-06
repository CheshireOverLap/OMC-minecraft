package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * 검무 - 주변 적에게 연속 공격
 */
public class SwordDanceAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;
    private static final double RADIUS = 4.0;
    private static final double DAMAGE = 6.0;
    private static final int HITS = 5;

    public SwordDanceAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() {
        return "sword_dance";
    }

    @Override
    public String getDisplayName() {
        return "§c검무";
    }

    @Override
    public String getDescription() {
        return "§7주변 적에게 회전 공격을 가합니다.";
    }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.sword_dance", 30.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        // 쿨다운 확인
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        // 쿨다운 설정
        plugin.getSpecialAbilityManager().setCooldown(
            player, getInternalName(), getCooldownSeconds(), getDisplayName()
        );

        // 효과 실행
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
        
        new BukkitRunnable() {
            int hits = 0;
            double angle = 0;

            @Override
            public void run() {
                if (hits >= HITS || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                // 파티클 효과
                for (int i = 0; i < 12; i++) {
                    double particleAngle = angle + (i * Math.PI / 6);
                    double x = Math.cos(particleAngle) * RADIUS;
                    double z = Math.sin(particleAngle) * RADIUS;
                    player.getWorld().spawnParticle(
                        Particle.SWEEP_ATTACK,
                        player.getLocation().add(x, 1, z),
                        1, 0, 0, 0, 0
                    );
                }

                // 범위 내 적 공격
                for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                    if (entity instanceof LivingEntity target && entity != player) {
                        target.damage(DAMAGE, player);
                        
                        // 넉백
                        Vector knockback = target.getLocation().toVector()
                            .subtract(player.getLocation().toVector())
                            .normalize()
                            .multiply(0.5);
                        knockback.setY(0.2);
                        target.setVelocity(target.getVelocity().add(knockback));
                    }
                }

                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 1.0f + (hits * 0.1f));
                
                angle += Math.PI / 3;
                hits++;
            }
        }.runTaskTimer(plugin, 0L, 4L);

        return true;
    }
}
