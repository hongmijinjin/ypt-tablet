package com.example.ypttablet.model

import android.graphics.drawable.Drawable

enum class TimerMode(val title: String) {
    STOPWATCH("스톱워치"),
    TIMER("타이머"),
    POMODORO("뽀모도로")
}

data class TodoItem(
    val id: Long,
    val subject: String,
    val task: String,
    val isCompleted: Boolean = false
)

data class DailyStat(
    val dayLabel: String,
    val studySeconds: Long,
    val subjectBreakdown: Map<String, Long>
)

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val isAllowed: Boolean = false
)

data class BellMarker(
    val label: String,
    val timeString: String,
    val secondOfDay: Int
)

data class CsatPeriod(
    val id: Int,
    val periodName: String,
    val subjectName: String,
    val timeRangeText: String,
    val durationMinutes: Int,
    val markers: List<BellMarker>
)

data class LapRecord(
    val index: Int,
    val periodSubject: String,
    val clockTime: String,
    val elapsedFromMain: String,
    val lapDiff: String
)

object CsatScheduleRepository {
    private fun hmsToSec(h: Int, m: Int, s: Int = 0): Int = h * 3600 + m * 60 + s

    val periods = listOf(
        CsatPeriod(
            id = 1,
            periodName = "1교시",
            subjectName = "국어",
            timeRangeText = "08:40 ~ 10:00 (80분)",
            durationMinutes = 80,
            markers = listOf(
                BellMarker("예비령", "08:25", hmsToSec(8, 25)),
                BellMarker("준비령", "08:35", hmsToSec(8, 35)),
                BellMarker("본령", "08:40", hmsToSec(8, 40)),
                BellMarker("10분전", "09:50", hmsToSec(9, 50)),
                BellMarker("종료", "10:00", hmsToSec(10, 0))
            )
        ),
        CsatPeriod(
            id = 2,
            periodName = "2교시",
            subjectName = "수학",
            timeRangeText = "10:30 ~ 12:10 (100분)",
            durationMinutes = 100,
            markers = listOf(
                BellMarker("예비령", "10:20", hmsToSec(10, 20)),
                BellMarker("준비령", "10:25", hmsToSec(10, 25)),
                BellMarker("본령", "10:30", hmsToSec(10, 30)),
                BellMarker("10분전", "12:00", hmsToSec(12, 0)),
                BellMarker("종료", "12:10", hmsToSec(12, 10))
            )
        ),
        CsatPeriod(
            id = 3,
            periodName = "3교시",
            subjectName = "영어",
            timeRangeText = "13:10 ~ 14:20 (70분)",
            durationMinutes = 70,
            markers = listOf(
                BellMarker("예비령", "13:00", hmsToSec(13, 0)),
                BellMarker("준비령", "13:05", hmsToSec(13, 5)),
                BellMarker("본령", "13:10", hmsToSec(13, 10)),
                BellMarker("10분전", "14:10", hmsToSec(14, 10)),
                BellMarker("종료", "14:20", hmsToSec(14, 20))
            )
        ),
        CsatPeriod(
            id = 4,
            periodName = "4교시",
            subjectName = "한국사",
            timeRangeText = "14:50 ~ 15:20 (30분)",
            durationMinutes = 30,
            markers = listOf(
                BellMarker("예비령", "14:35", hmsToSec(14, 35)),
                BellMarker("준비령", "14:45", hmsToSec(14, 45)),
                BellMarker("본령", "14:50", hmsToSec(14, 50)),
                BellMarker("10분전", "15:10", hmsToSec(15, 10)),
                BellMarker("종료", "15:20", hmsToSec(15, 20))
            )
        ),
        CsatPeriod(
            id = 5,
            periodName = "4교시 탐구①",
            subjectName = "생활과 윤리",
            timeRangeText = "15:35 ~ 16:05 (30분)",
            durationMinutes = 30,
            markers = listOf(
                BellMarker("예비령", "15:25", hmsToSec(15, 25)),
                BellMarker("준비령", "15:30", hmsToSec(15, 30)),
                BellMarker("본령", "15:35", hmsToSec(15, 35)),
                BellMarker("10분전", "15:55", hmsToSec(15, 55)),
                BellMarker("종료", "16:05", hmsToSec(16, 5))
            )
        ),
        CsatPeriod(
            id = 6,
            periodName = "4교시 탐구②",
            subjectName = "사회·문화",
            timeRangeText = "16:07 ~ 16:37 (30분)",
            durationMinutes = 30,
            markers = listOf(
                BellMarker("준비령", "16:05", hmsToSec(16, 5)),
                BellMarker("본령", "16:07", hmsToSec(16, 7)),
                BellMarker("10분전", "16:27", hmsToSec(16, 27)),
                BellMarker("종료", "16:37", hmsToSec(16, 37))
            )
        )
    )
}
