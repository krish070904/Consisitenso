package com.consisteso.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.consisteso.app.data.model.*
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Rule Creation Screen
 * 
 * Allows users to create IF-THEN-CONSEQUENCE rules
 * Philosophy: Simple, direct, no hand-holding
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleCreationScreen(
    onNavigateBack: () -> Unit,
    onCreateRule: (
        name: String,
        description: String,
        triggerType: TriggerType,
        triggerTime: LocalTime?,
        triggerDays: List<DayOfWeek>,
        actionType: ActionType,
        actionDescription: String,
        estimatedDurationMinutes: Int,
        consequenceType: ConsequenceType,
        timeDebtMultiplier: Float,
        lockedApps: List<String>
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var triggerType by remember { mutableStateOf(TriggerType.TIME) }
    var triggerHour by remember { mutableStateOf("7") }
    var triggerMinute by remember { mutableStateOf("0") }
    var selectedDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var actionType by remember { mutableStateOf(ActionType.HABIT) }
    var actionDescription by remember { mutableStateOf("") }
    var estimatedDuration by remember { mutableStateOf("30") }
    var consequenceType by remember { mutableStateOf(ConsequenceType.TIME_DEBT) }
    var debtMultiplier by remember { mutableStateOf("1.5") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Rule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rule Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Rule Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            // Trigger Type
            Text("TRIGGER", fontWeight = FontWeight.Bold)
            
            TriggerTypeSelector(
                selected = triggerType,
                onSelect = { triggerType = it }
            )
            
            // Trigger Time (if TIME trigger)
            if (triggerType == TriggerType.TIME) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = triggerHour,
                        onValueChange = { triggerHour = it },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = triggerMinute,
                        onValueChange = { triggerMinute = it },
                        label = { Text("Minute") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
            
            // Days of week
            Text("Active Days", fontWeight = FontWeight.Bold)
            DayOfWeekSelector(
                selectedDays = selectedDays,
                onDaysChange = { selectedDays = it }
            )
            
            // Action
            Text("ACTION", fontWeight = FontWeight.Bold)
            
            ActionTypeSelector(
                selected = actionType,
                onSelect = { actionType = it }
            )
            
            OutlinedTextField(
                value = actionDescription,
                onValueChange = { actionDescription = it },
                label = { Text("What to do") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            OutlinedTextField(
                value = estimatedDuration,
                onValueChange = { estimatedDuration = it },
                label = { Text("Estimated Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Consequence
            Text("CONSEQUENCE", fontWeight = FontWeight.Bold)
            
            ConsequenceTypeSelector(
                selected = consequenceType,
                onSelect = { consequenceType = it }
            )
            
            if (consequenceType == ConsequenceType.TIME_DEBT) {
                OutlinedTextField(
                    value = debtMultiplier,
                    onValueChange = { debtMultiplier = it },
                    label = { Text("Debt Multiplier (e.g., 1.5)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create Button
            Button(
                onClick = {
                    val triggerTime = if (triggerType == TriggerType.TIME) {
                        LocalTime.of(
                            triggerHour.toIntOrNull() ?: 7,
                            triggerMinute.toIntOrNull() ?: 0
                        )
                    } else null
                    
                    onCreateRule(
                        name,
                        description,
                        triggerType,
                        triggerTime,
                        selectedDays.toList(),
                        actionType,
                        actionDescription,
                        estimatedDuration.toIntOrNull() ?: 30,
                        consequenceType,
                        debtMultiplier.toFloatOrNull() ?: 1.5f,
                        emptyList()
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && actionDescription.isNotBlank()
            ) {
                Text("CREATE RULE")
            }
        }
    }
}

@Composable
fun TriggerTypeSelector(
    selected: TriggerType,
    onSelect: (TriggerType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TriggerType.values().forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(type.name) }
            )
        }
    }
}

@Composable
fun ActionTypeSelector(
    selected: ActionType,
    onSelect: (ActionType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionType.values().forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(type.name) }
            )
        }
    }
}

@Composable
fun ConsequenceTypeSelector(
    selected: ConsequenceType,
    onSelect: (ConsequenceType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ConsequenceType.values().forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(type.name) }
            )
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDays: Set<DayOfWeek>,
    onDaysChange: (Set<DayOfWeek>) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DayOfWeek.values().forEach { day ->
            FilterChip(
                selected = selectedDays.contains(day),
                onClick = {
                    val newDays = if (selectedDays.contains(day)) {
                        selectedDays - day
                    } else {
                        selectedDays + day
                    }
                    onDaysChange(newDays)
                },
                label = { Text(day.name.take(3)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
