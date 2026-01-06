package com.anvilupgrade;

import com.anvilupgrade.item.UpgradeItems;
import com.anvilupgrade.listener.DurabilityListener;
import com.anvilupgrade.listener.SpecialAbilityListener;
import com.anvilupgrade.listener.UpgradeListener;
import com.anvilupgrade.listener.VeinMinerListener;
import com.anvilupgrade.listener.passive.*;
import com.anvilupgrade.manager.SpecialAbilityManager;
import com.anvilupgrade.manager.UpgradeManager;
import com.anvilupgrade.util.ActionBarManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AnvilUpgrade extends JavaPlugin {

    private static AnvilUpgrade instance;
    
    private UpgradeManager upgradeManager;
    private SpecialAbilityManager specialAbilityManager;
    private ActionBarManager actionBarManager;
    
    // 강화 비활성화한 플레이어 목록
    private final Set<UUID> disabledPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        getLogger().info("AnvilUpgrade 플러그인을 활성화합니다...");
        
        // 매니저 초기화
        initializeManagers();
        
        // 리스너 등록
        registerListeners();
        
        // 조합법 등록
        registerRecipes();
        
        getLogger().info("AnvilUpgrade 플러그인이 활성화되었습니다!");
    }
    
    private void initializeManagers() {
        specialAbilityManager = new SpecialAbilityManager(this);
        upgradeManager = new UpgradeManager(this);
        actionBarManager = new ActionBarManager(this, specialAbilityManager);
        
        // 특수 능력 등록
        specialAbilityManager.registerAbilities();
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new UpgradeListener(this), this);
        getServer().getPluginManager().registerEvents(new SpecialAbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new DurabilityListener(this), this);
        getServer().getPluginManager().registerEvents(new VeinMinerListener(this), this);
        
        // 패시브 리스너
        getServer().getPluginManager().registerEvents(new BowPassiveListener(this), this);
        getServer().getPluginManager().registerEvents(new CrossbowPassiveListener(this), this);
        getServer().getPluginManager().registerEvents(new TridentPassiveListener(this), this);
        getServer().getPluginManager().registerEvents(new FishingRodPassiveListener(this), this);
    }
    
    private void registerRecipes() {
        // 강화석 조합법
        int outputAmount = getConfig().getInt("recipe-output-amount", 8);
        if (outputAmount > 0) {
            NamespacedKey key = new NamespacedKey(this, "upgrade_stone_recipe");
            Bukkit.removeRecipe(key);
            
            ItemStack result = UpgradeItems.createUpgradeStone(outputAmount);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(" D ", "DAD", " D ");
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('A', Material.AMETHYST_SHARD);
            
            Bukkit.addRecipe(recipe);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AnvilUpgrade 플러그인을 비활성화합니다...");
        
        if (specialAbilityManager != null) {
            specialAbilityManager.cleanupAllActiveAbilities();
            specialAbilityManager.saveAllData();
        }
        
        getLogger().info("AnvilUpgrade 플러그인이 비활성화되었습니다.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("upgrade")) return false;
        
        if (args.length == 0) {
            sender.sendMessage("§6[AnvilUpgrade] §f사용법:");
            sender.sendMessage("§e/upgrade on §f- 강화 기능 활성화");
            sender.sendMessage("§e/upgrade off §f- 강화 기능 비활성화");
            sender.sendMessage("§e/upgrade give [개수] §f- 강화석 지급 (관리자)");
            sender.sendMessage("§e/upgrade reload §f- 설정 리로드 (관리자)");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "on" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c플레이어만 사용할 수 있습니다.");
                    return true;
                }
                disabledPlayers.remove(player.getUniqueId());
                player.sendMessage("§6[AnvilUpgrade] §a강화 기능이 활성화되었습니다.");
                player.sendMessage("§7모루를 우클릭하면 강화 GUI가 열립니다.");
            }
            case "off" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c플레이어만 사용할 수 있습니다.");
                    return true;
                }
                disabledPlayers.add(player.getUniqueId());
                player.sendMessage("§6[AnvilUpgrade] §c강화 기능이 비활성화되었습니다.");
                player.sendMessage("§7모루를 우클릭해도 강화 GUI가 열리지 않습니다.");
            }
            case "give" -> {
                if (!sender.hasPermission("anvilupgrade.admin")) {
                    sender.sendMessage("§c권한이 없습니다.");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c플레이어만 사용할 수 있습니다.");
                    return true;
                }
                int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
                player.getInventory().addItem(UpgradeItems.createUpgradeStone(amount));
                player.sendMessage("§6[AnvilUpgrade] §f강화석 " + amount + "개를 지급받았습니다.");
            }
            case "reload" -> {
                if (!sender.hasPermission("anvilupgrade.admin")) {
                    sender.sendMessage("§c권한이 없습니다.");
                    return true;
                }
                reloadConfig();
                sender.sendMessage("§6[AnvilUpgrade] §f설정을 다시 불러왔습니다.");
            }
            default -> {
                sender.sendMessage("§6[AnvilUpgrade] §f/upgrade <on|off|give|reload>");
            }
        }
        
        return true;
    }
    
    /**
     * 플레이어의 강화 기능 활성화 여부 확인
     */
    public boolean isUpgradeEnabled(Player player) {
        return !disabledPlayers.contains(player.getUniqueId());
    }

    // Getters
    public static AnvilUpgrade getInstance() { return instance; }
    public UpgradeManager getUpgradeManager() { return upgradeManager; }
    public SpecialAbilityManager getSpecialAbilityManager() { return specialAbilityManager; }
    public ActionBarManager getActionBarManager() { return actionBarManager; }
}
