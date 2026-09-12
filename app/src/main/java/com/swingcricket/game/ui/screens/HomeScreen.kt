package com.swingcricket.game.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.components.PrimaryButton
import com.swingcricket.game.ui.components.SecondaryButton
import com.swingcricket.game.ui.theme.PitchGreen
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary

fun nearbyPermissions(): Array<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add(Manifest.permission.BLUETOOTH_ADVERTISE)
        add(Manifest.permission.BLUETOOTH_CONNECT)
        add(Manifest.permission.BLUETOOTH_SCAN)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}.toTypedArray()

@Composable
fun HomeScreen(
    playerName: String,
    onNameChange: (String) -> Unit,
    onHost: () -> Unit,
    onJoin: () -> Unit,
    onPermissionDenied: () -> Unit,
) {
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            pendingAction?.invoke()
        } else {
            onPermissionDenied()
        }
        pendingAction = null
    }

    fun withPermissions(action: () -> Unit) {
        pendingAction = action
        permissionLauncher.launch(nearbyPermissions())
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏏",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Swing Cricket",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Bowl and bat with a real swing of your phone",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            OutlinedTextField(
                value = playerName,
                onValueChange = onNameChange,
                singleLine = true,
                label = { Text("Your name") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = PitchGreen,
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            PrimaryButton(text = "Host Game", onClick = { withPermissions(onHost) })
            Spacer(Modifier.height(14.dp))
            SecondaryButton(text = "Join Game", onClick = { withPermissions(onJoin) })

            Spacer(Modifier.height(32.dp))
            Text(
                text = "One player hosts, the other joins — both phones need Bluetooth and Wi-Fi turned on and to be near each other.",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
