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

public class ShieldBashAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public ShieldBashAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "shield_bash"; }

    @Override
    public String getDisplayName() { return "§e쉴드 배쉬"; }

    @Override
    public String getDescription() { return "§7방패로 적을 밀쳐냅니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.shield_bash", 15.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        Vector direction = player.getLocation().getDirection();
        
        for (Entity entity : player.getNearbyEntities(3, 2, 3)) {
            if (entity instanceof LivingEntity target && entity != player) {
                Vector knockback = direction.clone().multiply(2.5);
                knockback.setY(0.5);
                target.setVelocity(knockback);
                target.damage(4.0, player);
                
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation().add(direction), 3);
        return true;
    }
}
