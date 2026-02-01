package com.consisteso.app.data.local.dao

import androidx.room.*
import com.consisteso.app.data.model.Execution
import com.consisteso.app.data.model.ExecutionStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Execution tracking
 */
@Dao
interface ExecutionDao {
    
    @Query("SELECT * FROM executions ORDER BY timestamp DESC LIMIT 100")
    fun getRecentExecutions(): Flow<List<Execution>>
    
    @Query("SELECT * FROM executions WHERE ruleId = :ruleId ORDER BY timestamp DESC")
    fun getExecutionsForRule(ruleId: Long): Flow<List<Execution>>
    
    @Query("SELECT * FROM executions WHERE id = :executionId")
    suspend fun getExecutionById(executionId: Long): Execution?
    
    @Query("SELECT * FROM executions WHERE status = :status ORDER BY timestamp DESC")
    fun getExecutionsByStatus(status: ExecutionStatus): Flow<List<Execution>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: Execution): Long
    
    @Update
    suspend fun updateExecution(execution: Execution)
    
    @Delete
    suspend fun deleteExecution(execution: Execution)
    
    @Query("SELECT * FROM executions WHERE status = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingExecutions(): Flow<List<Execution>>
    
    @Query("UPDATE executions SET status = :status, completedAt = :completedAt WHERE id = :executionId")
    suspend fun updateExecutionStatus(executionId: Long, status: ExecutionStatus, completedAt: Long)
    
    @Query("SELECT COUNT(*) FROM executions WHERE ruleId = :ruleId AND status = 'COMPLETED'")
    suspend fun getCompletionCount(ruleId: Long): Int
    
    @Query("SELECT COUNT(*) FROM executions WHERE ruleId = :ruleId AND status = 'MISSED'")
    suspend fun getMissCount(ruleId: Long): Int
    
    @Query("SELECT AVG(suspicionScore) FROM executions WHERE ruleId = :ruleId AND timestamp > :since")
    suspend fun getAverageSuspicionScore(ruleId: Long, since: Long): Float?
    
    @Query("SELECT * FROM executions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getExecutionsInRange(startTime: Long, endTime: Long): Flow<List<Execution>>
    
    @Query("DELETE FROM executions WHERE timestamp < :before")
    suspend fun deleteExecutionsBefore(before: Long)
    
    // Pattern detection queries
    @Query("SELECT hourOfDay, COUNT(*) as count FROM executions WHERE status = 'MISSED' GROUP BY hourOfDay ORDER BY count DESC LIMIT 5")
    suspend fun getTopFailureHours(): List<HourCount>
    
    @Query("SELECT dayOfWeek, COUNT(*) as count FROM executions WHERE status = 'MISSED' GROUP BY dayOfWeek ORDER BY count DESC LIMIT 3")
    suspend fun getTopFailureDays(): List<DayCount>
}

data class HourCount(val hourOfDay: Int, val count: Int)
data class DayCount(val dayOfWeek: Int, val count: Int)
