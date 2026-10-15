package com.example.ypttablet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ypttablet.service.AppBlockAccessibilityService
import com.example.ypttablet.service.PermissionHelper
import com.example.ypttablet.ui.AllowedAppsWhitelistDialog
import com.example.ypttablet.ui.CsatModeScreen
import com.example.ypttablet.ui.MainTabletScreen
import com.example.ypttablet.ui.OnboardingPermissionDialog
import com.example.ypttablet.ui.theme.*
import com.example.ypttablet.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {

    private val studyViewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!AppBlockAccessibilityService.isStudyBlockingActive) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        studyViewModel.loadInstalledApps(this)

        setContent {
            YptTabletTheme {
                var currentScreen by remember { mutableStateOf("DASHBOARD") }
                var showWhitelistDialog by remember { mutableStateOf(false) }
                var showOnboardingDialog by remember {
                    mutableStateOf(
                        !PermissionHelper.hasUsageStatsPermission(this) ||
                        !PermissionHelper.isAccessibilityServiceEnabled(this)
                    )
                }

                val blockedPkg by AppBlockAccessibilityService.blockedPackageAlert.collectAsState()

                if (currentScreen == "DASHBOARD") {
                    MainTabletScreen(
                        viewModel = studyViewModel,
                        onNavigateCsatMode = {
                            studyViewModel.pauseStudyTimer()
                            currentScreen = "CSAT"
                        },
                        onOpenWhitelistSettings = { showWhitelistDialog = true },
                        onOpenPermissionOnboarding = { showOnboardingDialog = true }
                    )
                } else {
                    CsatModeScreen(
                        viewModel = studyViewModel,
                        onBackToDashboard = { currentScreen = "DASHBOARD" }
                    )
                }

                if (showOnboardingDialog) {
                    OnboardingPermissionDialog(onDismiss = { showOnboardingDialog = false })
                }

                if (showWhitelistDialog) {
                    AllowedAppsWhitelistDialog(
                        viewModel = studyViewModel,
                        onDismiss = { showWhitelistDialog = false }
                    )
                }

                if (blockedPkg != null) {
                    Dialog(
                        onDismissRequest = {},
                        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                    ) {
                        Surface(
                            color = YptCardBg,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.width(440.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🚫 학습 중 이탈 감지", color = YptAccentOrange, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "허용되지 않은 앱 또는 홈 화면 이동이 감지되어 차단되었습니다.\n(감지된 패키지: $blockedPkg)",
                                    color = YptTextPrimary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { AppBlockAccessibilityService.blockedPackageAlert.value = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = YptPrimaryGreen),
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("타이머 화면으로 돌아가기", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
