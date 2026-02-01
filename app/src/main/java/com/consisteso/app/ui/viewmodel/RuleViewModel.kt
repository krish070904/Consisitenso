package com.consisteso.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.consisteso.app.data.local.ConsistEsoDatabase
import com.consisteso.app.data.model.*
import com.consisteso.app.data.repository.RuleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * ViewModel for Rule management
 */
class RuleViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = ConsistEsoDatabase.getDatabase(application)
    private val ruleRepository = RuleRepository(
        database.ruleDao(),
        database.executionDao(),
        database.timeDebtDao()
    )
    
    val allRules: StateFlow<List<Rule>> = ruleRepository.getAllRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _selectedRule = MutableStateFlow<Rule?>(null)
    val selectedRule: StateFlow<Rule?> = _selectedRule.asStateFlow()
    
    /**
     * Create a new rule
     */
    fun createRule(
        name: String,
        description: String,
        triggerType: TriggerType,
        triggerTime: LocalTime? = null,
        triggerDays: List<DayOfWeek> = emptyList(),
        actionType: ActionType,
        actionDescription: String,
        estimatedDurationMinutes: Int,
        consequenceType: ConsequenceType,
        timeDebtMultiplier: Float = 1.5f,
        lockedApps: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val rule = Rule(
                name = name,
                description = description,
                triggerType = triggerType,
                triggerTime = triggerTime,
                triggerDays = triggerDays,
                actionType = actionType,
                actionDescription = actionDescription,
                estimatedDurationMinutes = estimatedDurationMinutes,
                consequenceType = consequenceType,
                timeDebtMultiplier = timeDebtMultiplier,
                lockedApps = lockedApps
            )
            ruleRepository.createRule(rule)
        }
    }
    
    /**
     * Update existing rule
     */
    fun updateRule(rule: Rule) {
        viewModelScope.launch {
            ruleRepository.updateRule(rule)
        }
    }
    
    /**
     * Delete rule
     */
    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            ruleRepository.deleteRule(ruleId)
        }
    }
    
    /**
     * Toggle rule active status
     */
    fun toggleRuleActive(ruleId: Long, isActive: Boolean) {
        viewModelScope.launch {
            ruleRepository.toggleRuleActive(ruleId, isActive)
        }
    }
    
    /**
     * Select rule for viewing/editing
     */
    fun selectRule(ruleId: Long) {
        viewModelScope.launch {
            ruleRepository.getRuleById(ruleId).collect { rule ->
                _selectedRule.value = rule
            }
        }
    }
    
    /**
     * Clear selected rule
     */
    fun clearSelection() {
        _selectedRule.value = null
    }
}
