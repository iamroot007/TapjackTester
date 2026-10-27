package com.example.tapjacktester

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tapjacktester.model.SensitiveOperationStatus
import com.example.tapjacktester.model.TouchLogEntry
import com.example.tapjacktester.ui.CredentialExposureSection
import com.example.tapjacktester.ui.SensitiveActionSection
import com.example.tapjacktester.ui.theme.TapjackTesterTheme
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ensureOverlayPermission()

        setContent {
            TapjackTesterTheme {
                Scaffold { padding ->
                    MainAppContent(modifier = Modifier.padding(padding))
                }
            }
        }
    }

    private fun ensureOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }
}

@Composable
private fun MainAppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeOverlayMode by remember { mutableStateOf<String?>(null) }

    // States for Tapjacking Target (Vulnerable)
    var vulnerableStatus by remember { mutableStateOf<SensitiveOperationStatus>(SensitiveOperationStatus.Idle) }
    var vulnerableTouchLog by remember { mutableStateOf<TouchLogEntry?>(null) }

    // States for Tapjacking Target (Protected)
    var protectedStatus by remember { mutableStateOf<SensitiveOperationStatus>(SensitiveOperationStatus.Idle) }
    var protectedTouchLog by remember { mutableStateOf<TouchLogEntry?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Tapjacking PoC") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Credential Exposure") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Defensive Guidance") }
            )
        }

        // Overlay Service Controls Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Overlay Control Center",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (activeOverlayMode != null)
                        "Active Mode: $activeOverlayMode"
                    else
                        "Overlay Status: Inactive",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val newMode = if (activeOverlayMode == OverlayService.MODE_PASSTHROUGH) null else OverlayService.MODE_PASSTHROUGH
                            activeOverlayMode = newMode
                            context.startService(
                                Intent(context, OverlayService::class.java).apply {
                                    action = if (newMode != null) OverlayService.ACTION_SHOW else OverlayService.ACTION_HIDE
                                    putExtra(OverlayService.EXTRA_MODE, OverlayService.MODE_PASSTHROUGH)
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeOverlayMode == OverlayService.MODE_PASSTHROUGH)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (activeOverlayMode == OverlayService.MODE_PASSTHROUGH) "Stop Overlay" else "Passthrough")
                    }

                    Button(
                        onClick = {
                            val newMode = if (activeOverlayMode == OverlayService.MODE_INTERCEPT) null else OverlayService.MODE_INTERCEPT
                            activeOverlayMode = newMode
                            context.startService(
                                Intent(context, OverlayService::class.java).apply {
                                    action = if (newMode != null) OverlayService.ACTION_SHOW else OverlayService.ACTION_HIDE
                                    putExtra(OverlayService.EXTRA_MODE, OverlayService.MODE_INTERCEPT)
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeOverlayMode == OverlayService.MODE_INTERCEPT)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (activeOverlayMode == OverlayService.MODE_INTERCEPT) "Stop Overlay" else "Intercepting")
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> {
                    // Tapjacking PoC Tab
                    SensitiveActionSection(
                        title = "1. Vulnerable Sensitive Action Target",
                        isProtected = false,
                        status = vulnerableStatus,
                        lastTouchLog = vulnerableTouchLog,
                        onExecuteOperation = { executed, isObscured, message ->
                            vulnerableTouchLog = TouchLogEntry(
                                actionName = "Vulnerable Action Confirm",
                                executed = executed,
                                isObscured = isObscured,
                                details = message
                            )
                            if (executed) {
                                vulnerableStatus = SensitiveOperationStatus.Executed(
                                    transactionId = UUID.randomUUID().toString().take(8),
                                    timestampMs = System.currentTimeMillis()
                                )
                            }
                        },
                        onResetState = {
                            vulnerableStatus = SensitiveOperationStatus.Idle
                            vulnerableTouchLog = null
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SensitiveActionSection(
                        title = "2. Protected Sensitive Action Target",
                        isProtected = true,
                        status = protectedStatus,
                        lastTouchLog = protectedTouchLog,
                        onExecuteOperation = { executed, isObscured, message ->
                            protectedTouchLog = TouchLogEntry(
                                actionName = "Protected Action Confirm",
                                executed = executed,
                                isObscured = isObscured,
                                details = message
                            )
                            if (executed) {
                                protectedStatus = SensitiveOperationStatus.Executed(
                                    transactionId = UUID.randomUUID().toString().take(8),
                                    timestampMs = System.currentTimeMillis()
                                )
                            } else {
                                protectedStatus = SensitiveOperationStatus.Blocked(
                                    reason = message,
                                    timestampMs = System.currentTimeMillis()
                                )
                            }
                        },
                        onResetState = {
                            protectedStatus = SensitiveOperationStatus.Idle
                            protectedTouchLog = null
                        }
                    )
                }

                1 -> {
                    // Credential Exposure Tab
                    CredentialExposureSection()
                }

                2 -> {
                    // Defensive Guidance Tab
                    DefensiveGuidanceSection()
                }
            }
        }
    }
}

@Composable
fun DefensiveGuidanceSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Android Overlay Mitigation Guidance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1. View-Level Touch Filtering (filterTouchesWhenObscured):\n" +
                        "Set `android:filterTouchesWhenObscured=\"true\"` in XML layout or `setFilterTouchesWhenObscured(true)` programmatically on sensitive buttons. When active, Android automatically drops touch events when an overlay covers the window.",
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "2. Custom Touch Event Handling (onFilterTouchEventForSecurity):\n" +
                        "Override `onFilterTouchEventForSecurity(MotionEvent event)` in custom views to detect `FLAG_WINDOW_IS_OBSCURED` or `FLAG_WINDOW_IS_PARTIALLY_OBSCURED` and reject sensitive actions.",
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "3. Activity-Level Protection (Hide Overlays):\n" +
                        "On Android 12 (API 31+), call `Window.setHideOverlayWindows(true)` to prevent non-system overlays from being drawn on top of sensitive activities altogether.",
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
