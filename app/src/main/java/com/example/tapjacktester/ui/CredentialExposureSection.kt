package com.example.tapjacktester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CredentialLogEntry(
    val timestampMs: Long = System.currentTimeMillis(),
    val usernameLength: Int,
    val passwordLength: Int,
    val eventDetails: String
)

@Composable
fun CredentialExposureSection() {
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var credentialLog by remember { mutableStateOf<CredentialLogEntry?>(null) }
    var submissionMessage by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Credential Exposure Demonstration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Text(
                text = "Demonstrates how synthetic login input receives user entry. Input is processed only in transient memory with an explicit log message.",
                fontSize = 13.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            OutlinedTextField(
                value = usernameInput,
                onValueChange = {
                    usernameInput = it
                    credentialLog = CredentialLogEntry(
                        usernameLength = usernameInput.length,
                        passwordLength = passwordInput.length,
                        eventDetails = "Username text updated in transient memory"
                    )
                },
                label = { Text("Fictional Username / Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                    passwordInput = it
                    credentialLog = CredentialLogEntry(
                        usernameLength = usernameInput.length,
                        passwordLength = passwordInput.length,
                        eventDetails = "Password text updated in transient memory"
                    )
                },
                label = { Text("Fictional Password (Dummy)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    submissionMessage = "Input processed in memory by TapjackTester App. " +
                            "(Username: ${usernameInput.length} chars, Password: ${passwordInput.length} chars). " +
                            "No password content was saved or transmitted."
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                Text("Submit Dummy Login")
            }

            if (credentialLog != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFFFB74D), shape = RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Transient Memory Log (TapjackTester):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "• Event: ${credentialLog!!.eventDetails}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "• Username field length: ${credentialLog!!.usernameLength}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "• Password field length: ${credentialLog!!.passwordLength}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        if (submissionMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = submissionMessage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFBF360C)
                            )
                        }
                    }
                }
            }
        }
    }
}
