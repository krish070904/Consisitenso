package com.consisteso.app.data.local.dao

import androidx.room.*
import com.consisteso.app.data.model.Reward
import com.consisteso.app.data.model.RewardType
import com.consisteso.app.data.model.SkipToken
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Reward management
 */
@Dao
interface RewardDao {
    
    @Query("SELECT * FROM rewards ORDER BY isUnlocked DESC, type ASC")
    fun getAllRewards(): Flow<List<Reward>>
    
    @Query("SELECT * FROM rewards WHERE isUnlocked = 1")
    fun getUnlockedRewards(): Flow<List<Reward>>
    
    @Query("SELECT * FROM rewards WHERE type = :type")
    suspend fun getRewardByType(type: RewardType): Reward?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: Reward): Long
    
    @Update
    suspend fun updateReward(reward: Reward)
    
    @Query("UPDATE rewards SET isUnlocked = :unlocked, unlockedAt = :timestamp WHERE type = :type")
    suspend fun setRewardUnlocked(type: RewardType, unlocked: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE rewards SET timesUsed = timesUsed + 1, lastUsedAt = :timestamp WHERE type = :type")
    suspend fun incrementRewardUsage(type: RewardType, timestamp: Long = System.currentTimeMillis())
    
    // Skip tokens
    @Query("SELECT * FROM skip_tokens WHERE isUsed = 0 ORDER BY earnedAt DESC")
    fun getAvailableSkipTokens(): Flow<List<SkipToken>>
    
    @Query("SELECT COUNT(*) FROM skip_tokens WHERE isUsed = 0")
    suspend fun getAvailableSkipTokenCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkipToken(token: SkipToken): Long
    
    @Query("UPDATE skip_tokens SET isUsed = 1, usedAt = :timestamp, usedOnRuleId = :ruleId WHERE id = :tokenId")
    suspend fun useSkipToken(tokenId: Long, ruleId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM skip_tokens ORDER BY earnedAt DESC")
    fun getAllSkipTokens(): Flow<List<SkipToken>>
}
