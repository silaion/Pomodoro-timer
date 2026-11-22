package com.example.pomodoro_timer.domain

/**
 * 타이머의 현재 동작 상태를 나타냅니다.
 */

enum class TimerStatus {
    IDLE,       // 대기 중 (시작 전 또는 초기화 직후)
    RUNNING,    // 카운트다운 실행 중
    PAUSED,     // 일시정지됨
    FINISHED    // 한 세션이 끝남
}

/**
 * 포모도로 타이머의 전체 상태 스냅샷을 담는 데이터 클래스입니다.
 */

data class PomodoroTimerState( // data class 는 데이터를 담는 상자로 copy(...) 기능을 기본 제공해서 나중에 "나머지는 그대로 두고 남은 시간만 바꾼 새 상태"를 쉽게 만들 수 있습니다.
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val status: TimerStatus = TimerStatus.IDLE,
    val remainingMillis: Long = PomodoroPhase.FOCUS.defaultMinutes * 60_000L, // 언더바(_)는 사람이 숫자를 읽기 쉽게 넣는 쉼표(60,000) 같은 역할이며, 뒤의 L은 64비트 정수(Long)라는 뜻(1분 = 60 밀리초)
    val totalMillis: Long = PomodoroPhase.FOCUS.defaultMinutes * 60_000L,
    val completedFocusCount: Int = 0
)