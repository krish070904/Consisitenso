package com.consisteso.app.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.time.LocalTime
import java.time.Duration
import java.time.LocalDateTime

/**
 * Schedules all background workers
 * 
 * Workers:
 * 1. RuleEvaluationWorker - Every 15 minutes
 * 2. DailyRewardWorker - Daily at 11:59 PM
 * 
 * Philosophy: Relentless background enforcement
 */
object WorkerScheduler {
    
    private const val RULE_EVALUATION_WORK = "rule_evaluation_work"
    private const val DAILY_REWARD_WORK = "daily_reward_work"
    
    /**
     * Schedule all workers
     */
    fun scheduleAllWorkers(context: Context) {
        scheduleRuleEvaluation(context)
        scheduleDailyReward(context)
    }
    
    /**
     * Schedule rule evaluation every 15 minutes
     */
    private fun scheduleRuleEvaluation(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false) // Run even on low battery
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<RuleEvaluationWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RULE_EVALUATION_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * Schedule daily reward calculation at 11:59 PM
     */
    private fun scheduleDailyReward(context: Context) {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(23, 59)
        var targetDateTime = now.with(targetTime)
        
        // If target time has passed today, schedule for tomorrow
        if (now.isAfter(targetDateTime)) {
            targetDateTime = targetDateTime.plusDays(1)
        }
        
        val initialDelay = Duration.between(now, targetDateTime).toMinutes()
        
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<DailyRewardWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REWARD_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * Cancel all workers (for testing or emergency exit)
     */
    fun cancelAllWorkers(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(RULE_EVALUATION_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REWARD_WORK)
    }
}
