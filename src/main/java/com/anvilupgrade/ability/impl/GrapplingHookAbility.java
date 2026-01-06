package com.anvilupgrade.ability.impl;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class GrapplingHookAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public GrapplingHookAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "grappling_hook"; }

    @Override
    public String getDisplayName() { return "§a그래플링 훅"; }

    @Override
    public String getDescription() { return "§7바라보는 곳으로 끌려갑니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.grappling_hook", 10.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        // 레이트레이스로 블록 찾기
        RayTraceResult result = player.rayTraceBlocks(50);
        if (result == null || result.getHitBlock() == null) {
            player.sendMessage("§c그래플링 훅을 걸 곳이 없습니다!");
            return false;
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        Location target = result.getHitPosition().toLocation(player.getWorld());
        Vector direction = target.toVector().subtract(player.getLocation().toVector()).normalize();
        
        double distance = player.getLocation().distance(target);
        double power = Math.min(distance / 10, 3.0);
        
        player.setVelocity(direction.multiply(power));
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.FISHING, player.getLocation(), 10);
        return true;
    }
}
