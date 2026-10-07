package com.recipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.recipeapp.data.ApiKeyStore

/**
 * Lets the user paste in their own free Gemini API key, stored encrypted
 * on-device (see ApiKeyStore) -- never compiled into the app or committed to
 * source control.
 */
@Composable
fun SettingsScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var keyInput by remember { mutableStateOf("") }
    var hasKey by remember { mutableStateOf(ApiKeyStore.hasKey(context)) }
    var showKey by remember { mutableStateOf(false) }

    Column(Modifier.padding(24.dp).fillMaxSize()) {
        Text("Gemini API Key", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Get a free key at aistudio.google.com/apikey (Google account, no credit card). " +
                "It's stored encrypted on this device only -- never bundled into the app.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))

        if (hasKey) {
            Text("✓ A key is saved on this device.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = {
                ApiKeyStore.clearKey(context)
                hasKey = false
            }) { Text("Remove saved key") }
        } else {
            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text("Paste your Gemini API key") },
                singleLine = true,
                visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None
                    else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { showKey = !showKey }) {
                Text(if (showKey) "Hide" else "Show")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    ApiKeyStore.setKey(context, keyInput)
                    hasKey = true
                },
                enabled = keyInput.isNotBlank()
            ) { Text("Save key") }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text(if (hasKey) "Continue" else "Skip for now") }
    }
}
