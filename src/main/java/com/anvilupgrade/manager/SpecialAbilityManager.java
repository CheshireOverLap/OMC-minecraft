package com.anvilupgrade.manager;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import com.anvilupgrade.ability.impl.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpecialAbilityManager {

    private final AnvilUpgrade plugin;
    private final Map<String, ISpecialAbility> registeredAbilities = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public SpecialAbilityManager(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    public void registerAbilities() {
        // 무기 능력
        registerAbility(new SwordDanceAbility(plugin));      // 검
        registerAbility(new StunAbility(plugin));            // 도끼
        registerAbility(new LaserShotAbility(plugin));       // 활
        registerAbility(new SuperchargeAbility(plugin));     // 쇠뇌
        registerAbility(new LightningSpearAbility(plugin));  // 삼지창
        registerAbility(new GrapplingHookAbility(plugin));   // 낚싯대
        registerAbility(new ShieldBashAbility(plugin));      // 방패
        
        // 방어구 능력
        registerAbility(new CleansingAbility(plugin));       // 투구
        registerAbility(new AbsorptionAbility(plugin));      // 흉갑
        registerAbility(new RegenerationAbility(plugin));    // 레깅스
        registerAbility(new DoubleJumpAbility(plugin));      // 부츠
        
        // 도구 능력
        registerAbility(new VeinMinerAbility(plugin));       // 곡괭이/삽/괭이
        
        plugin.getLogger().info("총 " + registeredAbilities.size() + "개의 특수 능력이 등록되었습니다.");
    }

    public void registerAbility(ISpecialAbility ability) {
        registeredAbilities.put(ability.getInternalName(), ability);
    }

    public ISpecialAbility getRegisteredAbility(String internalName) {
        return registeredAbilities.get(internalName);
    }

    public Collection<ISpecialAbility> getAllAbilities() {
        return registeredAbilities.values();
    }

    // ============ 쿨다운 관리 ============

    public void setCooldown(Player player, String key, double seconds, String displayName) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(key, System.currentTimeMillis() + (long)(seconds * 1000));
    }

    public long getRemainingCooldown(Player player, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;
        
        Long endTime = playerCooldowns.get(key);
        if (endTime == null) return 0;
        
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public boolean isOnCooldown(Player player, String key) {
        return getRemainingCooldown(player, key) > 0;
    }

    // ============ 정리 ============

    public void cleanupAllActiveAbilities() {
        for (ISpecialAbility ability : registeredAbilities.values()) {
            ability.cleanup();
        }
    }

    public void saveAllData() {
        // 필요시 데이터 저장
    }
}
