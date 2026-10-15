package com.example.ypttablet.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ypttablet.model.*
import com.example.ypttablet.service.AppBlockAccessibilityService
import com.example.ypttablet.service.PermissionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyViewModel : ViewModel() {

    private val _timerMode = MutableStateFlow(TimerMode.STOPWATCH)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _currentSubject = MutableStateFlow("국어")
    val currentSubject: StateFlow<String> = _currentSubject.asStateFlow()

    private val _displaySeconds = MutableStateFlow(0L)
    val displaySeconds: StateFlow<Long> = _displaySeconds.asStateFlow()

    private val _todayTotalSeconds = MutableStateFlow(11420L)
    val todayTotalSeconds: StateFlow<Long> = _todayTotalSeconds.asStateFlow()

    private val _subjectStats = MutableStateFlow(
        mutableMapOf(
            "국어" to 3600L,
            "수학" to 4200L,
            "영어" to 1800L,
            "생활과 윤리" to 1020L,
            "사회·문화" to 800L
        )
    )
    val subjectStats: StateFlow<Map<String, Long>> = _subjectStats.asStateFlow()

    private var timerJob: Job? = null

    private val _todos = MutableStateFlow(
        listOf(
            TodoItem(1, "국어", "수능특강 독서 3강 지문 분석", true),
            TodoItem(2, "수학", "기출 4점 문항 오답노트 정리", false),
            TodoItem(3, "생활과 윤리", "분배적 정의(롤스·노직) 기출 선지 정리", false),
            TodoItem(4, "사회·문화", "도표 통계 문제 시간 단축 연습 (3문제)", false)
        )
    )
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    val weeklyStats = listOf(
        DailyStat("월", 25200L, emptyMap()),
        DailyStat("화", 28800L, emptyMap()),
        DailyStat("수", 19800L, emptyMap()),
        DailyStat("목", 31200L, emptyMap()),
        DailyStat("금", 27000L, emptyMap()),
        DailyStat("토", 36000L, emptyMap()),
        DailyStat("일(오늘)", 11420L, emptyMap())
    )

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _allowedPackageSet = MutableStateFlow<Set<String>>(emptySet())
    val allowedPackageSet: StateFlow<Set<String>> = _allowedPackageSet.asStateFlow()

    private val _selectedCsatPeriod = MutableStateFlow(CsatScheduleRepository.periods.first())
    val selectedCsatPeriod: StateFlow<CsatPeriod> = _selectedCsatPeriod.asStateFlow()

    private val _csatSimTimeSec = MutableStateFlow(8 * 3600 + 25 * 60)
    val csatSimTimeSec: StateFlow<Int> = _csatSimTimeSec.asStateFlow()

    private val _isCsatRunning = MutableStateFlow(false)
    val isCsatRunning: StateFlow<Boolean> = _isCsatRunning.asStateFlow()

    private val _csatLaps = MutableStateFlow<List<LapRecord>>(emptyList())
    val csatLaps: StateFlow<List<LapRecord>> = _csatLaps.asStateFlow()

    private var lastLapSecOfDay: Int? = null
    private var csatJob: Job? = null

    fun setTimerMode(mode: TimerMode) {
        pauseStudyTimer()
        _timerMode.value = mode
        _displaySeconds.value = when (mode) {
            TimerMode.STOPWATCH -> 0L
            TimerMode.TIMER -> 60 * 60L
            TimerMode.POMODORO -> 25 * 60L
        }
    }

    fun selectSubject(subject: String) {
        _currentSubject.value = subject
    }

    fun toggleStudyTimer() {
        if (_isRunning.value) pauseStudyTimer() else startStudyTimer()
    }

    private fun startStudyTimer() {
        _isRunning.value = true
        AppBlockAccessibilityService.isStudyBlockingActive = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                delay(1000L)
                when (_timerMode.value) {
                    TimerMode.STOPWATCH -> _displaySeconds.value += 1
                    TimerMode.TIMER, TimerMode.POMODORO -> {
                        if (_displaySeconds.value > 0) _displaySeconds.value -= 1
                        else pauseStudyTimer()
                    }
                }
                _todayTotalSeconds.value += 1
                val subj = _currentSubject.value
                val updatedMap = _subjectStats.value.toMutableMap()
                updatedMap[subj] = (updatedMap[subj] ?: 0L) + 1L
                _subjectStats.value = updatedMap
            }
        }
    }

    fun pauseStudyTimer() {
        _isRunning.value = false
        AppBlockAccessibilityService.isStudyBlockingActive = false
        timerJob?.cancel()
    }

    fun toggleTodo(id: Long) {
        _todos.value = _todos.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun addTodo(subject: String, task: String) {
        if (task.isBlank()) return
        val newItem = TodoItem(System.currentTimeMillis(), subject, task, false)
        _todos.value = _todos.value + newItem
    }

    fun deleteTodo(id: Long) {
        _todos.value = _todos.value.filterNot { it.id == id }
    }

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            val list = PermissionHelper.getInstalledLauncherApps(context, _allowedPackageSet.value)
            _installedApps.value = list
        }
    }

    fun toggleAllowedApp(packageName: String) {
        val current = _allowedPackageSet.value.toMutableSet()
        if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
        _allowedPackageSet.value = current
        AppBlockAccessibilityService.allowedPackages = current

        _installedApps.value = _installedApps.value.map {
            if (it.packageName == packageName) it.copy(isAllowed = current.contains(packageName)) else it
        }
    }

    fun selectCsatPeriod(period: CsatPeriod) {
        pauseCsatClock()
        _selectedCsatPeriod.value = period
        val firstMarkerSec = period.markers.first().secondOfDay
        _csatSimTimeSec.value = firstMarkerSec
        lastLapSecOfDay = null
        _csatLaps.value = emptyList()
    }

    fun toggleCsatClock() {
        if (_isCsatRunning.value) pauseCsatClock() else startCsatClock()
    }

    private fun startCsatClock() {
        _isCsatRunning.value = true
        AppBlockAccessibilityService.isStudyBlockingActive = true
        csatJob?.cancel()
        csatJob = viewModelScope.launch {
            while (_isCsatRunning.value) {
                delay(1000L)
                val endSec = _selectedCsatPeriod.value.markers.last().secondOfDay
                if (_csatSimTimeSec.value < endSec) {
                    _csatSimTimeSec.value += 1
                    _todayTotalSeconds.value += 1
                } else {
                    pauseCsatClock()
                }
            }
        }
    }

    fun pauseCsatClock() {
        _isCsatRunning.value = false
        AppBlockAccessibilityService.isStudyBlockingActive = false
        csatJob?.cancel()
    }

    fun adjustCsatTimeBySeconds(deltaSeconds: Int) {
        val markers = _selectedCsatPeriod.value.markers
        val minSec = markers.first().secondOfDay - 600
        val maxSec = markers.last().secondOfDay
        _csatSimTimeSec.value = (_csatSimTimeSec.value + deltaSeconds).coerceIn(minSec, maxSec)
    }

    fun jumpToMarker(marker: BellMarker) {
        _csatSimTimeSec.value = marker.secondOfDay
    }

    fun recordCsatLap() {
        val period = _selectedCsatPeriod.value
        val nowSec = _csatSimTimeSec.value
        val mainBellSec = period.markers.find { it.label == "본령" }?.secondOfDay
            ?: period.markers.first().secondOfDay

        val elapsedFromMain = (nowSec - mainBellSec).coerceAtLeast(0)
        val prevSec = lastLapSecOfDay ?: mainBellSec
        val diffSec = (nowSec - prevSec).coerceAtLeast(0)
        lastLapSecOfDay = nowSec

        val newLap = LapRecord(
            index = _csatLaps.value.size + 1,
            periodSubject = "${period.periodName} ${period.subjectName}",
            clockTime = formatClockTime(nowSec),
            elapsedFromMain = formatMinSec(elapsedFromMain),
            lapDiff = "+${formatMinSec(diffSec)}"
        )
        _csatLaps.value = listOf(newLap) + _csatLaps.value
    }

    companion object {
        fun formatHms(totalSec: Long): String {
            val h = totalSec / 3600
            val m = (totalSec % 3600) / 60
            val s = totalSec % 60
            return String.format("%02d:%02d:%02d", h, m, s)
        }

        fun formatClockTime(secOfDay: Int): String {
            val h = (secOfDay / 3600) % 24
            val m = (secOfDay % 3600) / 60
            val s = secOfDay % 60
            return String.format("%02d:%02d:%02d", h, m, s)
        }

        fun formatMinSec(sec: Int): String {
            val m = sec / 60
            val s = sec % 60
            return String.format("%02d분 %02d초", m, s)
        }
    }
}
