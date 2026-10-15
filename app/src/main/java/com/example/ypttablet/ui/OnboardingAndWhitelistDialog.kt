package com.example.ypttablet.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ypttablet.service.PermissionHelper
import com.example.ypttablet.ui.theme.*
import com.example.ypttablet.viewmodel.StudyViewModel

@Composable
fun OnboardingPermissionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasUsage by remember { mutableStateOf(PermissionHelper.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = YptCardBg,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.width(520.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("열품타 앱 차단 권한 온보딩", color = YptPrimaryGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "학습 중 허용되지 않은 앱 실행 및 홈 화면 탈출을 감지하고 차단하기 위해 아래 3가지 권한이 필요합니다.",
                    color = YptTextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                PermissionRow(
                    title = "1. 사용정보 접근 권한 (필수)",
                    isGranted = hasUsage,
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                PermissionRow(
                    title = "2. 다른 앱 위에 표시 권한 (필수)",
                    isGranted = hasOverlay,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                PermissionRow(
                    title = "3. 접근성 서비스 권한 (홈 탈출 및 비허용 앱 즉시 차단)",
                    isGranted = hasAccessibility,
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        hasUsage = PermissionHelper.hasUsageStatsPermission(context)
                        hasOverlay = PermissionHelper.hasOverlayPermission(context)
                        hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context)
                    }) {
                        Text("🔄 권한 상태 새로고침", color = YptPrimaryGreen)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = YptPrimaryGreen)
                    ) {
                        Text("확인 및 시작", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(title: String, isGranted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252F2C), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = YptTextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) Color(0xFF2E4F46) else YptAccentOrange
            )
        ) {
            Text(
                text = if (isGranted) "허용됨 ✓" else "설정 이동",
                color = if (isGranted) YptPrimaryGreen else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AllowedAppsWhitelistDialog(
    viewModel: StudyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val apps by viewModel.installedApps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = YptCardBg,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .width(560.dp)
                .height(600.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("허용 앱(화이트리스트) 지정", color = YptPrimaryGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "체크된 앱만 공부 중에 실행할 수 있으며, 체크되지 않은 앱이나 홈 화면으로 이동 시 즉시 차단됩니다.",
                    color = YptTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF252F2C), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.appName, color = YptTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(app.packageName, color = YptTextSecondary, fontSize = 12.sp)
                            }
                            Checkbox(
                                checked = app.isAllowed,
                                onCheckedChange = { viewModel.toggleAllowedApp(app.packageName) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = YptPrimaryGreen),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("저장 후 닫기", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
