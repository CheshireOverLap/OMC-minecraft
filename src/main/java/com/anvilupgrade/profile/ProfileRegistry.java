package com.anvilupgrade.profile;

import com.anvilupgrade.profile.type.*;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * 아이템 타입별 강화 프로필을 관리하는 레지스트리
 */
public class ProfileRegistry {

    private final Map<Material, IUpgradeableProfile> profiles = new HashMap<>();

    public ProfileRegistry() {
        registerAllProfiles();
    }

    private void registerAllProfiles() {
        // 검 프로필
        registerToolSet(new SwordProfile(),
                Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
                Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);

        // 도끼 프로필
        registerToolSet(new AxeProfile(),
                Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
                Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE);

        // 곡괭이 프로필
        registerToolSet(new PickaxeProfile(),
                Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
                Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE);

        // 삽 프로필
        registerToolSet(new ShovelProfile(),
                Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
                Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL);

        // 괭이 프로필
        registerToolSet(new HoeProfile(),
                Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
                Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE);

        // 헬멧 프로필
        registerArmorSet(new HelmetProfile(),
                Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET,
                Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
                Material.TURTLE_HELMET);

        // 흉갑 프로필
        registerArmorSet(new ChestplateProfile(),
                Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE,
                Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE);

        // 레깅스 프로필
        registerArmorSet(new LeggingsProfile(),
                Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS,
                Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS);

        // 부츠 프로필
        registerArmorSet(new BootsProfile(),
                Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS,
                Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS);

        // 특수 장비
        profiles.put(Material.SHIELD, new ShieldProfile());
        profiles.put(Material.BOW, new BowProfile());
        profiles.put(Material.CROSSBOW, new CrossbowProfile());
        profiles.put(Material.TRIDENT, new TridentProfile());
        profiles.put(Material.FISHING_ROD, new FishingRodProfile());
    }

    private void registerToolSet(IUpgradeableProfile profile, Material... materials) {
        for (Material material : materials) {
            profiles.put(material, profile);
        }
    }

    private void registerArmorSet(IUpgradeableProfile profile, Material... materials) {
        for (Material material : materials) {
            profiles.put(material, profile);
        }
    }

    /**
     * 아이템 타입에 해당하는 프로필을 반환합니다.
     * @param material 아이템 타입
     * @return 프로필 (없으면 null)
     */
    public IUpgradeableProfile getProfile(Material material) {
        return profiles.get(material);
    }

    /**
     * 프로필 존재 여부를 확인합니다.
     * @param material 아이템 타입
     * @return 프로필 존재 여부
     */
    public boolean hasProfile(Material material) {
        return profiles.containsKey(material);
    }
}
