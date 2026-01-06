package com.anvilupgrade.ability;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 10강 장비의 특수 능력 인터페이스
 */
public interface ISpecialAbility {

    /**
     * 능력의 내부 식별자를 반환합니다.
     */
    String getInternalName();

    /**
     * 플레이어에게 표시할 능력 이름을 반환합니다.
     */
    String getDisplayName();

    /**
     * 능력에 대한 설명을 반환합니다.
     */
    String getDescription();

    /**
     * 능력을 활성화합니다.
     * @param player 플레이어
     * @param item 해당 아이템
     * @return 능력 사용 성공 여부
     */
    boolean activate(Player player, ItemStack item);

    /**
     * 능력 사용 조건을 확인합니다.
     * @param player 플레이어
     * @param item 해당 아이템
     * @return 사용 가능 여부
     */
    default boolean canActivate(Player player, ItemStack item) {
        return true;
    }

    /**
     * 쿨다운 시간을 반환합니다. (초)
     */
    default double getCooldownSeconds() {
        return 30.0;
    }

    /**
     * 액션바에 쿨다운을 표시할지 여부
     */
    default boolean showInActionBar() {
        return true;
    }

    /**
     * 충전 기반 능력인지 여부
     */
    default boolean isChargeBased() {
        return false;
    }

    /**
     * 최대 충전 횟수 (충전 기반인 경우)
     */
    default int getMaxCharges() {
        return 1;
    }

    /**
     * 충전 복구 시간 (초, 충전 기반인 경우)
     */
    default double getChargeRegenSeconds() {
        return getCooldownSeconds();
    }

    /**
     * 능력 정리 (플러그인 비활성화 시)
     */
    default void cleanup() {}
}
