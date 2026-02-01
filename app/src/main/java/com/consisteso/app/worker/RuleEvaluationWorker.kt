package com.consisteso.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.consisteso.app.core.engine.RuleEngine
import com.consisteso.app.data.local.ConsistEsoDatabase
import com.consisteso.app.data.repository.RuleRepository
import com.consisteso.app.data.repository.SystemStateRepository

/**
 * WorkManager worker for periodic rule evaluation
 * 
 * Runs every 15 minutes to:
 * 1. Evaluate all active rules
 * 2. Check execution deadlines
 * 3. Apply consequences
 * 4. Update system state
 * 
 * Philosophy: Silent, relentless, automatic
 */
class RuleEvaluationWorker(
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
            
            // Evaluate all rules
            ruleEngine.evaluateAllRules()
            
            // Check deadlines
            ruleEngine.checkDeadlines()
            
            Result.success()
        } catch (e: Exception) {
            // Retry on failure
            Result.retry()
        }
    }
}
