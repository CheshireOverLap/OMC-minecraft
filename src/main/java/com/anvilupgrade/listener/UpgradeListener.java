package com.anvilupgrade.listener;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.manager.UpgradeManager;
import com.anvilupgrade.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * 모루 강화 GUI 리스너
 */
public class UpgradeListener implements Listener {

    private final AnvilUpgrade plugin;
    private final UpgradeManager upgradeManager;
    
    public static final String GUI_TITLE = "§6장비 강화";
    public static final int ITEM_SLOT = 4;

    public UpgradeListener(AnvilUpgrade plugin) {
        this.plugin = plugin;
        this.upgradeManager = plugin.getUpgradeManager();
    }

    @EventHandler
    public void onAnvilInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.ANVIL 
            && event.getClickedBlock().getType() != Material.CHIPPED_ANVIL
            && event.getClickedBlock().getType() != Material.DAMAGED_ANVIL) return;

        Player player = event.getPlayer();
        
        // 강화 기능 비활성화 상태면 기본 모루 동작
        if (!plugin.isUpgradeEnabled(player)) {
            return; // 이벤트 취소하지 않고 기본 모루 사용
        }

        event.setCancelled(true);
        openUpgradeGUI(player);
    }

    private void openUpgradeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);

        // 배경 채우기
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 9; i++) {
            if (i != ITEM_SLOT) {
                gui.setItem(i, filler);
            }
        }

        // 중앙 플레이스홀더
        gui.setItem(ITEM_SLOT, createPlaceholder());

        player.openInventory(gui);
    }

    private ItemStack createPlaceholder() {
        ItemStack placeholder = new ItemStack(Material.ANVIL);
        ItemMeta meta = placeholder.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e강화할 아이템을 올려두세요");
            meta.setLore(Arrays.asList(
                "§7이곳에 강화할 장비를 놓으세요.",
                "",
                "§a좌클릭§7: 강화 시도",
                "§c우클릭§7: 아이템 회수"
            ));
            placeholder.setItemMeta(meta);
        }
        return placeholder;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInventory = event.getView().getTopInventory();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) return;

        if (clickedInventory.equals(topInventory)) {
            handleGuiClick(event, player, topInventory);
        } else {
            handlePlayerInventoryClick(event, player, topInventory);
        }
    }

    private void handleGuiClick(InventoryClickEvent event, Player player, Inventory gui) {
        if (event.getSlot() != ITEM_SLOT) return;

        ItemStack cursorItem = event.getCursor();
        ItemStack itemInSlot = gui.getItem(ITEM_SLOT);

        // 커서에 아이템이 있으면: 배치
        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
            if (isPlaceholder(itemInSlot)) {
                // 강화 가능한 아이템인지 확인
                if (!upgradeManager.getProfileRegistry().hasProfile(cursorItem.getType())) {
                    player.sendMessage("§c이 아이템은 강화할 수 없습니다.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
                    return;
                }
                
                gui.setItem(ITEM_SLOT, cursorItem.clone());
                player.setItemOnCursor(null);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.7f, 1.5f);
            }
            return;
        }

        // 슬롯에 아이템이 있으면
        if (!isPlaceholder(itemInSlot)) {
            if (event.isLeftClick()) {
                // 좌클릭: 강화 시도
                upgradeManager.attemptUpgrade(player, itemInSlot);
            } else if (event.isRightClick()) {
                // 우클릭: 아이템 회수
                InventoryUtils.giveOrDropItems(player, itemInSlot);
                gui.setItem(ITEM_SLOT, createPlaceholder());
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
            }
        }
    }

    private void handlePlayerInventoryClick(InventoryClickEvent event, Player player, Inventory gui) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (isPlaceholder(gui.getItem(ITEM_SLOT))) {
            // 강화 가능한 아이템인지 확인
            if (!upgradeManager.getProfileRegistry().hasProfile(clickedItem.getType())) {
                player.sendMessage("§c이 아이템은 강화할 수 없습니다.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
                return;
            }
            
            gui.setItem(ITEM_SLOT, clickedItem.clone());
            event.setCurrentItem(null);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.7f, 1.5f);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        
        ItemStack item = event.getInventory().getItem(ITEM_SLOT);
        if (!isPlaceholder(item) && item != null) {
            InventoryUtils.giveOrDropItems((Player) event.getPlayer(), item);
        }
    }

    private boolean isPlaceholder(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return true;
        return item.getType() == Material.ANVIL && item.hasItemMeta() 
            && item.getItemMeta().hasDisplayName();
    }
}
