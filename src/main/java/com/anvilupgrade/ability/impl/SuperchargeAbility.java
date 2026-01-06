package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SuperchargeAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public SuperchargeAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "supercharge"; }

    @Override
    public String getDisplayName() { return "§e과충전"; }

    @Override
    public String getDescription() { return "§7폭발하는 화살을 발사합니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.supercharge", 45.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        // 폭발 효과
        Vector direction = player.getLocation().getDirection();
        player.getWorld().createExplosion(
                player.getLocation().add(direction.multiply(3)),
                3.0f, false, false, player
        );

        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity target && entity != player) {
                target.damage(12.0, player);
                Vector knockback = target.getLocation().toVector()
                        .subtract(player.getLocation().toVector())
                        .normalize().multiply(1.5);
                knockback.setY(0.5);
                target.setVelocity(knockback);
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 5);
        return true;
    }
}
