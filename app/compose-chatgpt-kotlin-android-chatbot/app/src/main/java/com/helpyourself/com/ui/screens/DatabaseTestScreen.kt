package com.helpyourself.com.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpyourself.com.data.model.MentalHealthMetric
import com.helpyourself.com.ui.viewmodel.DatabaseTestViewModel
import kotlinx.coroutines.launch

@Composable
fun DatabaseTestScreen(
    viewModel: DatabaseTestViewModel = hiltViewModel()
) {
    val TAG = "DatabaseTestScreen"
    // Log when the screen is composed
    LaunchedEffect(Unit) {
        Log.d(TAG, "Screen opened")
    }

    var connectionStatus by remember { mutableStateOf("Not tested") }
    var latestMetric by remember { mutableStateOf<MentalHealthMetric?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Firebase Connection Status:",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = connectionStatus,
            style = MaterialTheme.typography.bodyLarge,
            color = when(connectionStatus) {
                "Connected" -> MaterialTheme.colorScheme.primary
                "Not Connected" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                scope.launch {
                    Log.d(TAG, "Test Connection clicked")
                    connectionStatus = "Testing..."
                    try {
                        val isConnected = viewModel.testConnection()
                        Log.d(TAG, "onTestConnection result = $isConnected")
                        connectionStatus = if (isConnected) "Connected" else "Not Connected"
                    } catch (e: Exception) {
                        Log.e(TAG, "onTestConnection error", e)
                        connectionStatus = "Error: ${e.message}"
                    }
                }
            }
        ) {
            Text("Test Connection")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    Log.d(TAG, "Save Dummy Metric clicked")
                    connectionStatus = "Saving dummy metric..."
                    try {
                        viewModel.saveDummyMetric()
                        latestMetric = viewModel.fetchLatestMetric()
                        Log.d(TAG, "Dummy metric saved: $latestMetric")
                        connectionStatus = "Dummy metric saved!"
                    } catch (e: Exception) {
                        Log.e(TAG, "saveDummyMetric error", e)
                        connectionStatus = "Error: ${e.message}"
                    }
                }
            }
        ) {
            Text("Save Dummy Metric")
        }

        Spacer(modifier = Modifier.height(24.dp))

        latestMetric?.let {
            Text("Latest Metric:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Depression: ${it.depression}")
            Text("Anxiety: ${it.anxiety}")
            Text("Stress: ${it.stress}")
        }
    }
}
