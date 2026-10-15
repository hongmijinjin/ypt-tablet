package com.example.ypttablet.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ypttablet.model.BellMarker
import com.example.ypttablet.model.CsatPeriod
import com.example.ypttablet.model.CsatScheduleRepository
import com.example.ypttablet.ui.theme.*
import com.example.ypttablet.viewmodel.StudyViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CsatModeScreen(
    viewModel: StudyViewModel,
    onBackToDashboard: () -> Unit
) {
    val selectedPeriod by viewModel.selectedCsatPeriod.collectAsState()
    val simTimeSec by viewModel.csatSimTimeSec.collectAsState()
    val isRunning by viewModel.isCsatRunning.collectAsState()
    val laps by viewModel.csatLaps.collectAsState()

    var isMinimalClockMode by remember { mutableStateOf(false) }

    val clockSize by animateDpAsState(
        targetValue = if (isMinimalClockMode) 420.dp else 250.dp,
        label = "clockSizeAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isMinimalClockMode) Color.Black else YptDarkBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isMinimalClockMode = !isMinimalClockMode
            }
            .padding(24.dp)
    ) {
        AnimatedVisibility(
            visible = !isMinimalClockMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        viewModel.pauseCsatClock()
                        onBackToDashboard()
                    }) {
                        Text("← 일반 대시보드", color = YptPrimaryGreen, fontSize = 16.sp)
                    }

                    Text(
                        text = "수능 실전 시뮬레이션 모드 (배경 터치 시 미니멀 시계 전환)",
                        color = YptTextSecondary,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "${selectedPeriod.periodName} ${selectedPeriod.subjectName} (${selectedPeriod.timeRangeText})",
                        color = YptTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CsatScheduleRepository.periods) { period ->
                        val isSelected = period.id == selectedPeriod.id
                        Surface(
                            color = if (isSelected) YptPrimaryGreen else YptCardBg,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { viewModel.selectCsatPeriod(period) }
                        ) {
                            Text(
                                text = "${period.periodName} · ${period.subjectName}",
                                color = if (isSelected) Color.Black else YptTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnalogCsatClock(
                    secOfDay = simTimeSec,
                    modifier = Modifier.size(clockSize)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = StudyViewModel.formatClockTime(simTimeSec),
                    color = if (isMinimalClockMode) Color(0xFF758580) else YptPrimaryGreen,
                    fontSize = if (isMinimalClockMode) 28.sp else 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = !isMinimalClockMode && laps.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = YptCardBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(320.dp)
                        .height(260.dp)
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "구간 기록 (랩타임)",
                            color = YptPrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(laps) { lap ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("#${lap.index} (${lap.clockTime})", color = YptTextSecondary, fontSize = 13.sp)
                                    Text(lap.elapsedFromMain, color = YptTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(lap.lapDiff, color = YptAccentOrange, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isMinimalClockMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CsatInteractiveTimeline(
                    period = selectedPeriod,
                    currentSec = simTimeSec,
                    onMarkerClick = { marker -> viewModel.jumpToMarker(marker) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.adjustCsatTimeBySeconds(-30) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = YptTextPrimary)
                    ) {
                        Text("⏪ -30초")
                    }

                    Button(
                        onClick = { viewModel.toggleCsatClock() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) YptAccentOrange else YptPrimaryGreen
                        ),
                        modifier = Modifier.height(48.dp).width(140.dp)
                    ) {
                        Text(
                            text = if (isRunning) "일시정지" else "시험 시작",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.adjustCsatTimeBySeconds(30) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = YptTextPrimary)
                    ) {
                        Text("+30초 ⏩")
                    }

                    Button(
                        onClick = { viewModel.recordCsatLap() },
                        colors = ButtonDefaults.buttonColors(containerColor = YptCardBg)
                    ) {
                        Text("⏱ 구간 기록", color = YptPrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalogCsatClock(secOfDay: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = Color(0xFF1E2624), radius = radius, center = center)
        drawCircle(
            color = Color(0xFF00C896),
            radius = radius,
            center = center,
            style = Stroke(width = 6f)
        )

        for (i in 0 until 60) {
            val angleRad = Math.toRadians((i * 6 - 90).toDouble())
            val isHourTick = i % 5 == 0
            val tickLength = if (isHourTick) radius * 0.12f else radius * 0.05f
            val startRadius = radius * 0.92f
            val endRadius = startRadius - tickLength

            val start = Offset(
                x = center.x + (startRadius * cos(angleRad)).toFloat(),
                y = center.y + (startRadius * sin(angleRad)).toFloat()
            )
            val end = Offset(
                x = center.x + (endRadius * cos(angleRad)).toFloat(),
                y = center.y + (endRadius * sin(angleRad)).toFloat()
            )

            drawLine(
                color = if (isHourTick) Color.White else Color(0xFF657570),
                start = start,
                end = end,
                strokeWidth = if (isHourTick) 5f else 2f,
                cap = StrokeCap.Round
            )

            if (isHourTick) {
                val hourNumber = if (i == 0) 12 else i / 5
                val textRadius = radius * 0.72f
                val textX = center.x + (textRadius * cos(angleRad)).toFloat()
                val textY = center.y + (textRadius * sin(angleRad)).toFloat() + (radius * 0.04f)

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = radius * 0.13f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    drawText(hourNumber.toString(), textX, textY, paint)
                }
            }
        }

        val hours = (secOfDay / 3600f) % 12f
        val minutes = (secOfDay % 3600) / 60f
        val seconds = (secOfDay % 60).toFloat()

        val hourAngle = Math.toRadians((hours * 30f - 90f).toDouble())
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + (radius * 0.5f * cos(hourAngle)).toFloat(),
                center.y + (radius * 0.5f * sin(hourAngle)).toFloat()
            ),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )

        val minAngle = Math.toRadians((minutes * 6f - 90f).toDouble())
        drawLine(
            color = Color(0xFF00C896),
            start = center,
            end = Offset(
                center.x + (radius * 0.72f * cos(minAngle)).toFloat(),
                center.y + (radius * 0.72f * sin(minAngle)).toFloat()
            ),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )

        val secAngle = Math.toRadians((seconds * 6f - 90f).toDouble())
        drawLine(
            color = Color(0xFFFF6D3F),
            start = center,
            end = Offset(
                center.x + (radius * 0.82f * cos(secAngle)).toFloat(),
                center.y + (radius * 0.82f * sin(secAngle)).toFloat()
            ),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        drawCircle(color = Color(0xFFFF6D3F), radius = 8f, center = center)
    }
}

@Composable
fun CsatInteractiveTimeline(
    period: CsatPeriod,
    currentSec: Int,
    onMarkerClick: (BellMarker) -> Unit
) {
    val startSec = period.markers.first().secondOfDay
    val endSec = period.markers.last().secondOfDay
    val totalSpan = (endSec - startSec).coerceAtLeast(1)
    val progress = ((currentSec - startSec).toFloat() / totalSpan.toFloat()).coerceIn(0f, 1f)

    Surface(
        color = YptCardBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = YptPrimaryGreen,
                trackColor = Color(0xFF2C3835)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                period.markers.forEach { marker ->
                    val isPassedOrCurrent = currentSec >= marker.secondOfDay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onMarkerClick(marker) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isPassedOrCurrent) YptPrimaryGreen else YptTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = marker.label,
                            color = if (isPassedOrCurrent) YptPrimaryGreen else YptTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = marker.timeString,
                            color = YptTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
