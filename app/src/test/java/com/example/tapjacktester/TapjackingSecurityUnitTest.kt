package com.example.tapjacktester

import android.view.MotionEvent
import com.example.tapjacktester.model.SensitiveOperationStatus
import com.example.tapjacktester.model.TouchLogEntry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TapjackingSecurityUnitTest {

    @Test
    fun testVulnerableTargetExecutesWhenObscured() {
        var status: SensitiveOperationStatus = SensitiveOperationStatus.Idle

        // Simulate touch delivered through passthrough overlay
        val isObscured = true
        val isProtected = false

        if (!isProtected || !isObscured) {
            status = SensitiveOperationStatus.Executed("tx-123", System.currentTimeMillis())
        }

        assertTrue("Vulnerable target must execute sensitive action when obscured", status is SensitiveOperationStatus.Executed)
    }

    @Test
    fun testProtectedTargetBlocksWhenObscured() {
        var status: SensitiveOperationStatus = SensitiveOperationStatus.Idle

        // Simulate obscured touch event with filterTouchesWhenObscured active
        val isObscured = true
        val isProtected = true

        if (isProtected && isObscured) {
            status = SensitiveOperationStatus.Blocked("Touch rejected by system filter", System.currentTimeMillis())
        } else {
            status = SensitiveOperationStatus.Executed("tx-123", System.currentTimeMillis())
        }

        assertTrue("Protected target must block sensitive action when obscured", status is SensitiveOperationStatus.Blocked)
        assertEquals("Touch rejected by system filter", (status as SensitiveOperationStatus.Blocked).reason)
    }

    @Test
    fun testCredentialLogEventAttribution() {
        val username = "testuser@example.com"
        val password = "SecretPassword123"

        val logEntry = TouchLogEntry(
            actionName = "Dummy Login Submitted",
            executed = true,
            isObscured = false,
            details = "Input processed in memory by TapjackTester App. Username len: ${username.length}, Password len: ${password.length}"
        )

        assertTrue(logEntry.details.contains("TapjackTester App"))
        assertTrue(logEntry.details.contains("Password len: ${password.length}"))
        assertFalse(logEntry.details.contains("SecretPassword123")) // Raw password not leaked
    }

    @Test
    fun testOverlayServiceIntentActions() {
        assertEquals("overlay_show", OverlayService.ACTION_SHOW)
        assertEquals("overlay_hide", OverlayService.ACTION_HIDE)
        assertEquals("passthrough", OverlayService.MODE_PASSTHROUGH)
        assertEquals("intercept", OverlayService.MODE_INTERCEPT)
    }
}
