package com.consisteso.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.consisteso.app.data.model.*
import com.consisteso.app.ui.theme.ConsistEsoTheme
import com.consisteso.app.ui.viewmodel.MainViewModel
import com.consisteso.app.util.NotificationHelper
import com.consisteso.app.worker.WorkerScheduler
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)
        
        // Schedule background workers
        WorkerScheduler.scheduleAllWorkers(this)
        
        setContent {
            val systemState by viewModel.systemState.collectAsState()
            val isBoringMode = systemState?.isBoringModeActive ?: false
            
            ConsistEsoTheme(
                darkTheme = true,
                dynamicColor = false
            ) {
                // Apply boring mode (grayscale) if active
                val systemUiController = rememberSystemUiController()
                
                LaunchedEffect(isBoringMode) {
                    if (isBoringMode) {
                        // In real implementation, this would apply grayscale filter
                        // For now, we'll just change the status bar
                        systemUiController.setSystemBarsColor(
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isBoringMode) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val systemState by viewModel.systemState.collectAsState()
    val pendingExecutions by viewModel.pendingExecutions.collectAsState()
    val currentDebt by viewModel.currentDebt.collectAsState()
    val recentExecutions by viewModel.recentExecutions.collectAsState()
    
    val isBoringMode = systemState?.isBoringModeActive ?: false
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "CONSISTESO",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isBoringMode) Color.Gray else MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // System status
        SystemStatusCard(systemState, currentDebt, isBoringMode)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Pending tasks
        if (pendingExecutions.isNotEmpty()) {
            Text(
                text = "PENDING",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isBoringMode) Color.Gray else Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingExecutions) { execution ->
                    PendingExecutionCard(
                        execution = execution,
                        onComplete = { duration ->
                            viewModel.completeExecution(execution.id, duration)
                        },
                        onSkip = {
                            viewModel.useSkipToken(execution.id)
                        },
                        isBoringMode = isBoringMode,
                        skipTokensAvailable = systemState?.skipTokensAvailable ?: 0
                    )
                }
            }
        } else {
            // Execution graph
            Text(
                text = "EXECUTION GRAPH",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isBoringMode) Color.Gray else Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExecutionGraph(
                executions = recentExecutions,
                isBoringMode = isBoringMode
            )
        }
    }
}

@Composable
fun SystemStatusCard(
    systemState: SystemState?,
    currentDebt: TimeDebt?,
    isBoringMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBoringMode) Color(0xFF2A2A2A) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Debt display (only shown when it hurts)
            val activeDebt = currentDebt?.activeDebtMinutes ?: 0
            if (activeDebt > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEBT",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isBoringMode) Color.Gray else Color(0xFFFF5252)
                    )
                    Text(
                        text = "${activeDebt}m",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isBoringMode) Color.Gray else Color(0xFFFF5252)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Rewards status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RewardIndicator(
                    label = "MUSIC",
                    unlocked = systemState?.musicUnlocked ?: false,
                    isBoringMode = isBoringMode
                )
                RewardIndicator(
                    label = "VIDEO",
                    unlocked = systemState?.youtubeUnlocked ?: false,
                    isBoringMode = isBoringMode
                )
                RewardIndicator(
                    label = "COLOR",
                    unlocked = systemState?.colorModeUnlocked ?: true,
                    isBoringMode = isBoringMode
                )
            }
            
            // Skip tokens
            val skipTokens = systemState?.skipTokensAvailable ?: 0
            if (skipTokens > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SKIP TOKENS: $skipTokens",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isBoringMode) Color.Gray else Color(0xFF4CAF50)
                )
            }
            
            // Boring mode indicator
            if (isBoringMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "BORING MODE ACTIVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                systemState?.boringModeReason?.let { reason ->
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun RewardIndicator(
    label: String,
    unlocked: Boolean,
    isBoringMode: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (unlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = label,
            tint = when {
                isBoringMode -> Color.Gray
                unlocked -> Color(0xFF4CAF50)
                else -> Color(0xFF757575)
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isBoringMode) Color.Gray else Color.White
        )
    }
}

@Composable
fun PendingExecutionCard(
    execution: Execution,
    onComplete: (Int) -> Unit,
    onSkip: () -> Unit,
    isBoringMode: Boolean,
    skipTokensAvailable: Int
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBoringMode) Color(0xFF2A2A2A) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rule #${execution.ruleId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isBoringMode) Color.Gray else Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCompleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBoringMode) Color(0xFF3A3A3A) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("COMPLETE")
                }
                
                if (skipTokensAvailable > 0) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SKIP")
                    }
                }
            }
        }
    }
    
    if (showCompleteDialog) {
        CompleteExecutionDialog(
            onDismiss = { showCompleteDialog = false },
            onComplete = { duration ->
                onComplete(duration)
                showCompleteDialog = false
            }
        )
    }
}

@Composable
fun CompleteExecutionDialog(
    onDismiss: () -> Unit,
    onComplete: (Int) -> Unit
) {
    var durationText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Execution") },
        text = {
            Column {
                Text("How long did it take? (minutes)")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duration") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 0
                    onComplete(duration)
                }
            ) {
                Text("CONFIRM")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun ExecutionGraph(
    executions: List<Execution>,
    isBoringMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBoringMode) Color(0xFF2A2A2A) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (executions.isEmpty()) {
                Text(
                    text = "No executions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isBoringMode) Color.Gray else Color.White
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(executions.take(20)) { execution ->
                        ExecutionGraphItem(execution, isBoringMode)
                    }
                }
            }
        }
    }
}

@Composable
fun ExecutionGraphItem(
    execution: Execution,
    isBoringMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rule #${execution.ruleId}",
            style = MaterialTheme.typography.bodySmall,
            color = if (isBoringMode) Color.Gray else Color.White
        )
        
        Icon(
            imageVector = when (execution.status) {
                ExecutionStatus.COMPLETED -> Icons.Default.CheckCircle
                ExecutionStatus.MISSED -> Icons.Default.Cancel
                ExecutionStatus.SKIPPED -> Icons.Default.SkipNext
                ExecutionStatus.PENDING -> Icons.Default.Schedule
                else -> Icons.Default.Help
            },
            contentDescription = execution.status.name,
            tint = when {
                isBoringMode -> Color.Gray
                execution.status == ExecutionStatus.COMPLETED -> Color(0xFF4CAF50)
                execution.status == ExecutionStatus.MISSED -> Color(0xFFFF5252)
                else -> Color(0xFF757575)
            },
            modifier = Modifier.size(16.dp)
        )
    }
}

