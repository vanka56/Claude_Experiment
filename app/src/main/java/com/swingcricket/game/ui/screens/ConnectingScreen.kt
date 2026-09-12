package com.swingcricket.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swingcricket.game.connection.ConnectionState
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.components.PulsingRadar
import com.swingcricket.game.ui.components.SecondaryButton
import com.swingcricket.game.ui.theme.PitchGreen
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary

@Composable
fun ConnectingScreen(isHost: Boolean, connectionState: ConnectionState, onCancel: () -> Unit) {
    GradientBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PulsingRadar()
            Spacer(Modifier.height(32.dp))
            Text(
                text = if (isHost) "Waiting for a player to join…" else "Searching for a game…",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = statusText(connectionState),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 36.dp)
            )
            if (connectionState is ConnectionState.PendingConnection) {
                CircularProgressIndicator(color = PitchGreen)
                Spacer(Modifier.height(24.dp))
            }
            SecondaryButton(text = "Cancel", onClick = onCancel)
        }
    }
}

private fun statusText(state: ConnectionState): String = when (state) {
    is ConnectionState.PendingConnection -> "Connecting to ${state.endpointName}…"
    is ConnectionState.Failed -> state.reason
    else -> "Keep both phones' Bluetooth and Wi-Fi on and close together"
}
