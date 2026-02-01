package com.consisteso.app.data.local.dao

import androidx.room.*
import com.consisteso.app.data.model.SystemState
import com.consisteso.app.data.model.BehaviorPattern
import com.consisteso.app.data.model.PatternType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for System State management
 */
@Dao
interface SystemStateDao {
    
    @Query("SELECT * FROM system_state WHERE id = 1")
    fun getSystemState(): Flow<SystemState?>
    
    @Query("SELECT * FROM system_state WHERE id = 1")
    suspend fun getSystemStateSnapshot(): SystemState?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemState(state: SystemState)
    
    @Update
    suspend fun updateSystemState(state: SystemState)
    
    @Query("UPDATE system_state SET isBoringModeActive = :active, boringModeLevel = :level WHERE id = 1")
    suspend fun setBoringMode(active: Boolean, level: Int)
    
    @Query("UPDATE system_state SET musicUnlocked = :unlocked WHERE id = 1")
    suspend fun setMusicUnlocked(unlocked: Boolean)
    
    @Query("UPDATE system_state SET youtubeUnlocked = :unlocked WHERE id = 1")
    suspend fun setYoutubeUnlocked(unlocked: Boolean)
    
    @Query("UPDATE system_state SET colorModeUnlocked = :unlocked WHERE id = 1")
    suspend fun setColorModeUnlocked(unlocked: Boolean)
    
    @Query("UPDATE system_state SET skipTokensAvailable = skipTokensAvailable + 1 WHERE id = 1")
    suspend fun addSkipToken()
    
    @Query("UPDATE system_state SET skipTokensAvailable = skipTokensAvailable - 1 WHERE id = 1 AND skipTokensAvailable > 0")
    suspend fun useSkipToken()
    
    @Query("UPDATE system_state SET globalSuspicionScore = :score WHERE id = 1")
    suspend fun updateSuspicionScore(score: Float)
    
    @Query("UPDATE system_state SET uninstallAttempts = uninstallAttempts + 1 WHERE id = 1")
    suspend fun incrementUninstallAttempts()
    
    // Behavior patterns
    @Query("SELECT * FROM behavior_patterns ORDER BY confidence DESC, occurrenceCount DESC")
    fun getAllPatterns(): Flow<List<BehaviorPattern>>
    
    @Query("SELECT * FROM behavior_patterns WHERE patternType = :type")
    fun getPatternsByType(type: PatternType): Flow<List<BehaviorPattern>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: BehaviorPattern): Long
    
    @Update
    suspend fun updatePattern(pattern: BehaviorPattern)
    
    @Query("UPDATE behavior_patterns SET occurrenceCount = occurrenceCount + 1, lastOccurrence = :timestamp WHERE id = :patternId")
    suspend fun incrementPatternOccurrence(patternId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM behavior_patterns WHERE confidence < 0.3")
    suspend fun deleteLowConfidencePatterns()
}
