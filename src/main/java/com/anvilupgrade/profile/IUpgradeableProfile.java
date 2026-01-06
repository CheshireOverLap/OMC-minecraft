package com.anvilupgrade.profile;

import com.anvilupgrade.ability.ISpecialAbility;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Optional;

/**
 * 강화 가능한 아이템의 프로필 인터페이스
 */
public interface IUpgradeableProfile {

    /**
     * 아이템의 강화 등급에 따른 표시 이름을 생성합니다.
     * @param originalItem 원본 아이템
     * @param level 강화 레벨
     * @return 강화된 아이템 이름
     */
    String generateDisplayName(ItemStack originalItem, int level);

    /**
     * 아이템의 속성(Attribute)을 강화 레벨에 맞게 적용합니다.
     * @param meta 아이템 메타
     * @param level 강화 레벨
     */
    void applyAttributes(ItemMeta meta, int level);

    /**
     * 강화 레벨에 따른 인챈트를 적용합니다.
     * @param meta 아이템 메타
     * @param level 강화 레벨
     */
    default void applyEnchantments(ItemMeta meta, int level) {
        // 기본 구현: 아무것도 하지 않음
    }

    /**
     * 이 프로필이 제공하는 레벨당 보너스 정보를 반환합니다.
     * 로어(Lore) 생성에 사용됩니다.
     * @return 속성과 레벨당 보너스 값의 맵
     */
    Map<Attribute, Double> getLevelBonuses();

    /**
     * 10강 달성 시 해금되는 특수 능력을 반환합니다.
     * @return 특수 능력 (없으면 Optional.empty())
     */
    default Optional<ISpecialAbility> getSpecialAbility() {
        return Optional.empty();
    }
}
