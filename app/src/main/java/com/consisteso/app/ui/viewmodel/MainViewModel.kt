package com.consisteso.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.consisteso.app.data.local.ConsistEsoDatabase
import com.consisteso.app.data.model.*
import com.consisteso.app.data.repository.RuleRepository
import com.consisteso.app.data.repository.SystemStateRepository
import com.consisteso.app.core.engine.RuleEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Main ViewModel
 * Manages overall app state
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = ConsistEsoDatabase.getDatabase(application)
    private val ruleRepository = RuleRepository(
        database.ruleDao(),
        database.executionDao(),
        database.timeDebtDao()
    )
    private val systemStateRepository = SystemStateRepository(
        database.systemStateDao(),
        database.executionDao()
    )
    private val ruleEngine = RuleEngine(
        ruleRepository,
        systemStateRepository,
        application
    )
    
    // System state
    val systemState: StateFlow<SystemState?> = systemStateRepository.getSystemState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Active rules
    val activeRules: StateFlow<List<Rule>> = ruleRepository.getAllActiveRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Pending executions
    val pendingExecutions: StateFlow<List<Execution>> = database.executionDao()
        .getPendingExecutions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Time debt
    val currentDebt: StateFlow<TimeDebt?> = database.timeDebtDao()
        .getCurrentDebt()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Recent executions (for execution graph)
    val recentExecutions: StateFlow<List<Execution>> = database.executionDao()
        .getRecentExecutions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // UI state
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        // Initialize system if needed
        viewModelScope.launch {
            val state = systemStateRepository.getSystemStateSnapshot()
            if (state == null) {
                // First launch - initialize
                initializeSystem()
            }
        }
    }
    
    private suspend fun initializeSystem() {
        // System is initialized in database callback
        // This is just a placeholder for any additional setup
    }
    
    /**
     * Complete an execution
     */
    fun completeExecution(executionId: Long, durationMinutes: Int, note: String? = null) {
        viewModelScope.launch {
            ruleRepository.completeExecution(executionId, durationMinutes, note)
            
            // Detect cheating
            val execution = database.executionDao().getExecutionById(executionId)
            execution?.let {
                ruleEngine.detectCheating(it)
            }
        }
    }
    
    /**
     * Use skip token on execution
     */
    fun useSkipToken(executionId: Long) {
        viewModelScope.launch {
            val canUse = systemStateRepository.useSkipToken()
            if (canUse) {
                val execution = database.executionDao().getExecutionById(executionId)
                execution?.let {
                    val updated = it.copy(
                        status = ExecutionStatus.SKIPPED,
                        wasSkipped = true,
                        completedAt = System.currentTimeMillis()
                    )
                    database.executionDao().updateExecution(updated)
                }
            }
        }
    }
    
    /**
     * Evaluate all rules (manual trigger)
     */
    fun evaluateRules() {
        viewModelScope.launch {
            ruleEngine.evaluateAllRules()
        }
    }
    
    /**
     * Check deadlines (manual trigger)
     */
    fun checkDeadlines() {
        viewModelScope.launch {
            ruleEngine.checkDeadlines()
        }
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showOnboarding: Boolean = false
)
