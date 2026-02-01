package com.consisteso.app.data.local.dao

import androidx.room.*
import com.consisteso.app.data.model.TimeDebt
import com.consisteso.app.data.model.DebtTransaction
import com.consisteso.app.data.model.DebtTransactionType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Time Debt management
 */
@Dao
interface TimeDebtDao {
    
    @Query("SELECT * FROM time_debt ORDER BY id DESC LIMIT 1")
    fun getCurrentDebt(): Flow<TimeDebt?>
    
    @Query("SELECT * FROM time_debt ORDER BY id DESC LIMIT 1")
    suspend fun getCurrentDebtSnapshot(): TimeDebt?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: TimeDebt): Long
    
    @Update
    suspend fun updateDebt(debt: TimeDebt)
    
    @Query("UPDATE time_debt SET activeDebtMinutes = activeDebtMinutes + :minutes, totalDebtMinutes = totalDebtMinutes + :minutes, lastUpdated = :timestamp WHERE id = (SELECT id FROM time_debt ORDER BY id DESC LIMIT 1)")
    suspend fun addDebt(minutes: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE time_debt SET activeDebtMinutes = activeDebtMinutes - :minutes, clearedDebtMinutes = clearedDebtMinutes + :minutes, lastUpdated = :timestamp, lastDebtCleared = :timestamp WHERE id = (SELECT id FROM time_debt ORDER BY id DESC LIMIT 1)")
    suspend fun clearDebt(minutes: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE time_debt SET currentMultiplier = :multiplier WHERE id = (SELECT id FROM time_debt ORDER BY id DESC LIMIT 1)")
    suspend fun updateMultiplier(multiplier: Float)
    
    // Debt transactions
    @Query("SELECT * FROM debt_transactions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTransactions(): Flow<List<DebtTransaction>>
    
    @Query("SELECT * FROM debt_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: DebtTransactionType): Flow<List<DebtTransaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DebtTransaction): Long
    
    @Query("SELECT SUM(amountMinutes) FROM debt_transactions WHERE type = 'ACCRUED' AND timestamp >= :since")
    suspend fun getTotalDebtAccruedSince(since: Long): Int?
    
    @Query("SELECT SUM(amountMinutes) FROM debt_transactions WHERE type = 'CLEARED' AND timestamp >= :since")
    suspend fun getTotalDebtClearedSince(since: Long): Int?
}
