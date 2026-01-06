package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class LaserShotAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public LaserShotAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "laser_shot"; }

    @Override
    public String getDisplayName() { return "§c레이저 샷"; }

    @Override
    public String getDescription() { return "§7관통하는 레이저를 발사합니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.laser_shot", 25.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        Vector direction = player.getLocation().getDirection();
        Location start = player.getEyeLocation();

        new BukkitRunnable() {
            double distance = 0;
            final double maxDistance = 50;

            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    distance += 0.5;
                    if (distance > maxDistance) {
                        this.cancel();
                        return;
                    }

                    Location point = start.clone().add(direction.clone().multiply(distance));
                    point.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);

                    // 적 체크
                    for (Entity entity : point.getWorld().getNearbyEntities(point, 0.5, 0.5, 0.5)) {
                        if (entity instanceof LivingEntity target && entity != player) {
                            target.damage(15.0, player);
                            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation(), 20, 0.3, 0.5, 0.3, 0.05);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 2.0f, 2.0f);
        return true;
    }
}
