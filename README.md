# Pomodoro-timer 🍅

포모도로 기법(Pomodoro Technique)을 기반으로 설계된 네이티브 안드로이드 시간 관리 애플리케이션입니다.  
백그라운드 환경 및 기기 절전 모드(Doze)에서도 시간 오차 없이 안정적으로 동작하는 것을 핵심 목표로 합니다.

---

## 📌 주요 기능

### 핵심 기능 (MVP)
- **표준 포모도로 사이클:** 25분 집중(Focus) → 5분 짧은 휴식(Short Break)을 4회 반복 후 → 15~30분 긴 휴식(Long Break) 자동/수동 전환
- **타이머 제어:** 시작, 일시정지, 초기화(정지), 현재 세션 건너뛰기 지원
- **백그라운드 타이머 유지:** `Foreground Service` 및 `SystemClock.elapsedRealtime()` 기반 시간 계산으로 앱 종료(백그라운드 전환) 및 화면 꺼짐 상태에서도 정확한 카운트다운 보장
- **알림 및 피드백:** 세션 종료 시 상단 헤드업 알림(Push Notification), 알람음, 진동 피드백 제공

### 확장 및 고도화 기능 (예정)
- **사용자 커스터마이징:** 집중 시간, 짧은/긴 휴식 시간, 긴 휴식 주기 설정 및 자동 전환 옵션 (`DataStore` 영구 저장)
- **할 일(To-Do) 및 통계:** 작업별 소모 세션 기록 및 일간/주간 집중 시간 리포트 제공

---

## 🛠 기술 스택 및 빌드 환경

### SDK 및 언어
- **Language:** Kotlin / Java 11 (`JavaVersion.VERSION_11`)
- **Min SDK:** 33 (Android 13 / Tiramisu 이상)
- **Target / Compile SDK:** 37

### 빌드 도구
- **Android Gradle Plugin (AGP):** 9.5.2
- **Gradle Wrapper:** 9.6.0
- **Dependency Management:** Gradle Version Catalog (`gradle/libs.versions.toml`)
- **Build Optimization:** Gradle Configuration Cache 활성화 (`org.gradle.configuration-cache=true`)

### 주요 라이브러리
- `androidx.core:core-ktx:1.19.1`
- `androidx.appcompat:appcompat:1.8.0`
- `com.google.android.material:material:1.14.0` (Material Design 3 컴포넌트)
- **Test:** `junit:junit:4.13.2`, `androidx.test.ext:junit:1.3.0`, `androidx.test.espresso:espresso-core:3.7.0`

---

## 📂 패키지 구조

```text
com.example.pomodoro_timer
├── ui/          # Activity, Fragment, ViewModel, 커스텀 뷰 (ViewBinding 기반)
├── service/     # PomodoroTimerService (Foreground Service) 및 알림(Notification) 헬퍼
├── domain/      # 타이머 상태 머신(Idle, Running, Paused, Finished) 및 사이클 계산 로직
└── data/        # 사용자 설정(DataStore) 및 세션 기록(Room) 저장소
```
