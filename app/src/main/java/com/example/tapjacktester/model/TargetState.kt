package com.example.tapjacktester.model

data class TouchLogEntry(
    val timestampMs: Long = System.currentTimeMillis(),
    val actionName: String,
    val executed: Boolean,
    val isObscured: Boolean,
    val details: String
)

sealed class SensitiveOperationStatus {
    object Idle : SensitiveOperationStatus()
    data class Executed(val transactionId: String, val timestampMs: Long) : SensitiveOperationStatus()
    data class Blocked(val reason: String, val timestampMs: Long) : SensitiveOperationStatus()
}
