package com.example.ypttablet.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.ypttablet.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow

class AppBlockAccessibilityService : AccessibilityService() {

    companion object {
        var isStudyBlockingActive = false
        var allowedPackages = setOf<String>()
        val blockedPackageAlert = MutableStateFlow<String?>(null)

        private val systemExemptPackages = setOf(
            "com.android.systemui",
            "com.samsung.android.honeyboard",
            "com.google.android.inputmethod.latin",
            "android"
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isStudyBlockingActive || event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val currentPkg = event.packageName?.toString() ?: return

            if (currentPkg == packageName || systemExemptPackages.contains(currentPkg)) {
                return
            }

            if (allowedPackages.contains(currentPkg)) {
                return
            }

            blockedPackageAlert.value = currentPkg
            val lockIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("BLOCKED_PACKAGE", currentPkg)
            }
            startActivity(lockIntent)
        }
    }

    override fun onInterrupt() {}
}
