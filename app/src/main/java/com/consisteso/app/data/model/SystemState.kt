package com.consisteso.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * System State - Global app state
 * Controls boring mode, lockouts, rewards, etc.
 */
@Entity(tableName = "system_state")
data class SystemState(
    @PrimaryKey
    val id: Int = 1, // Singleton
    
    // Mode
    val isBoringModeActive: Boolean = false,
    val boringModeLevel: Int = 0, // 0-3, higher = more restrictive
    val boringModeReason: String? = null,
    
    // Lockouts
    val lockedApps: List<String> = emptyList(),
    val unlockedApps: List<String> = emptyList(),
    
    // Rewards
    val musicUnlocked: Boolean = false,
    val youtubeUnlocked: Boolean = false,
    val colorModeUnlocked: Boolean = true,
    val skipTokensAvailable: Int = 0,
    
    // Anti-cheat
    val globalSuspicionScore: Float = 0f,
    val silentPunishmentActive: Boolean = false,
    val silentPunishmentMultiplier: Float = 1.0f,
    
    // Edit window
    val nextEditWindowStart: Long = 0,
    val nextEditWindowEnd: Long = 0,
    val isInEditWindow: Boolean = false,
    
    // Uninstall protection
    val uninstallBlocked: Boolean = true,
    val uninstallAttempts: Int = 0,
    
    // Stats
    val totalDaysActive: Int = 0,
    val currentPerfectDays: Int = 0,
    val longestPerfectStreak: Int = 0,
    
    // Metadata
    val lastUpdated: Long = System.currentTimeMillis(),
    val appInstalledAt: Long = System.currentTimeMillis()
)

/**
 * Behavior Pattern - Detected patterns in user behavior
 * Used for escalation memory and prediction
 */
@Entity(
    tableName = "behavior_patterns",
    indices = [androidx.room.Index("detectedAt")]
)
data class BehaviorPattern(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val patternType: PatternType,
    val description: String,
    
    // Pattern details
    val failureHours: List<Int> = emptyList(), // Hours when user fails most
    val failureDays: List<Int> = emptyList(), // Days when user fails most
    val commonExcuses: List<String> = emptyList(),
    
    // Statistics
    val occurrenceCount: Int = 0,
    val confidence: Float = 0f, // 0-1
    
    // Metadata
    val detectedAt: Long = System.currentTimeMillis(),
    val lastOccurrence: Long = System.currentTimeMillis()
)

enum class PatternType {
    NIGHT_FAILURE,      // Fails at night consistently
    MORNING_FAILURE,    // Fails in morning
    WEEKEND_FAILURE,    // Fails on weekends
    RAPID_MARKING,      // Marks tasks too quickly
    TIME_MANIPULATION,  // Suspicious timing patterns
    EXCUSE_REPETITION,  // Same excuses repeatedly
    STREAK_BREAK,       // Breaks streaks at specific points
    CUSTOM              // Other detected patterns
}
