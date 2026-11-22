package com.example.pomodoro_timer.domain

/**
 * 포모도로 타이머의 세션 종류를 정의하는 Enum 클래스입니다.
 *
 * @property defaultMinutes 각 세션의 기본 제공 시간(분 단위)
 */
enum class PomodoroPhase(val defaultMinutes: Long) {
    FOCUS(25L),         // 25분 집중
    SHORT_BREAK(5L),    // 5분 짧은 휴식
    LONG_BREAK(15L)     // 15분 긴 휴식
}