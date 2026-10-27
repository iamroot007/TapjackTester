package com.example.tapjacktester.ui

import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tapjacktester.model.SensitiveOperationStatus
import com.example.tapjacktester.model.TouchLogEntry
import java.util.UUID

@Composable
fun SensitiveActionSection(
    title: String,
    isProtected: Boolean,
    status: SensitiveOperationStatus,
    lastTouchLog: TouchLogEntry?,
    onExecuteOperation: (executed: Boolean, isObscured: Boolean, message: String) -> Unit,
    onResetState: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isProtected) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isProtected) Color(0xFF2E7D32) else Color(0xFFC62828)
            )

            Text(
                text = if (isProtected)
                    "Protection Mode: filterTouchesWhenObscured enabled. Obscured touches are blocked."
                else
                    "Protection Mode: Unprotected control. Touches execute sensitive operation even when obscured.",
                fontSize = 13.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Native Android Button wrapped in AndroidView to utilize true Android Window Touch Filtering
            AndroidView(
                factory = { context ->
                    Button(context).apply {
                        text = if (isProtected) "CONFIRM TRANSFER (PROTECTED)" else "CONFIRM TRANSFER (VULNERABLE)"
                        textSize = 15f
                        setBackgroundColor(if (isProtected) 0xFF2E7D32.toInt() else 0xFFC62828.toInt())
                        setTextColor(android.graphics.Color.WHITE)

                        if (isProtected) {
                            filterTouchesWhenObscured = true
                        }

                        setOnTouchListener { v, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                val isObscured = (event.flags and MotionEvent.FLAG_WINDOW_IS_OBSCURED != 0) ||
                                        (event.flags and MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED != 0)

                                if (isProtected && isObscured) {
                                    onExecuteOperation(
                                        false,
                                        isObscured,
                                        "Touch rejected by Android system (FLAG_WINDOW_IS_OBSCURED detected)"
                                    )
                                    true // Consumed/Blocked
                                } else {
                                    false // Let click listener handle it
                                }
                            } else {
                                false
                            }
                        }

                        setOnClickListener {
                            onExecuteOperation(
                                true,
                                false,
                                "Sensitive operation executed successfully!"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // State Display
            Text(
                text = "Target Application State:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            when (status) {
                is SensitiveOperationStatus.Idle -> {
                    Text(
                        text = "STATUS: PENDING_USER_CONFIRMATION",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                is SensitiveOperationStatus.Executed -> {
                    Text(
                        text = "STATUS: EXECUTED (TxID: ${status.transactionId})",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
                is SensitiveOperationStatus.Blocked -> {
                    Text(
                        text = "STATUS: BLOCKED (${status.reason})",
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (lastTouchLog != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last Event: ${lastTouchLog.details} (Obscured: ${lastTouchLog.isObscured})",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            if (status !is SensitiveOperationStatus.Idle) {
                OutlinedButton(
                    onClick = onResetState,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Reset Target State")
                }
            }
        }
    }
}
