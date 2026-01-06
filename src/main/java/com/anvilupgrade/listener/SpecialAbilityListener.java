package com.anvilupgrade.listener;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.ability.ISpecialAbility;
import com.anvilupgrade.manager.UpgradeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * 특수 능력 발동 리스너
 */
public class SpecialAbilityListener implements Listener {

    private final AnvilUpgrade plugin;

    public SpecialAbilityListener(AnvilUpgrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!event.getPlayer().isSneaking()) return;

        Player player = event.getPlayer();
        ItemStack item = findItemWithAbility(player);

        if (item == null) return;

        String abilityKey = item.getItemMeta().getPersistentDataContainer()
            .get(UpgradeManager.SPECIAL_ABILITY_KEY, PersistentDataType.STRING);

        ISpecialAbility ability = plugin.getSpecialAbilityManager().getRegisteredAbility(abilityKey);
        if (ability != null && ability.canActivate(player, item)) {
            if (ability.activate(player, item)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * 특수 능력이 있는 아이템을 찾습니다. (메인핸드 → 오프핸드 → 방어구 순서)
     */
    private ItemStack findItemWithAbility(Player player) {
        // 1. 메인핸드 체크
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (hasSpecialAbility(mainHand)) {
            return mainHand;
        }

        // 2. 오프핸드 체크 (방패 등)
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (hasSpecialAbility(offHand)) {
            return offHand;
        }

        // 3. 방어구 슬롯 체크 (부츠 등)
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (hasSpecialAbility(armor)) {
                return armor;
            }
        }

        return null;
    }

    private boolean hasSpecialAbility(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(UpgradeManager.SPECIAL_ABILITY_KEY, PersistentDataType.STRING);
    }
}
