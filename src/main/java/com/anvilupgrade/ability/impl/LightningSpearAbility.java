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
import org.bukkit.util.RayTraceResult;

public class LightningSpearAbility implements ISpecialAbility {

    private final AnvilUpgrade plugin;

    public LightningSpearAbility(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getInternalName() { return "lightning_spear"; }

    @Override
    public String getDisplayName() { return "§b번개창"; }

    @Override
    public String getDescription() { return "§7바라보는 곳에 번개를 내립니다."; }

    @Override
    public double getCooldownSeconds() {
        return plugin.getConfig().getDouble("ability-cooldowns.lightning_spear", 30.0);
    }

    @Override
    public boolean activate(Player player, ItemStack item) {
        if (plugin.getSpecialAbilityManager().isOnCooldown(player, getInternalName())) {
            return false;
        }

        RayTraceResult result = player.rayTraceBlocks(30);
        Location strikeLocation;
        
        if (result != null && result.getHitBlock() != null) {
            strikeLocation = result.getHitPosition().toLocation(player.getWorld());
        } else {
            strikeLocation = player.getLocation().add(player.getLocation().getDirection().multiply(30));
        }

        plugin.getSpecialAbilityManager().setCooldown(player, getInternalName(), getCooldownSeconds(), getDisplayName());

        // 번개 소환
        player.getWorld().strikeLightningEffect(strikeLocation);
        
        // 범위 데미지
        for (Entity entity : strikeLocation.getWorld().getNearbyEntities(strikeLocation, 3, 3, 3)) {
            if (entity instanceof LivingEntity target && entity != player) {
                target.damage(20.0, player);
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation(), 30, 0.5, 1, 0.5, 0.1);
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
        return true;
    }
}
