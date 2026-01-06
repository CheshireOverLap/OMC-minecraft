# AnvilUpgrade

모루 기반 장비 강화 시스템 마인크래프트 플러그인

## 요구 사항

- Paper 1.21.4+
- Java 21+

## 설치

1. [Releases](https://github.com/CheshireOverLap/OMC-minecraft/releases)에서 JAR 파일 다운로드
2. 서버의 `plugins` 폴더에 복사
3. 서버 재시작

## 기능

### 강화 시스템
- 모루를 **Shift + 우클릭**하여 강화 GUI 오픈
- 최대 **10강**까지 강화 가능
- 강화석 필요 (현재 레벨 + 1개)
- 확률 기반: 성공 / 유지 / 하락 / 파괴

### 강화석 조합법
```
    D
  D A D
    D

D = 다이아몬드
A = 자수정 조각
```

### 지원 장비
| 종류 | 아이템 |
|------|--------|
| 무기 | 검, 도끼, 활, 쇠뇌, 삼지창, 낚싯대 |
| 방어구 | 투구, 흉갑, 레깅스, 부츠 |
| 도구 | 곡괭이, 삽, 괭이 |
| 기타 | 방패 |

## 특수 능력 (10강)

10강 달성 시 장비별 특수 능력이 해금됩니다. **Shift + 우클릭**으로 발동.

| 장비 | 능력 | 설명 | 쿨타임 |
|------|------|------|--------|
| 검 | 검무 | 주변 적에게 연속 공격 | 30초 |
| 도끼 | 기절 | 적을 기절시킴 | 20초 |
| 활 | 레이저 샷 | 관통 레이저 발사 | 25초 |
| 쇠뇌 | 과충전 | 강력한 폭발 화살 | 45초 |
| 삼지창 | 번개창 | 번개 소환 | 30초 |
| 낚싯대 | 그래플링 훅 | 갈고리로 이동 | 10초 |
| 방패 | 쉴드 배쉬 | 적을 밀쳐냄 | 15초 |
| 투구 | 정화 | 디버프 제거 | 60초 |
| 흉갑 | 흡수 | 흡수 하트 부여 | 90초 |
| 레깅스 | 재생 | 체력 재생 | 60초 |
| 부츠 | 더블 점프 | 공중 2단 점프 | 5초 |
| 곡괭이 | 광맥 채굴 | 연결된 광석 한번에 채굴 | - |

## 명령어

| 명령어 | 설명 | 권한 |
|--------|------|------|
| `/upgrade on` | 강화 기능 활성화 | - |
| `/upgrade off` | 강화 기능 비활성화 | - |
| `/upgrade give [개수]` | 강화석 지급 | `anvilupgrade.admin` |
| `/upgrade reload` | 설정 리로드 | `anvilupgrade.admin` |

## 설정 (config.yml)

```yaml
# 강화 확률 표시 여부
show-success-chance: true

# 강화석 조합 결과 개수
recipe-output-amount: 8

# 레벨별 강화 확률
level-settings:
  0:
    success: 1.0    # 100% 성공
    failure: 0.0
    downgrade: 0.0
    destroy: 0.0
  # ... 레벨이 올라갈수록 확률 감소

# 특수 능력 쿨다운 (초)
ability-cooldowns:
  sword_dance: 30
  double_jump: 5
  # ...
```

## 빌드

```bash
# Maven
mvn clean package

# Gradle
gradle build
```

## 라이선스

MIT License
