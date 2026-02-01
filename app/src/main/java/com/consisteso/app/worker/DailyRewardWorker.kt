package com.consisteso.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.consisteso.app.core.engine.RuleEngine
import com.consisteso.app.data.local.ConsistEsoDatabase
import com.consisteso.app.data.repository.RuleRepository
import com.consisteso.app.data.repository.SystemStateRepository

/**
 * WorkManager worker for daily reward calculation
 * 
 * Runs at end of day (11:59 PM) to:
 * 1. Calculate if day was perfect
 * 2. Award/revoke rewards
 * 3. Award skip tokens (if 7 perfect days)
 * 4. Reset daily counters
 * 5. Detect patterns
 * 
 * Philosophy: Fair but brutal. No partial credit.
 */
class DailyRewardWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val database = ConsistEsoDatabase.getDatabase(applicationContext)
            
            val ruleRepository = RuleRepository(
                database.ruleDao(),
                database.executionDao(),
                database.timeDebtDao()
            )
            
            val systemStateRepository = SystemStateRepository(
                database.systemStateDao(),
                database.executionDao()
            )
            
            val ruleEngine = RuleEngine(
                ruleRepository,
                systemStateRepository,
                applicationContext
            )
            
            // Calculate daily rewards
            ruleEngine.calculateDailyRewards()
            
            // Detect behavior patterns
            systemStateRepository.detectPatterns()
            
            Result.success()
        } catch (e: Exception) {
            // Retry on failure
            Result.retry()
        }
    }
}
