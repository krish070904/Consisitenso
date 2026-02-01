package com.consisteso.app.core.engine

import android.content.Context
import com.consisteso.app.data.local.ConsistEsoDatabase
import com.consisteso.app.data.model.*
import com.consisteso.app.data.repository.RuleRepository
import com.consisteso.app.data.repository.SystemStateRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * RULE ENGINE - The Brain
 * 
 * This is the core enforcement system.
 * It evaluates rules, triggers consequences, and maintains the trap.
 * 
 * Philosophy: Silent, relentless, fair.
 */
class RuleEngine(
    private val ruleRepository: RuleRepository,
    private val systemStateRepository: SystemStateRepository,
    private val context: Context
) {
    
    /**
     * Evaluate all active rules
     * Called periodically by WorkManager
     */
    suspend fun evaluateAllRules() {
        val activeRules = ruleRepository.getAllActiveRules().first()
        val currentTime = LocalDateTime.now()
        
        for (rule in activeRules) {
            evaluateRule(rule, currentTime)
        }
    }
    
    /**
     * Evaluate a single rule
     */
    private suspend fun evaluateRule(rule: Rule, currentTime: LocalDateTime) {
        // Check if rule should trigger
        if (ruleRepository.shouldRuleTrigger(rule, currentTime)) {
            // Create pending execution
            val executionId = ruleRepository.createExecution(rule)
            
            // Schedule deadline check
            scheduleDeadlineCheck(executionId, rule)
        }
    }
    
    /**
     * Check if execution deadline has passed
     */
    suspend fun checkDeadlines() {
        val database = ConsistEsoDatabase.getDatabase(context)
        val pendingExecutions = database.executionDao().getPendingExecutions().first()
        
        val now = System.currentTimeMillis()
        
        for (execution in pendingExecutions) {
            val rule = database.ruleDao().getRuleById(execution.ruleId) ?: continue
            
            // Calculate deadline
            val deadline = execution.timestamp + (rule.estimatedDurationMinutes * 60 * 1000)
            
            if (now > deadline) {
                // Deadline passed - mark as missed
                ruleRepository.missExecution(execution.id)
                
                // Show subtle notification (not aggressive)
                showDeadlineMissedNotification(rule)
            }
        }
    }
    
    /**
     * Apply consequences for rule violation
     * This is where the trap tightens
     */
    suspend fun applyConsequences(rule: Rule, execution: Execution) {
        when (rule.consequenceType) {
            ConsequenceType.TIME_DEBT -> {
                // Already handled in RuleRepository
            }
            
            ConsequenceType.APP_LOCK -> {
                // Lock specified apps
                rule.lockedApps.forEach { packageName ->
                    systemStateRepository.lockApp(packageName)
                }
            }
            
            ConsequenceType.BORING_MODE -> {
                // Enable boring mode
                systemStateRepository.setBoringMode(
                    active = true,
                    level = 1,
                    reason = "Missed: ${rule.name}"
                )
            }
            
            ConsequenceType.ESCALATION -> {
                // Increase difficulty
                escalateConsequences(rule)
            }
            
            ConsequenceType.NONE -> {
                // No consequence
            }
        }
    }
    
    /**
     * Escalate consequences based on repeated failures
     */
    private suspend fun escalateConsequences(rule: Rule) {
        val missRate = if (rule.totalCompletions + rule.totalMisses > 0) {
            rule.totalMisses.toFloat() / (rule.totalCompletions + rule.totalMisses)
        } else {
            0f
        }
        
        when {
            missRate > 0.7f -> {
                // High failure rate - maximum punishment
                systemStateRepository.setBoringMode(active = true, level = 3)
                systemStateRepository.setMusicUnlocked(false)
                systemStateRepository.setYoutubeUnlocked(false)
                systemStateRepository.setColorModeUnlocked(false)
            }
            missRate > 0.5f -> {
                // Medium failure rate - moderate punishment
                systemStateRepository.setBoringMode(active = true, level = 2)
                systemStateRepository.setMusicUnlocked(false)
            }
            missRate > 0.3f -> {
                // Low failure rate - light punishment
                systemStateRepository.setBoringMode(active = true, level = 1)
            }
        }
    }
    
    /**
     * Calculate daily rewards
     * Called at end of day
     */
    suspend fun calculateDailyRewards() {
        val database = ConsistEsoDatabase.getDatabase(context)
        val systemState = systemStateRepository.getSystemStateSnapshot() ?: return
        
        // Get today's executions
        val today = LocalDateTime.now()
        val startOfDay = today.toLocalDate().atStartOfDay()
        val endOfDay = today.toLocalDate().atTime(LocalTime.MAX)
        
        val todayExecutions = database.executionDao().getExecutionsInRange(
            startOfDay.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
            endOfDay.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        ).first()
        
        // Check if day was perfect
        val isPerfectDay = todayExecutions.all { 
            it.status == ExecutionStatus.COMPLETED || it.status == ExecutionStatus.SKIPPED 
        }
        
        if (isPerfectDay) {
            // Unlock daily rewards
            systemStateRepository.setMusicUnlocked(true)
            systemStateRepository.setYoutubeUnlocked(true)
            systemStateRepository.setColorModeUnlocked(true)
            
            // Update perfect days counter
            val updatedState = systemState.copy(
                currentPerfectDays = systemState.currentPerfectDays + 1,
                longestPerfectStreak = maxOf(
                    systemState.longestPerfectStreak,
                    systemState.currentPerfectDays + 1
                )
            )
            database.systemStateDao().updateSystemState(updatedState)
            
            // Award skip token after 7 perfect days
            if (updatedState.currentPerfectDays % 7 == 0) {
                systemStateRepository.addSkipToken()
                showSkipTokenEarnedNotification()
            }
            
            // Disable boring mode
            systemStateRepository.setBoringMode(active = false, level = 0)
            
        } else {
            // Imperfect day - lock rewards
            systemStateRepository.setMusicUnlocked(false)
            systemStateRepository.setYoutubeUnlocked(false)
            
            // Reset perfect days counter
            val updatedState = systemState.copy(currentPerfectDays = 0)
            database.systemStateDao().updateSystemState(updatedState)
        }
    }
    
    /**
     * Detect cheating patterns
     */
    suspend fun detectCheating(execution: Execution) {
        if (execution.suspicionScore > 0.7f) {
            // High suspicion - apply silent punishment
            systemStateRepository.updateSuspicionScore(0.2f)
        }
        
        // Check for rapid marking pattern
        val database = ConsistEsoDatabase.getDatabase(context)
        val recentExecutions = database.executionDao().getRecentExecutions().first().take(5)
        
        val rapidMarking = recentExecutions.count { 
            it.durationMinutes < 2 
        } >= 3
        
        if (rapidMarking) {
            // Detected rapid marking - silent punishment
            systemStateRepository.updateSuspicionScore(0.3f)
            
            // Store pattern
            val pattern = BehaviorPattern(
                patternType = PatternType.RAPID_MARKING,
                description = "Marks tasks too quickly",
                occurrenceCount = 1,
                confidence = 0.9f
            )
            database.systemStateDao().insertPattern(pattern)
        }
    }
    
    /**
     * Schedule deadline check
     */
    private fun scheduleDeadlineCheck(executionId: Long, rule: Rule) {
        // This would use WorkManager to schedule a check
        // Implementation in WorkManager section
    }
    
    /**
     * Show subtle notification (not aggressive)
     */
    private fun showDeadlineMissedNotification(rule: Rule) {
        // Subtle, factual notification
        // "Rule missed: ${rule.name}"
        // No emotion, no motivation
    }
    
    /**
     * Show skip token earned notification
     */
    private fun showSkipTokenEarnedNotification() {
        // Rare dopamine shot
        // "Skip token earned. This week was statistically rare."
    }
}
