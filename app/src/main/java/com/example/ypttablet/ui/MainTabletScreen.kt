package com.example.ypttablet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ypttablet.model.TimerMode
import com.example.ypttablet.ui.theme.*
import com.example.ypttablet.viewmodel.StudyViewModel

@Composable
fun MainTabletScreen(
    viewModel: StudyViewModel,
    onNavigateCsatMode: () -> Unit,
    onOpenWhitelistSettings: () -> Unit,
    onOpenPermissionOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val timerMode by viewModel.timerMode.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val displaySec by viewModel.displaySeconds.collectAsState()
    val todayTotalSec by viewModel.todayTotalSeconds.collectAsState()
    val currentSubject by viewModel.currentSubject.collectAsState()
    val todos by viewModel.todos.collectAsState()
    val subjectStats by viewModel.subjectStats.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    val subjects = listOf("국어", "수학", "영어", "한국사", "생활과 윤리", "사회·문화")
    var newTodoText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(YptDarkBg)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("YPT TABLET", color = YptPrimaryGreen, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text(
                        text = if (isRunning) "🔒 타 앱 및 홈 화면 이탈 차단 중" else "대기 중 (시작 시 자동 앱 차단)",
                        color = if (isRunning) YptAccentOrange else YptTextSecondary,
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onNavigateCsatMode,
                        colors = ButtonDefaults.buttonColors(containerColor = YptAccentOrange)
                    ) {
                        Text("🎯 수능 모드", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onOpenWhitelistSettings) {
                        Text("허용 앱 설정", color = YptPrimaryGreen)
                    }
                    TextButton(onClick = onOpenPermissionOnboarding) {
                        Text("권한 점검", color = YptTextSecondary)
                    }
                }
            }

            Surface(
                color = YptCardBg,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TimerMode.entries.forEach { mode ->
                            val selected = mode == timerMode
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setTimerMode(mode) },
                                label = { Text(mode.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YptPrimaryGreen,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "현재 과목: $currentSubject",
                        color = YptPrimaryGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = StudyViewModel.formatHms(displaySec),
                        color = YptTextPrimary,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Text(
                        text = "오늘 총 누적 학습: ${StudyViewModel.formatHms(todayTotalSec)}",
                        color = YptTextSecondary,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.toggleStudyTimer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) YptAccentOrange else YptPrimaryGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(54.dp)
                    ) {
                        Text(
                            text = if (isRunning) "휴식하기 (일시정지)" else "공부 시작 (앱 차단 활성화)",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column {
                Text("과목 선택", color = YptTextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects) { subj ->
                        val isSelected = subj == currentSubject
                        Surface(
                            color = if (isSelected) YptPrimaryGreen else YptCardBg,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { viewModel.selectSubject(subj) }
                        ) {
                            Text(
                                text = subj,
                                color = if (isSelected) Color.Black else YptTextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                val allowedApps = installedApps.filter { it.isAllowed }
                Text("학습 중 사용 가능 허용 앱 (${allowedApps.size}개)", color = YptTextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (allowedApps.isEmpty()) {
                    Text("우측 상단 '허용 앱 설정'에서 인강/사전/노트 앱을 체크해 주세요.", color = YptTextSecondary, fontSize = 12.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(allowedApps) { app ->
                            AssistChip(
                                onClick = {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (launchIntent != null) context.startActivity(launchIntent)
                                },
                                label = { Text(app.appName, color = YptTextPrimary) }
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                color = YptCardBg,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("오늘의 To-Do 리스트", color = YptPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTodoText,
                            onValueChange = { newTodoText = it },
                            placeholder = { Text("[$currentSubject] 할 일 입력...", color = YptTextSecondary) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                viewModel.addTodo(currentSubject, newTodoText)
                                newTodoText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YptPrimaryGreen)
                        ) {
                            Text("추가", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(todos, key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF252F2C))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = item.isCompleted,
                                        onCheckedChange = { viewModel.toggleTodo(item.id) }
                                    )
                                    Text(
                                        text = "[${item.subject}] ${item.task}",
                                        color = if (item.isCompleted) YptTextSecondary else YptTextPrimary,
                                        fontSize = 14.sp
                                    )
                                }
                                TextButton(onClick = { viewModel.deleteTodo(item.id) }) {
                                    Text("삭제", color = YptAccentOrange, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                color = YptCardBg,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("일별 과목 비중 & 주간 학습 통계", color = YptPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val totalSubjSec = subjectStats.values.sum().coerceAtLeast(1L)
                    subjectStats.forEach { (subj, sec) ->
                        val ratio = (sec.toFloat() / totalSubjSec.toFloat()).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subj, color = YptTextPrimary, fontSize = 13.sp, modifier = Modifier.width(80.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                color = YptPrimaryGreen,
                                trackColor = Color(0xFF2A3633),
                                modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${sec / 60}분 (${(ratio * 100).toInt()}%)",
                                color = YptTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.width(85.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF2C3835))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        viewModel.weeklyStats.forEach { stat ->
                            val hours = stat.studySeconds / 3600f
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(String.format("%.1fh", hours), color = YptPrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stat.dayLabel, color = YptTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
