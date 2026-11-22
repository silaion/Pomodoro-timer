# AGENTS.md - Pomodoro-timer 안드로이드 프로젝트 지침서

## 1. 프로젝트 개요 및 핵심 도메인
이 프로젝트는 네이티브 안드로이드 포모도로 타이머 애플리케이션(`Pomodoro-timer`)이다.
AI 에이전트는 포모도로 기법(Pomodoro Technique)을 기반으로 배터리 효율이 높고 백그라운드에서도 정확하게 동작하는 시간 관리 앱의 설계, 구현, 테스트를 지원해야 한다.

### 핵심 기능 요구사항
- **표준 사이클:** 25분 집중(Focus) -> 5분 짧은 휴식(Short Break)을 4회 반복 후 -> 15~30분 긴 휴식(Long Break).
- **타이머 제어:** 시작, 일시정지, 정지(초기화), 현재 세션 건너뛰기 기능.
- **사용자 설정(커스터마이징):** 집중 시간, 짧은 휴식, 긴 휴식, 긴 휴식 주기(기본 4사이클) 변경 및 다음 세션 자동 시작 여부 토글.
- **알림 및 피드백:** 세션 종료 시 소리, 진동, 상단 헤드업 알림(Push Notification) 제공.
- **백그라운드 생존성 및 정확도:** 앱이 백그라운드로 전환되거나 화면이 꺼진 상태(Doze 모드)에서도 타이머가 멈추거나 시간 오차가 발생하지 않아야 한다.
- **통계 기능 (2단계 고도화):** 완료된 포모도로 세션 및 일간/주간 집중 시간의 로컬 데이터 저장 및 시각화.

---

## 2. 기술 환경 및 엄격한 제약 사항 (STRICT)
생성되는 모든 코드, Gradle 설정, API 호출은 아래의 프로젝트 환경을 엄격히 준수해야 한다. **버전을 임의로 다운그레이드하거나 설정과 충돌하는 아키텍처를 도입하지 말 것.**

- **프로젝트명:** `Pomodoro-timer`
- **모듈 구성:** 단일 모듈 구조 (`:app`)
- **애플리케이션 ID / 네임스페이스:** `com.example.pomodoro_timer`
- **SDK 버전:**
    - `compileSdk`: 37
    - `targetSdk`: 37
    - `minSdk`: 33 (Android 13 / Tiramisu 이상 지원)
- **Java / JVM 호환성:** Java 11 (`JavaVersion.VERSION_11`)
- **빌드 시스템:**
    - Android Gradle Plugin (AGP): `9.5.2`
    - Gradle Wrapper: `9.6.0`
    - 의존성 관리: Gradle Version Catalog (`gradle/libs.versions.toml`)
    - Gradle 최적화: `org.gradle.configuration-cache=true`가 활성화되어 있다. 새로 추가하는 커스텀 Gradle 태스크나 플러그인은 반드시 Configuration Cache를 지원해야 한다.

### 현재 등록된 의존성 (`libs.versions.toml`)
현재 프로젝트는 표준 AndroidX View 및 Material 라이브러리를 기반으로 구성되어 있다. (Jetpack Compose는 설정되어 있지 않음)
- `androidx.core:core-ktx:1.19.1`
- `androidx.appcompat:appcompat:1.8.0`
- `com.google.android.material:material:1.14.0`
- 테스트 라이브러리: `junit:junit:4.13.2`, `androidx.test.ext:junit:1.3.0`, `androidx.test.espresso:espresso-core:3.7.0`

**의존성 관리 규칙:**
- 별도로 Jetpack Compose 마이그레이션을 지시받지 않는 한, 기존 의존성(XML 레이아웃 + Material 3 컴포넌트 + ViewBinding)을 우선 사용한다.
- 새로운 라이브러리(예: `lifecycle-viewmodel-ktx`, `lifecycle-service`, `datastore-preferences`, `room` 등)를 추가할 때는 반드시 `gradle/libs.versions.toml`에 먼저 버전과 라이브러리를 정의한 뒤 `app/build.gradle.kts`에서 참조한다.

---

## 3. 아키텍처 및 구현 가이드라인

