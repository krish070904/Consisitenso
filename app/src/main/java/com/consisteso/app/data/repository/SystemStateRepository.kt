package com.consisteso.app.data.repository

import com.consisteso.app.data.local.dao.SystemStateDao
import com.consisteso.app.data.local.dao.ExecutionDao
import com.consisteso.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository for System State management
 * Controls boring mode, lockouts, rewards, anti-cheat
 */
class SystemStateRepository(
    private val systemStateDao: SystemStateDao,
    private val executionDao: ExecutionDao
) {
    
    fun getSystemState(): Flow<SystemState?> = systemStateDao.getSystemState()
    
    suspend fun getSystemStateSnapshot(): SystemState? = systemStateDao.getSystemStateSnapshot()
    
    /**
     * Enable/disable boring mode
     */
    suspend fun setBoringMode(active: Boolean, level: Int = 1, reason: String? = null) {
        val state = systemStateDao.getSystemStateSnapshot() ?: return
        val updatedState = state.copy(
            isBoringModeActive = active,
            boringModeLevel = level,
            boringModeReason = reason
        )
        systemStateDao.updateSystemState(updatedState)
    }
    
    /**
     * Lock/unlock specific apps
     */
    suspend fun lockApp(packageName: String) {
        val state = systemStateDao.getSystemStateSnapshot() ?: return
        val updatedLockedApps = state.lockedApps.toMutableList().apply {
            if (!contains(packageName)) add(packageName)
        }
        val updatedState = state.copy(lockedApps = updatedLockedApps)
        systemStateDao.updateSystemState(updatedState)
    }
    
    suspend fun unlockApp(packageName: String) {
        val state = systemStateDao.getSystemStateSnapshot() ?: return
        val updatedLockedApps = state.lockedApps.toMutableList().apply {
            remove(packageName)
        }
        val updatedState = state.copy(lockedApps = updatedLockedApps)
        systemStateDao.updateSystemState(updatedState)
    }
    
    /**
     * Update rewards
     */
    suspend fun setMusicUnlocked(unlocked: Boolean) {
        systemStateDao.setMusicUnlocked(unlocked)
    }
    
    suspend fun setYoutubeUnlocked(unlocked: Boolean) {
        systemStateDao.setYoutubeUnlocked(unlocked)
    }
    
    suspend fun setColorModeUnlocked(unlocked: Boolean) {
        systemStateDao.setColorModeUnlocked(unlocked)
    }
    
    /**
     * Skip tokens
     */
    suspend fun addSkipToken() {
        systemStateDao.addSkipToken()
    }
    
    suspend fun useSkipToken(): Boolean {
        val state = systemStateDao.getSystemStateSnapshot() ?: return false
        if (state.skipTokensAvailable > 0) {
            systemStateDao.useSkipToken()
            return true
        }
        return false
    }
    
    /**
     * Anti-cheat: Update suspicion score
     */
    suspend fun updateSuspicionScore(additionalScore: Float) {
        val state = systemStateDao.getSystemStateSnapshot() ?: return
        val newScore = (state.globalSuspicionScore + additionalScore).coerceIn(0f, 1f)
        systemStateDao.updateSuspicionScore(newScore)
        
        // Activate silent punishment if suspicion is high
        if (newScore > 0.6f && !state.silentPunishmentActive) {
            activateSilentPunishment()
        }
    }
    
    /**
     * Activate silent punishment (no warnings)
     */
    private suspend fun activateSilentPunishment() {
        val state = systemStateDao.getSystemStateSnapshot() ?: return
        val updatedState = state.copy(
            silentPunishmentActive = true,
            silentPunishmentMultiplier = 1.3f
        )
        systemStateDao.updateSystemState(updatedState)
    }
    
    /**
     * Detect and store behavior patterns
     */
    suspend fun detectPatterns() {
        // Get failure hours
        val failureHours = executionDao.getTopFailureHours()
        if (failureHours.isNotEmpty()) {
            val pattern = BehaviorPattern(
                patternType = PatternType.NIGHT_FAILURE,
                description = "Fails most at hours: ${failureHours.map { it.hourOfDay }.joinToString()}",
                failureHours = failureHours.map { it.hourOfDay },
                occurrenceCount = failureHours.sumOf { it.count },
                confidence = 0.8f
            )
            systemStateDao.insertPattern(pattern)
        }
        
        // Get failure days
        val failureDays = executionDao.getTopFailureDays()
        if (failureDays.isNotEmpty()) {
            val pattern = BehaviorPattern(
                patternType = PatternType.WEEKEND_FAILURE,
                description = "Fails most on days: ${failureDays.map { it.dayOfWeek }.joinToString()}",
                failureDays = failureDays.map { it.dayOfWeek },
                occurrenceCount = failureDays.sumOf { it.count },
                confidence = 0.75f
            )
            systemStateDao.insertPattern(pattern)
        }
    }
    
    /**
     * Get all detected patterns
     */
    fun getAllPatterns(): Flow<List<BehaviorPattern>> = systemStateDao.getAllPatterns()
    
    /**
     * Handle uninstall attempt
     */
    suspend fun handleUninstallAttempt() {
        systemStateDao.incrementUninstallAttempts()
        
        // Activate boring mode as punishment
        setBoringMode(
            active = true,
            level = 3,
            reason = "Uninstall attempt detected"
        )
    }
}
