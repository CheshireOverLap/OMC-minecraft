package com.anvilupgrade.manager;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import com.anvilupgrade.item.UpgradeItems;
import com.anvilupgrade.profile.IUpgradeableProfile;
import com.anvilupgrade.profile.ProfileRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * 강화 시스템의 핵심 로직을 담당하는 매니저
 */
public class UpgradeManager {

    private final AnvilUpgrade plugin;
    private final ProfileRegistry profileRegistry;
    private final Random random = new Random();
    
    private static final String PREFIX = "§6[강화] §f";
    public static final int MAX_UPGRADE_LEVEL = 10;
    
    // PDC 키
    public static final NamespacedKey ITEM_UUID_KEY;
    public static final NamespacedKey SPECIAL_ABILITY_KEY;
    
    static {
        ITEM_UUID_KEY = new NamespacedKey(AnvilUpgrade.getInstance(), "item_uuid");
        SPECIAL_ABILITY_KEY = new NamespacedKey(AnvilUpgrade.getInstance(), "special_ability");
    }

    public UpgradeManager(AnvilUpgrade plugin) {
        this.plugin = plugin;
        this.profileRegistry = new ProfileRegistry();
    }

    /**
     * 아이템의 강화 레벨을 반환합니다.
     */
    public int getUpgradeLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getLore() != null) {
            for (String line : meta.getLore()) {
                if (line.contains("★") || line.contains("☆")) {
                    int level = 0;
                    for (char c : ChatColor.stripColor(line).toCharArray()) {
                        if (c == '★') level++;
                    }
                    return level;
                }
            }
        }
        return 0;
    }

    /**
     * 강화를 시도합니다.
     */
    public void attemptUpgrade(Player player, ItemStack item) {
        // 1. 프로필 확인
        IUpgradeableProfile profile = profileRegistry.getProfile(item.getType());
        if (profile == null) {
            player.sendMessage(PREFIX + "§c이 아이템은 강화할 수 없습니다.");
            return;
        }

        // 2. 현재 레벨 확인
        final int currentLevel = getUpgradeLevel(item);
        if (currentLevel >= MAX_UPGRADE_LEVEL) {
            player.sendMessage(PREFIX + "§c최대 강화 레벨에 도달했습니다.");
            return;
        }

        // 3. 강화석 확인 (필요 개수: 현재 레벨 + 1)
        int requiredStones = currentLevel + 1;
        if (!hasEnoughStones(player, requiredStones)) {
            player.sendMessage(PREFIX + "§c강화석이 부족합니다! (필요: " + requiredStones + "개)");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.8f);
            return;
        }

        // 4. 강화석 소모
        consumeStones(player, requiredStones);

        // 5. 확률 계산
        FileConfiguration config = plugin.getConfig();
        String path = "level-settings." + currentLevel;

        if (!config.isConfigurationSection(path)) {
            player.sendMessage(PREFIX + "§c강화 설정을 찾을 수 없습니다.");
            player.getInventory().addItem(UpgradeItems.createUpgradeStone(requiredStones));
            return;
        }

        double successChance = config.getDouble(path + ".success", 0.0);
        double failureChance = config.getDouble(path + ".failure", 0.0);
        double downgradeChance = config.getDouble(path + ".downgrade", 0.0);
        double destroyChance = config.getDouble(path + ".destroy", 0.0);

        double totalChance = successChance + failureChance + downgradeChance + destroyChance;
        if (totalChance <= 0) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            return;
        }

        double roll = random.nextDouble() * totalChance;

        // 6. 결과 처리
        if (roll < successChance) {
            // 성공
            int newLevel = currentLevel + 1;
            setUpgradeLevel(item, newLevel);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.5f);
            
            if (newLevel == MAX_UPGRADE_LEVEL) {
                handleLegendaryUpgrade(player, item);
            }
        } else if (roll < successChance + failureChance) {
            // 실패 (유지)
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 1.0f);
        } else if (roll < successChance + failureChance + downgradeChance) {
            // 등급 하락
            int newLevel = Math.max(0, currentLevel - 1);
            setUpgradeLevel(item, newLevel);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 1.5f);
        } else {
            // 파괴
            handleDestroy(player, item, currentLevel);
        }
    }

    /**
     * 아이템의 강화 레벨을 설정합니다.
     */
    public void setUpgradeLevel(ItemStack item, int level) {
        IUpgradeableProfile profile = profileRegistry.getProfile(item.getType());
        if (profile == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 0. 기존 vanilla 속성 백업 (anvilupgrade 네임스페이스가 아닌 것들)
        Map<Attribute, List<AttributeModifier>> vanillaModifiers = backupVanillaModifiers(meta);

        // 1. 이름 설정
        meta.setDisplayName(profile.generateDisplayName(item, level));

        // 2. 로어 생성
        List<String> lore = new ArrayList<>();
        
        // 별 표시
        lore.add(generateStarLine(level));
        lore.add("");
        
        // 보너스 정보
        Map<Attribute, Double> bonuses = profile.getLevelBonuses();
        for (Map.Entry<Attribute, Double> entry : bonuses.entrySet()) {
            String attrName = getAttributeName(entry.getKey());
            double totalBonus = entry.getValue() * level;
            lore.add("§a+" + String.format("%.1f", totalBonus) + " " + attrName);
        }
        
        // 다음 강화 확률
        if (level < MAX_UPGRADE_LEVEL) {
            lore.add("");
            lore.addAll(generateChanceLore(level));
        }
        
        // 10강 특수 능력
        if (level >= MAX_UPGRADE_LEVEL) {
            profile.getSpecialAbility().ifPresent(ability -> {
                lore.add("");
                lore.add("§5[특수능력] §f" + ability.getDisplayName());
                lore.add("§7" + ability.getDescription());
            });
        }

        meta.setLore(lore);

        // 3. 속성 적용
        profile.applyAttributes(meta, level);
        profile.applyEnchantments(meta, level);
        
        // 4. 백업한 vanilla 속성 복원
        restoreVanillaModifiers(meta, vanillaModifiers);

        // 5. PDC 업데이트
        updatePersistentData(meta, profile, level);

        item.setItemMeta(meta);
    }
    
    /**
     * vanilla (non-anvilupgrade) attribute modifier를 백업합니다.
     */
    private Map<Attribute, List<AttributeModifier>> backupVanillaModifiers(ItemMeta meta) {
        Map<Attribute, List<AttributeModifier>> backup = new HashMap<>();
        
        // 모든 attribute 체크
        for (String attrKey : new String[]{"attack_damage", "attack_speed", "armor", "armor_toughness", 
                "knockback_resistance", "max_health", "movement_speed"}) {
            Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(attrKey));
            if (attr == null) continue;
            
            Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attr);
            if (modifiers == null) continue;
            
            List<AttributeModifier> vanillaList = new ArrayList<>();
            for (AttributeModifier mod : modifiers) {
                // anvilupgrade 네임스페이스가 아닌 것만 백업
                if (!mod.getKey().getNamespace().equals("anvilupgrade")) {
                    vanillaList.add(mod);
                }
            }
            
            if (!vanillaList.isEmpty()) {
                backup.put(attr, vanillaList);
            }
        }
        
        return backup;
    }
    
    /**
     * 백업한 vanilla modifier를 복원합니다.
     */
    private void restoreVanillaModifiers(ItemMeta meta, Map<Attribute, List<AttributeModifier>> backup) {
        for (Map.Entry<Attribute, List<AttributeModifier>> entry : backup.entrySet()) {
            for (AttributeModifier mod : entry.getValue()) {
                // 이미 있는지 체크
                Collection<AttributeModifier> existing = meta.getAttributeModifiers(entry.getKey());
                boolean exists = false;
                if (existing != null) {
                    for (AttributeModifier ex : existing) {
                        if (ex.getKey().equals(mod.getKey())) {
                            exists = true;
                            break;
                        }
                    }
                }
                
                if (!exists) {
                    meta.addAttributeModifier(entry.getKey(), mod);
                }
            }
        }
    }

    private String generateStarLine(int level) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < MAX_UPGRADE_LEVEL; i++) {
            if (i < level) {
                stars.append("§e★");
            } else {
                stars.append("§7☆");
            }
        }
        return stars.toString();
    }

    private List<String> generateChanceLore(int level) {
        List<String> lore = new ArrayList<>();
        FileConfiguration config = plugin.getConfig();
        
        if (!config.getBoolean("show-success-chance", true)) {
            return lore;
        }
        
        String path = "level-settings." + level;
        if (config.isConfigurationSection(path)) {
            double success = config.getDouble(path + ".success", 0.0) * 100;
            double failure = config.getDouble(path + ".failure", 0.0) * 100;
            double downgrade = config.getDouble(path + ".downgrade", 0.0) * 100;
            double destroy = config.getDouble(path + ".destroy", 0.0) * 100;

            lore.add("§a성공: " + String.format("%.1f", success) + "%");
            lore.add("§e유지: " + String.format("%.1f", failure) + "%");
            lore.add("§c하락: " + String.format("%.1f", downgrade) + "%");
            lore.add("§4파괴: " + String.format("%.1f", destroy) + "%");
        }
        
        return lore;
    }

    private void updatePersistentData(ItemMeta meta, IUpgradeableProfile profile, int level) {
        if (level < MAX_UPGRADE_LEVEL) {
            meta.getPersistentDataContainer().remove(SPECIAL_ABILITY_KEY);
        } else {
            profile.getSpecialAbility().ifPresent(ability -> {
                meta.getPersistentDataContainer().set(
                    SPECIAL_ABILITY_KEY, PersistentDataType.STRING, ability.getInternalName()
                );
            });
            
            if (!meta.getPersistentDataContainer().has(ITEM_UUID_KEY, PersistentDataType.STRING)) {
                meta.getPersistentDataContainer().set(
                    ITEM_UUID_KEY, PersistentDataType.STRING, UUID.randomUUID().toString()
                );
            }
        }
    }

    private void handleLegendaryUpgrade(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String legendaryName = meta.getDisplayName();
        
        Component hoverableItemName = LegacyComponentSerializer.legacySection()
            .deserialize(legendaryName)
            .hoverEvent(item.asHoverEvent());

        Component broadcastMessage = Component.text()
            .color(NamedTextColor.GOLD)
            .append(Component.text("[!] "))
            .append(hoverableItemName)
            .append(Component.text("이(가) 탄생했습니다!"))
            .build();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(broadcastMessage);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.0f);
        }
    }

    private void handleDestroy(Player player, ItemStack item, int currentLevel) {
        ItemStack destroyedItem = item.clone();
        String itemName = destroyedItem.getItemMeta() != null && destroyedItem.getItemMeta().hasDisplayName() 
            ? destroyedItem.getItemMeta().getDisplayName() 
            : destroyedItem.getType().name();

        Component hoverableItemName = LegacyComponentSerializer.legacySection()
            .deserialize(itemName)
            .hoverEvent(destroyedItem.asHoverEvent());

        Component broadcastMessage = Component.text()
            .append(Component.text("[!] ", NamedTextColor.DARK_RED))
            .append(Component.text("한 ", NamedTextColor.GRAY))
            .append(hoverableItemName)
            .append(Component.text(" (+" + currentLevel + ")", NamedTextColor.DARK_RED))
            .append(Component.text(" 아이템이 강화에 실패하여 파괴되었습니다.", NamedTextColor.GRAY))
            .build();
            
        Bukkit.broadcast(broadcastMessage);
        
        item.setAmount(0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.0f);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.05);
    }

    private boolean hasEnoughStones(Player player, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && UpgradeItems.isUpgradeStone(item)) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    private void consumeStones(Player player, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && UpgradeItems.isUpgradeStone(item)) {
                int toTake = Math.min(remaining, item.getAmount());
                item.setAmount(item.getAmount() - toTake);
                remaining -= toTake;
                if (item.getAmount() <= 0) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }

    private String getAttributeName(Attribute attribute) {
        String key = attribute.getKey().getKey();
        return switch (key) {
            case "attack_speed" -> "공격 속도";
            case "attack_damage" -> "공격력";
            case "armor" -> "방어력";
            case "armor_toughness" -> "방어 강도";
            case "max_health" -> "최대 체력";
            case "movement_speed" -> "이동 속도";
            case "knockback_resistance" -> "넉백 저항";
            default -> key;
        };
    }

    public ProfileRegistry getProfileRegistry() {
        return profileRegistry;
    }
}