### 타이머 정확도 및 백그라운드 실행 (매우 중요)
1. **단순 카운트다운 루프 금지:** 시스템 부하에 따른 시간 오차 누적을 방지하기 위해 `Handler.postDelayed`나 `CountDownTimer`로 1초씩 변수를 차감하는 방식에만 의존하지 않는다.
2. **경과 시간(Elapsed Realtime) 기준 계산:** 타이머 시작 시 `SystemClock.elapsedRealtime() + remainingMillis`를 계산하여 목표 종료 시각(Timestamp)을 저장하고, 남은 시간은 항상 `목표 종료 시각 - SystemClock.elapsedRealtime()`으로 동적 계산한다.
3. **포그라운드 서비스(Foreground Service) 사용:** OS에 의한 프로세스 강제 종료를 막기 위해 활성 타이머는 반드시 `ForegroundService` (`Service` 또는 `LifecycleService`) 내부에서 실행한다.
    - `AndroidManifest.xml`에 `<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />` 및 `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />` (백색소음 오디오 재생 시 `MEDIA_PLAYBACK`) 권한을 선언한다.
    - Target SDK 34 이상(현재 37) 정책 준수를 위해 `<service>` 태그 내부에 `android:foregroundServiceType`과 필수 `<property>` 태그를 누락 없이 작성한다.
4. **런타임 권한 처리 (Min SDK 33+):** 포그라운드 서비스 알림을 띄우기 전에 `android.permission.POST_NOTIFICATIONS` 런타임 권한 요청 로직을 명시적으로 구현한다.

### 코드 구조 및 모범 사례
- **패키지 구조 (`com.example.pomodoro_timer`):**
    - `ui/` — Activity, Fragment, ViewModel, 커스텀 뷰(예: 원형 프로그레스 바)
    - `service/` — `PomodoroTimerService` (포그라운드 서비스) 및 알림(Notification) 생성 헬퍼
    - `domain/` — 타이머 상태 머신(`Idle`, `Running`, `Paused`, `Finished`), 세션 타입(`Focus`, `ShortBreak`, `LongBreak`), 비즈니스 로직
    - `data/` — 사용자 설정 저장용 `DataStore`, 세션 기록 저장용 `Room` (통계 기능 구현 시)
- **상태 관리:** Service 및 Repository에서 UI 레이어로 타이머 상태(`remainingMillis`, `currentPhase`, `isRunning`, `completedCycles`)를 전달할 때는 `StateFlow` 및 `SharedFlow`를 사용한다.
- **UI 및 리소스 규칙:**
    - `app/build.gradle.kts`에서 `viewBinding = true`를 활성화한다.
    - 사용자에게 노출되는 모든 문자열은 코드에 하드코딩하지 않고 `res/values/strings.xml`로 분리한다.
    - UI 요소는 Material Design 3 컴포넌트(`MaterialButton`, `CircularProgressIndicator`, `MaterialCardView` 등)를 사용한다.

---

## 4. 에이전트 작업 흐름 및 검증 규칙
1. **단계별 점진적 구현:** 한 번에 모든 코드를 작성하지 않고 다음 순서대로 기능을 구현하고 검증한다.
    - 1단계: 핵심 타이머 상태 머신(Domain) 및 `PomodoroTimerService` (포그라운드 서비스) 구현
    - 2단계: 메인 UI (`Activity`/`Fragment` + `ViewBinding`) 구현, `StateFlow` 연동 및 알림 권한 처리
    - 3단계: 사용자 설정 영구 저장(`DataStore`) 및 종료 알림음/진동 피드백 추가
    - 4단계: 세션 통계 및 기록 기능 구현
2. **Gradle 동기화 및 빌드 검증:** `libs.versions.toml`, `build.gradle.kts`, `AndroidManifest.xml` 수정 후에는 `./gradlew assembleDebug` 명령을 통해 Configuration Cache 오류 없이 정상 컴파일되는지 확인한다.
3. **단위 테스트 작성:** `app/src/test/` 경로에 JUnit4 기반 테스트 코드를 작성하여 타이머 상태 전이 및 사이클 계산 로직(예: 4회 집중 완료 후 긴 휴식 전환 여부)을 검증한다.
4. **응답 언어:** 모든 설명, 코드 주석, 커밋 메시지 제안은 한국어로 작성한다.