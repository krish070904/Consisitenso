package com.consisteso.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Time Debt - The cumulative punishment system
 * Debt compounds and blocks rewards
 * Only execution clears debt
 */
@Entity(tableName = "time_debt")
data class TimeDebt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Debt tracking
    val totalDebtMinutes: Int = 0,
    val activeDebtMinutes: Int = 0, // Current unpaid debt
    val clearedDebtMinutes: Int = 0, // Debt paid through execution
    
    // Debt sources
    val fromMissedRules: Int = 0,
    val fromLateCompletions: Int = 0,
    val fromCheating: Int = 0,
    
    // Multipliers (increase with repeated failures)
    val currentMultiplier: Float = 1.5f,
    val maxMultiplier: Float = 3.0f,
    
    // Blocking
    val blocksRewards: Boolean = false,
    val blocksAppUnlock: Boolean = false,
    val forcesBoringMode: Boolean = false,
    
    // Metadata
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastDebtCleared: Long? = null,
    
    // History
    val peakDebtMinutes: Int = 0,
    val totalDebtEverAccrued: Int = 0
)

/**
 * Debt transaction - Individual debt entries
 */
@Entity(
    tableName = "debt_transactions",
    indices = [androidx.room.Index("timestamp")]
)
data class DebtTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Long = System.currentTimeMillis(),
    val type: DebtTransactionType,
    
    val amountMinutes: Int,
    val multiplier: Float,
    val reason: String,
    
    val ruleId: Long? = null,
    val executionId: Long? = null,
    
    val balanceBefore: Int,
    val balanceAfter: Int
)

enum class DebtTransactionType {
    ACCRUED,        // Debt added
    CLEARED,        // Debt paid
    COMPOUNDED,     // Debt multiplied
    FORGIVEN        // Debt removed (rare)
}
