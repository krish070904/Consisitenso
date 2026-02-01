package com.consisteso.app.data.local.dao

import androidx.room.*
import com.consisteso.app.data.model.Rule
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Rule operations
 */
@Dao
interface RuleDao {
    
    @Query("SELECT * FROM rules WHERE isActive = 1 ORDER BY priority DESC, createdAt ASC")
    fun getAllActiveRules(): Flow<List<Rule>>
    
    @Query("SELECT * FROM rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<Rule>>
    
    @Query("SELECT * FROM rules WHERE id = :ruleId")
    suspend fun getRuleById(ruleId: Long): Rule?
    
    @Query("SELECT * FROM rules WHERE id = :ruleId")
    fun getRuleByIdFlow(ruleId: Long): Flow<Rule?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: Rule): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<Rule>)
    
    @Update
    suspend fun updateRule(rule: Rule)
    
    @Delete
    suspend fun deleteRule(rule: Rule)
    
    @Query("DELETE FROM rules WHERE id = :ruleId")
    suspend fun deleteRuleById(ruleId: Long)
    
    @Query("UPDATE rules SET isActive = :isActive WHERE id = :ruleId")
    suspend fun setRuleActive(ruleId: Long, isActive: Boolean)
    
    @Query("UPDATE rules SET totalCompletions = totalCompletions + 1, currentStreak = currentStreak + 1 WHERE id = :ruleId")
    suspend fun incrementCompletion(ruleId: Long)
    
    @Query("UPDATE rules SET totalMisses = totalMisses + 1, currentStreak = 0 WHERE id = :ruleId")
    suspend fun incrementMiss(ruleId: Long)
    
    @Query("UPDATE rules SET longestStreak = :streak WHERE id = :ruleId AND longestStreak < :streak")
    suspend fun updateLongestStreak(ruleId: Long, streak: Int)
    
    @Query("SELECT COUNT(*) FROM rules WHERE isActive = 1")
    suspend fun getActiveRuleCount(): Int
    
    @Query("SELECT * FROM rules WHERE isActive = 1 AND triggerType = 'TIME' ORDER BY priority DESC")
    fun getTimeBasedRules(): Flow<List<Rule>>
}
