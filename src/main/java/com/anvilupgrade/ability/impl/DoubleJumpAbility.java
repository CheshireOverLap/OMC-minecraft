package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class DoubleJumpAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public DoubleJumpAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "double_jump"; }

    @Override
    public String getDisplayName() { return "§b더블 점프"; }

    @Override
    public String getDescription() { return "§7공중에서 한 번 더 점프합니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.double_jump", 5.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        if (player.isOnGround()) {
            return false; // 공중에서만 사용 가능
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        Vector velocity = player.getLocation().getDirection().multiply(0.5);
        velocity.setY(0.8);
        player.setVelocity(velocity);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.3, 0.1, 0.3, 0.05);
        return true;
    }
}
