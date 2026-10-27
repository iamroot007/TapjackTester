# TapjackTester & Security Proof-of-Concept

TapjackTester is an Android security research and testing application designed to evaluate Android overlay attacks (tapjacking) and demonstrate defensive mitigations on Android 8.0+ through SDK 36.

---

## Features & Scenarios

### 1. Tapjacking Proof-of-Concept (Sensitive Operation Target)
The app provides a controlled target environment demonstrating the real security impact of tapjacking:
- **Vulnerable Target Control**: Allows touches to pass through active system overlays. When tapped under a pass-through overlay (`FLAG_NOT_TOUCHABLE`), the touch reaches the underlying control and triggers a simulated sensitive operation (`PENDING_USER_CONFIRMATION` -> `EXECUTED`).
- **Protected Target Control**: Implements `filterTouchesWhenObscured = true` (or `android:filterTouchesWhenObscured`). When tapped under an overlay, the Android OS detects `FLAG_WINDOW_IS_OBSCURED` and drops/rejects the touch event (`PENDING_USER_CONFIRMATION` -> `BLOCKED`).

### 2. Overlay Behaviors
- **Passthrough Overlay**: Uses `WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE` with a semi-transparent red layer (`TYPE_APPLICATION_OVERLAY`). Touches pass directly through to the underlying application.
- **Intercepting Overlay**: Consumes touch events directly, preventing touches from reaching the underlying window.

### 3. Credential Exposure Demonstration
- Provides a synthetic login input interface using dummy credentials.
- Demonstrates how user-entered characters are received by the input field in transient memory state.
- Reports input length and event details with explicit attribution ("Input processed in memory by TapjackTester App") without storing or transmitting raw password contents.

### 4. Defensive Guidance
Detailed remediation advice covering:
1. View-level protection via `android:filterTouchesWhenObscured="true"` / `setFilterTouchesWhenObscured(true)`.
2. Custom touch handling via `onFilterTouchEventForSecurity(event)` detecting `FLAG_WINDOW_IS_OBSCURED`.
3. Activity-level protection on Android 12+ (API 31+) using `Window.setHideOverlayWindows(true)`.

---

## How to Build and Run

### Requirements
- Android Studio / Android SDK 36 (`compileSdk = 36`)
- JDK 17 / Java 11 runtime for Gradle
- Android Emulator or physical device running Android 8.0+ (API level 26+)

### Permissions Needed
- **SYSTEM_ALERT_WINDOW** ("Draw over other apps")
- **FOREGROUND_SERVICE** & **FOREGROUND_SERVICE_SPECIAL_USE**

### Build Commands
```bash
# Run unit tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug
```
The compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

### Reproduction Steps
1. Install and launch `app-debug.apk` on an Android emulator or device.
2. Grant "Draw over other apps" permission when prompted by system settings.
3. In the **Tapjacking PoC** tab:
   - Tap **Passthrough** in the Overlay Control Center to activate the red overlay.
   - Tap **CONFIRM TRANSFER (VULNERABLE)**: Note that the status changes to `EXECUTED (TxID: ...)`.
   - Tap **CONFIRM TRANSFER (PROTECTED)**: Note that the status changes to `BLOCKED (Touch rejected by Android system)`.
4. In the **Credential Exposure** tab:
   - Type dummy credentials in the synthetic login fields.
   - Observe the transient memory log updating with input length and attribution notice.

---

## Disclaimer & Security Boundaries

This project is intended strictly for security testing, research, and defensive education in authorized test environments. It does not collect, store, or transmit real user credentials or impersonate external services.

---

## License

MIT License - see `LICENSE` for details.
