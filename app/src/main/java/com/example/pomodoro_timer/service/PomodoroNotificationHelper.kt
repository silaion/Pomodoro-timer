package com.example.pomodoro_timer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.health.TimerStat
import androidx.core.app.NotificationCompat
import com.example.pomodoro_timer.R
import com.example.pomodoro_timer.domain.PomodoroPhase
import com.example.pomodoro_timer.domain.PomodoroTimerState
import com.example.pomodoro_timer.domain.TimerStatus
import java.util.Locale
import java.util.Timer

/**
 * 포그라운드 서비스용 알림 채널을 생성하고,
 * 현재 타이머 상태에 맞는 상단 알림(Notification)을 만들어 주는 도우미 클래스
 */
class PomodoroNotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    /**
     * 알림을 띄우기 전에 알림 채널(Android  8.0 이상 필수)을 등록합니다.
     * 이미 등록되어 있으면 시스템이 알아서 무시하므로 여러 번 호출해도 안전합니다.
     */
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_timer_name),
            NotificationManager.IMPORTANCE_LOW // 매초 시간을 갱신할 때 소리/진동이 울리지 않도록 LOW 설정
        ).apply {
            description = context.getString(R.string.notification_channel_timer_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 현재 타이머 상태를 받아 상단 알림 객체를 생성합니다.
     */
    fun buildTimerNotification(state: PomodoroTimerState): Notification {
        val title = when (state.phase) {
            PomodoroPhase.FOCUS -> context.getString(R.string.phase_focus)
            PomodoroPhase.SHORT_BREAK -> context.getString(R.string.phase_short_break)
            PomodoroPhase.LONG_BREAK -> context.getString(R.string.phase_long_break)
        }

        val timeText = formatMillis(state.remainingMillis)
        val contentText = when (state.status) {
            TimerStatus.RUNNING -> context.getString(R.string.notification_timer_running, timeText)
            TimerStatus.PAUSED -> context.getString(R.string.notification_timer_paused, timeText)
            TimerStatus.IDLE,
            TimerStatus.FINISHED -> context.getString(R.string.notification_timer_idle, timeText)
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(contentText)
            .setOnlyAlertOnce(true) // 알림 내용이 바뀔 때마다 다시 울리지 않게 함
            .setOngoing(state.status == TimerStatus.RUNNING) // 실행 중에는 알림을 밀어서 지우지 못하게 고정
            .build()
    }

    /**
     * 이미 떠 있는 알림의 내용을 최신 타이머 시간으로 갱신합니다.
     */
    fun updateNotification(state: PomodoroTimerState) {
        notificationManager.notify(NOTIFICATION_ID, buildTimerNotification(state))
    }

    companion object {
        const val CHANNEL_ID = "pomodoro_timer_running"
        const val NOTIFICATION_ID = 1001

        /**
         * 밀리초를 "MM:SS" 형태의 문자열로 바꿉니다. (예: 1500000L -> "25:00")
         */
        fun formatMillis(millis: Long): String {
            // + 999L로 올림 처리하면 0.5초 남았을 때 "00:00"이 아니라 "00:01"로 자연스럽게 보입니다.
            val totalSeconds = (millis.coerceAtLeast(0L) + 999L) / 1000L
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}