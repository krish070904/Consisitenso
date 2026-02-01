package com.consisteso.app.data.repository

import com.consisteso.app.data.local.dao.*
import com.consisteso.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Repository for Rule operations
 * Handles business logic for rule management
 */
class RuleRepository(
    private val ruleDao: RuleDao,
    private val executionDao: ExecutionDao,
    private val timeDebtDao: TimeDebtDao
) {
    
    fun getAllActiveRules(): Flow<List<Rule>> = ruleDao.getAllActiveRules()
    
    fun getAllRules(): Flow<List<Rule>> = ruleDao.getAllRules()
    
    fun getRuleById(ruleId: Long): Flow<Rule?> = ruleDao.getRuleByIdFlow(ruleId)
    
    suspend fun createRule(rule: Rule): Long {
        return ruleDao.insertRule(rule)
    }
    
    suspend fun updateRule(rule: Rule) {
        ruleDao.updateRule(rule)
    }
    
    suspend fun deleteRule(ruleId: Long) {
        ruleDao.deleteRuleById(ruleId)
    }
    
    suspend fun toggleRuleActive(ruleId: Long, isActive: Boolean) {
        ruleDao.setRuleActive(ruleId, isActive)
    }
    
    /**
     * Check if a rule should trigger now
     */
    suspend fun shouldRuleTrigger(rule: Rule, currentTime: LocalDateTime): Boolean {
        val currentDay = currentTime.dayOfWeek
        
        // Check if rule applies today
        if (rule.triggerDays.isNotEmpty() && !rule.triggerDays.contains(currentDay)) {
            return false
        }
        
        return when (rule.triggerType) {
            TriggerType.TIME -> {
                rule.triggerTime?.let { triggerTime ->
                    currentTime.toLocalTime().isAfter(triggerTime.minusMinutes(5)) &&
                    currentTime.toLocalTime().isBefore(triggerTime.plusMinutes(5))
                } ?: false
            }
            TriggerType.DAILY -> {
                // Check if already completed today
                !hasCompletedToday(rule.id, currentTime)
            }
            else -> false
        }
    }
    
    /**
     * Create execution for triggered rule
     */
    suspend fun createExecution(rule: Rule): Long {
        val now = LocalDateTime.now()
        val execution = Execution(
            ruleId = rule.id,
            timestamp = System.currentTimeMillis(),
            status = ExecutionStatus.PENDING,
            hourOfDay = now.hour,
            dayOfWeek = now.dayOfWeek.value
        )
        return executionDao.insertExecution(execution)
    }
    
    /**
     * Complete an execution
     */
    suspend fun completeExecution(
        executionId: Long,
        durationMinutes: Int,
        note: String? = null
    ) {
        val execution = executionDao.getExecutionById(executionId) ?: return
        val rule = ruleDao.getRuleById(execution.ruleId) ?: return
        
        val now = System.currentTimeMillis()
        val completionSpeed = durationMinutes.toFloat() / rule.estimatedDurationMinutes
        
        // Calculate suspicion score
        val suspicionScore = calculateSuspicionScore(completionSpeed, durationMinutes)
        
        val updatedExecution = execution.copy(
            status = if (suspicionScore > 0.7f) ExecutionStatus.CHEATED else ExecutionStatus.COMPLETED,
            completedAt = now,
            durationMinutes = durationMinutes,
            completionSpeed = completionSpeed,
            suspicionScore = suspicionScore,
            note = note
        )
        
        executionDao.updateExecution(updatedExecution)
        
        // Update rule stats
        if (suspicionScore <= 0.7f) {
            ruleDao.incrementCompletion(rule.id)
            val currentRule = ruleDao.getRuleById(rule.id)
            currentRule?.let {
                if (it.currentStreak > it.longestStreak) {
                    ruleDao.updateLongestStreak(it.id, it.currentStreak)
                }
            }
        }
    }
    
    /**
     * Mark execution as missed and apply consequences
     */
    suspend fun missExecution(executionId: Long) {
        val execution = executionDao.getExecutionById(executionId) ?: return
        val rule = ruleDao.getRuleById(execution.ruleId) ?: return
        
        val updatedExecution = execution.copy(
            status = ExecutionStatus.MISSED
        )
        executionDao.updateExecution(updatedExecution)
        
        // Update rule stats
        ruleDao.incrementMiss(rule.id)
        
        // Apply consequences
        applyConsequences(rule, execution)
    }
    
    /**
     * Apply consequences for missed rule
     */
    private suspend fun applyConsequences(rule: Rule, execution: Execution) {
        when (rule.consequenceType) {
            ConsequenceType.TIME_DEBT -> {
                val debtMinutes = (rule.estimatedDurationMinutes * rule.timeDebtMultiplier).toInt()
                timeDebtDao.addDebt(debtMinutes)
                
                // Record transaction
                val currentDebt = timeDebtDao.getCurrentDebtSnapshot()
                val transaction = DebtTransaction(
                    type = DebtTransactionType.ACCRUED,
                    amountMinutes = debtMinutes,
                    multiplier = rule.timeDebtMultiplier,
                    reason = "Missed: ${rule.name}",
                    ruleId = rule.id,
                    executionId = execution.id,
                    balanceBefore = currentDebt?.activeDebtMinutes ?: 0,
                    balanceAfter = (currentDebt?.activeDebtMinutes ?: 0) + debtMinutes
                )
                timeDebtDao.insertTransaction(transaction)
            }
            else -> {
                // Other consequences handled by SystemStateRepository
            }
        }
    }
    
    /**
     * Calculate suspicion score based on completion behavior
     */
    private fun calculateSuspicionScore(completionSpeed: Float, durationMinutes: Int): Float {
        var score = 0f
        
        // Too fast completion
        if (completionSpeed < 0.3f) {
            score += 0.5f
        }
        
        // Suspiciously short duration
        if (durationMinutes < 2) {
            score += 0.3f
        }
        
        // Instant completion
        if (durationMinutes == 0) {
            score = 1.0f
        }
        
        return score.coerceIn(0f, 1f)
    }
    
    /**
     * Check if rule was completed today
     */
    private suspend fun hasCompletedToday(ruleId: Long, currentTime: LocalDateTime): Boolean {
        val startOfDay = currentTime.toLocalDate().atStartOfDay()
        val endOfDay = currentTime.toLocalDate().atTime(LocalTime.MAX)
        
        val executions = executionDao.getExecutionsInRange(
            startOfDay.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
            endOfDay.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        ).first()
        
        return executions.any { 
            it.ruleId == ruleId && it.status == ExecutionStatus.COMPLETED 
        }
    }
}
