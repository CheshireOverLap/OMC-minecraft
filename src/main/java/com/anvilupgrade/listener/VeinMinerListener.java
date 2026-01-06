package com.anvilupgrade.listener;

import com.anvilupgrade.AnvilUpgrade;
import com.anvilupgrade.manager.UpgradeManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * 10강 도구 3x3 블록 파괴 리스너
 */
public class VeinMinerListener implements Listener {

    private final AnvilUpgrade plugin;
    private final UpgradeManager upgradeManager;
    
    // 중복 처리 방지용
    private final Set<Player> processing = new HashSet<>();

    public VeinMinerListener(AnvilUpgrade plugin) {
        this.plugin = plugin;
        this.upgradeManager = plugin.getUpgradeManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // 중복 처리 방지
        if (processing.contains(player)) return;
        
        // 크리에이티브 모드 제외
        if (player.getGameMode() == GameMode.CREATIVE) return;
        
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;
        
        // 10강인지 확인
        int level = upgradeManager.getUpgradeLevel(tool);
        if (level < 10) return;
        
        // 도구 타입 확인
        if (!isValidTool(tool.getType())) return;
        
        Block centerBlock = event.getBlock();
        
        // 플레이어가 바라보는 방향에 따라 3x3 평면 결정
        BlockFace face = getTargetBlockFace(player);
        
        processing.add(player);
        
        try {
            breakBlocksInArea(player, centerBlock, face, tool);
        } finally {
            processing.remove(player);
        }
    }
    
    private boolean isValidTool(Material type) {
        String name = type.name().toLowerCase();
        return name.contains("pickaxe") || name.contains("shovel") || name.contains("hoe");
    }
    
    private BlockFace getTargetBlockFace(Player player) {
        // 플레이어 시선 방향으로 어떤 면을 부수는지 판단
        float pitch = player.getLocation().getPitch();
        
        // 위/아래를 보고 있으면 수평 평면 (XZ)
        if (pitch < -45) return BlockFace.DOWN; // 위를 봄 -> 아래 면
        if (pitch > 45) return BlockFace.UP;    // 아래를 봄 -> 위 면
        
        // 옆을 보고 있으면 수직 평면
        float yaw = player.getLocation().getYaw();
        yaw = (yaw % 360 + 360) % 360;
        
        if (yaw >= 315 || yaw < 45) return BlockFace.SOUTH;
        if (yaw >= 45 && yaw < 135) return BlockFace.WEST;
        if (yaw >= 135 && yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }
    
    private void breakBlocksInArea(Player player, Block center, BlockFace face, ItemStack tool) {
        int[][] offsets = getOffsets(face);
        
        for (int[] offset : offsets) {
            Block target = center.getRelative(offset[0], offset[1], offset[2]);
            
            // 중심 블록은 이미 부서짐
            if (target.equals(center)) continue;
            
            // 공기나 액체는 무시
            if (target.getType().isAir() || target.isLiquid()) continue;
            
            // 베드락 등 불가능한 블록 무시
            if (target.getType().getHardness() < 0) continue;
            
            // 도구로 캘 수 있는 블록인지 확인
            if (!canBreakWith(tool.getType(), target.getType())) continue;
            
            // 블록 파괴 이벤트 발생
            BlockBreakEvent breakEvent = new BlockBreakEvent(target, player);
            plugin.getServer().getPluginManager().callEvent(breakEvent);
            
            if (!breakEvent.isCancelled()) {
                target.breakNaturally(tool);
            }
        }
    }
    
    private int[][] getOffsets(BlockFace face) {
        return switch (face) {
            case UP, DOWN -> new int[][] {
                // XZ 평면 (수평)
                {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
                {-1, 0, 0},  {0, 0, 0},  {1, 0, 0},
                {-1, 0, 1},  {0, 0, 1},  {1, 0, 1}
            };
            case NORTH, SOUTH -> new int[][] {
                // XY 평면 (남북 방향 벽)
                {-1, -1, 0}, {0, -1, 0}, {1, -1, 0},
                {-1, 0, 0},  {0, 0, 0},  {1, 0, 0},
                {-1, 1, 0},  {0, 1, 0},  {1, 1, 0}
            };
            case EAST, WEST -> new int[][] {
                // YZ 평면 (동서 방향 벽)
                {0, -1, -1}, {0, -1, 0}, {0, -1, 1},
                {0, 0, -1},  {0, 0, 0},  {0, 0, 1},
                {0, 1, -1},  {0, 1, 0},  {0, 1, 1}
            };
            default -> new int[][] {{0, 0, 0}};
        };
    }
    
    private boolean canBreakWith(Material toolType, Material blockType) {
        String tool = toolType.name().toLowerCase();
        String block = blockType.name().toLowerCase();
        
        // 곡괭이
        if (tool.contains("pickaxe")) {
            return block.contains("stone") || block.contains("ore") || 
                   block.contains("brick") || block.contains("concrete") ||
                   block.contains("terracotta") || block.contains("basalt") ||
                   block.contains("blackstone") || block.contains("deepslate") ||
                   block.contains("copper") || block.contains("iron") ||
                   block.contains("gold") || block.contains("diamond") ||
                   block.contains("emerald") || block.contains("lapis") ||
                   block.contains("redstone") || block.contains("quartz") ||
                   block.contains("netherrack") || block.contains("end_stone") ||
                   block.contains("obsidian") || block.contains("amethyst") ||
                   block.contains("calcite") || block.contains("dripstone") ||
                   block.contains("tuff") || block.contains("andesite") ||
                   block.contains("diorite") || block.contains("granite") ||
                   block.contains("cobble") || block.contains("mossy");
        }
        
        // 삽
        if (tool.contains("shovel")) {
            return block.contains("dirt") || block.contains("grass") ||
                   block.contains("sand") || block.contains("gravel") ||
                   block.contains("clay") || block.contains("soul") ||
                   block.contains("snow") || block.contains("mud") ||
                   block.contains("mycelium") || block.contains("podzol") ||
                   block.contains("farmland") || block.contains("path") ||
                   block.contains("concrete_powder");
        }
        
        // 괭이
        if (tool.contains("hoe")) {
            return block.contains("hay") || block.contains("target") ||
                   block.contains("leaves") || block.contains("sculk") ||
                   block.contains("shroomlight") || block.contains("nether_wart") ||
                   block.contains("warped") || block.contains("crimson") ||
                   block.contains("moss") || block.contains("sponge") ||
                   block.contains("dried_kelp");
        }
        
        return false;
    }
}
