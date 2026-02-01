package com.consisteso.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reward - Binary unlock system
 * Rewards are earned, not given
 */
@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val type: RewardType,
    val name: String,
    val description: String,
    
    // Unlock conditions
    val requiresPerfectDays: Int = 0,
    val requiresDebtFree: Boolean = true,
    val requiresStreakLength: Int = 0,
    
    // Status
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val expiresAt: Long? = null, // Some rewards are temporary
    
    // Usage
    val timesUsed: Int = 0,
    val lastUsedAt: Long? = null
)

enum class RewardType {
    // Daily functional rewards
    MUSIC_UNLOCK,
    YOUTUBE_UNLOCK,
    COLOR_RESTORE,
    
    // Special rewards
    SKIP_TOKEN,
    CONTROL_MODE,
    
    // Rare rewards
    SILENT_PRAISE,
    EDIT_WINDOW_EXTENSION,
    DEBT_REDUCTION,
    
    CUSTOM
}

/**
 * Skip Token - Earned after 7 perfect days
 * Can skip one rule once
 */
@Entity(tableName = "skip_tokens")
data class SkipToken(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val earnedAt: Long = System.currentTimeMillis(),
    val isUsed: Boolean = false,
    val usedAt: Long? = null,
    val usedOnRuleId: Long? = null,
    
    val earnedByStreak: Int = 0 // Which streak earned this
)
