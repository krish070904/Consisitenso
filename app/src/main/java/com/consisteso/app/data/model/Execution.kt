package com.consisteso.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Execution record - Every rule execution is tracked
 * This builds the execution graph and pattern detection
 */
@Entity(
    tableName = "executions",
    foreignKeys = [
        ForeignKey(
            entity = Rule::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ruleId"), Index("timestamp")]
)
data class Execution(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val ruleId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Execution details
    val status: ExecutionStatus,
    val completedAt: Long? = null,
    val durationMinutes: Int = 0,
    
    // Context (for pattern detection)
    val hourOfDay: Int,
    val dayOfWeek: Int,
    val wasLate: Boolean = false,
    val wasSkipped: Boolean = false,
    
    // Consequences applied
    val timeDebtAdded: Int = 0, // Minutes of debt added
    val appsLocked: List<String> = emptyList(),
    val boringModeEnabled: Boolean = false,
    
    // Anti-cheat
    val suspicionScore: Float = 0f, // 0-1, higher = more suspicious
    val completionSpeed: Float = 0f, // Actual time / Expected time
    
    // Notes
    val note: String? = null
)

enum class ExecutionStatus {
    PENDING,        // Rule triggered, waiting for completion
    COMPLETED,      // Successfully completed
    MISSED,         // Missed deadline
    SKIPPED,        // Used skip token
    PARTIAL,        // Partially completed
    CHEATED         // Detected as cheating
}
