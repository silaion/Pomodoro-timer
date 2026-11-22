package com.example.pomodoro_timer.domain

/**
 * 포모도로 타이머의 세션 전환 규칙과 시간 계산을 담당하는 클래스입니다.
 */

class PomodoroCycleManager(
    private val focusMinutes: Long = PomodoroPhase.FOCUS.defaultMinutes,
    private val shortBreakMinutes: Long = PomodoroPhase.SHORT_BREAK.defaultMinutes,
    private val longBreakMinutes: Long = PomodoroPhase.LONG_BREAK.defaultMinutes,
    private val cyclesBeforeLongBreak: Int = DEFAULT_CYCLE_BEFORE_LONG_BREAK
) {
    /**
     * 앱이 처음 켜졌을 때의 초기 상태(25분 집중, 대기 상태)를 만듭니다.
     */
    fun createInitialState(): PomodoroTimerState {
        val totalMillis = getDurationMillis(PomodoroPhase.FOCUS)
        return PomodoroTimerState(
            phase = PomodoroPhase.FOCUS,
            status = TimerStatus.IDLE,
            remainingMillis = totalMillis,
            totalMillis = totalMillis,
            completedFocusCount = 0
        )
    }

    /**
     * 세션 종류(집중 | 짧은 휴식 | 긴 휴식)에 맞는 전체 시간을 밀리초 단위로 반환합니다.
     */
    fun getDurationMillis(phase: PomodoroPhase): Long {
        val minutes = when (phase) {
            PomodoroPhase.FOCUS -> focusMinutes
            PomodoroPhase.SHORT_BREAK -> shortBreakMinutes
            PomodoroPhase.LONG_BREAK -> longBreakMinutes
        }
        return minutes * MILLIS_PER_MINUTES
    }

    /**
     * 현재 세션이 끝났거나(isCompleted = true) 건너뛰었을 때(isCompleted = false),
     * 다음 세션의 상태를 계산해 반환됩니다.
     */
    fun moveToNextPhase(
        currentState: PomodoroTimerState,
        isCompleted: Boolean = true
    ): PomodoroTimerState {
        // 집중 세션을 '완료'했을 때만 완료 횟수를 1 올립니다. (건너뛰면 올리지 않음)
        val nextCompletedCount = if (currentState.phase == PomodoroPhase.FOCUS && isCompleted) {
            currentState.completedFocusCount + 1
        } else {
            currentState.completedFocusCount
        }

        val nextPhase = when (currentState.phase) {
            PomodoroPhase.FOCUS -> {
                // 집중을 끝냈고, 누적 횟수가 4의 배수(4, 8, 12...)라면 긴 휴식!
                if (isCompleted && nextCompletedCount > 0 && nextCompletedCount % cyclesBeforeLongBreak == 0) {
                    PomodoroPhase.LONG_BREAK
                } else {
                    PomodoroPhase.SHORT_BREAK
                }
            }
            // 짧은 휴식이나 긴 휴식이 끝나면 다시 '집중'으로 돌아갑니다.
            PomodoroPhase.SHORT_BREAK,
            PomodoroPhase.LONG_BREAK -> PomodoroPhase.FOCUS
        }

        val nextDurationMillis = getDurationMillis(nextPhase)
        return PomodoroTimerState(
            phase = nextPhase,
            status = TimerStatus.IDLE,
            remainingMillis = nextDurationMillis,
            totalMillis = nextDurationMillis,
            completedFocusCount = nextCompletedCount
        )
    }

    companion object {
        const val DEFAULT_CYCLE_BEFORE_LONG_BREAK = 4
        private const val MILLIS_PER_MINUTES = 60_000L
    }
}