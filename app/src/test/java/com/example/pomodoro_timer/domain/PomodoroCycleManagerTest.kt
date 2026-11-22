package com.example.pomodoro_timer.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PomodoroCycleManagerTest {
    private val cycleManager = PomodoroCycleManager()

    @Test
    fun `초기 상태는 25분 집중 대기 상태이다`() {
        val state = cycleManager.createInitialState()

        assertEquals(PomodoroPhase.FOCUS, state.phase)
        assertEquals(TimerStatus.IDLE, state.status)
        assertEquals(25 * 60_000L, state.remainingMillis)
        assertEquals(0, state.completedFocusCount)
    }

    @Test
    fun `집중 1회 완료 후에는 짧은 휴식으로 전환된다`() {
        val initialState = cycleManager.createInitialState()

        val nextState = cycleManager.moveToNextPhase(initialState, true)

        assertEquals(PomodoroPhase.SHORT_BREAK, nextState.phase)
        assertEquals(5 * 60_000L, nextState.remainingMillis)
        assertEquals(1, nextState.completedFocusCount)
    }

    @Test
    fun `집중 4회 완료 후에는 긴 휴식으로 전환된다`() {
        var state = cycleManager.createInitialState()

        // 1~3회차: 집중 완료 -> 짧은 휴식 완료 반복
        repeat(3) {
            state = cycleManager.moveToNextPhase(state, isCompleted = true) // -> SHORT_BREAK
            state = cycleManager.moveToNextPhase(state, isCompleted = true) // -> FOCUS
        }

        val fourthBreakState = cycleManager.moveToNextPhase(state, isCompleted = true)

        assertEquals(PomodoroPhase.LONG_BREAK, fourthBreakState.phase)
        assertEquals(15 * 60_000L, fourthBreakState.remainingMillis)
        assertEquals(4, fourthBreakState.completedFocusCount)
    }

    @Test
    fun `집중을 건너뛰면 완료 횟수가 오르지 않고 짧은 휴식으로 넘어간다`() {
        val initialState = cycleManager.createInitialState()

        val skippedState = cycleManager.moveToNextPhase(initialState, isCompleted = false)

        assertEquals(PomodoroPhase.SHORT_BREAK, skippedState.phase)
        assertEquals(0, skippedState.completedFocusCount)
    }
}