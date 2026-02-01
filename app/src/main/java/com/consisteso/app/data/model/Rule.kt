package com.consisteso.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.consisteso.app.data.local.Converters
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Core Rule entity - The law that governs behavior
 * Rules are immutable once created (except during edit window)
 */
@Entity(tableName = "rules")
@TypeConverters(Converters::class)
data class Rule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val description: String,
    
    // IF conditions
    val triggerType: TriggerType,
    val triggerTime: LocalTime? = null, // For TIME triggers
    val triggerDays: List<DayOfWeek> = emptyList(), // Which days this rule applies
    
    // DO action
    val actionType: ActionType,
    val actionDescription: String,
    val estimatedDurationMinutes: Int, // How long it should take
    
    // CONSEQUENCE
    val consequenceType: ConsequenceType,
    val timeDebtMultiplier: Float = 1.5f, // Default 1.5x debt
    val lockedApps: List<String> = emptyList(), // Package names to lock
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val priority: Int = 0, // Higher priority rules checked first
    
    // Tracking
    val totalCompletions: Int = 0,
    val totalMisses: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

enum class TriggerType {
    TIME,           // Trigger at specific time
    DAILY,          // Trigger once per day (any time)
    WAKE_UP,        // Trigger on device unlock (morning)
    BEFORE_SLEEP,   // Trigger before sleep time
    APP_OPEN,       // Trigger when specific app opens
    CUSTOM          // Custom condition
}

enum class ActionType {
    TASK,           // Complete a task
    HABIT,          // Do a habit
    RESTRICTION,    // Don't do something
    TIME_BLOCK,     // Block time for activity
    CUSTOM          // Custom action
}

enum class ConsequenceType {
    TIME_DEBT,      // Add time debt
    APP_LOCK,       // Lock apps
    BORING_MODE,    // Enable grayscale + restrictions
    ESCALATION,     // Increase difficulty
    NONE            // No consequence (rare)
}
