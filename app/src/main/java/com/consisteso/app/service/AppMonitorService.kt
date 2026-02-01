package com.consisteso.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.lifecycleScope
import com.consisteso.app.data.local.ConsistEsoDatabase
import com.consisteso.app.data.repository.SystemStateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Accessibility Service for App Monitoring and Blocking
 * 
 * This service:
 * 1. Monitors which apps are opened
 * 2. Blocks locked apps (no error, just closes them)
 * 3. Tracks app usage for pattern detection
 * 4. Enforces boring mode restrictions
 * 
 * Philosophy: Silent enforcement. No warnings. Just unavailable.
 */
class AppMonitorService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: ConsistEsoDatabase
    private lateinit var systemStateRepository: SystemStateRepository
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // Initialize database and repository
        database = ConsistEsoDatabase.getDatabase(applicationContext)
        systemStateRepository = SystemStateRepository(
            database.systemStateDao(),
            database.executionDao()
        )
        
        // Configure accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore our own app
            if (packageName == applicationContext.packageName) return
            
            // Check if app should be blocked
            serviceScope.launch {
                checkAndBlockApp(packageName)
            }
        }
    }
    
    /**
     * Check if app is locked and block it if necessary
     */
    private suspend fun checkAndBlockApp(packageName: String) {
        val systemState = systemStateRepository.getSystemStateSnapshot() ?: return
        
        // Check if app is in locked list
        if (systemState.lockedApps.contains(packageName)) {
            blockApp(packageName)
            return
        }
        
        // Check boring mode restrictions
        if (systemState.isBoringModeActive) {
            if (isEntertainmentApp(packageName)) {
                blockApp(packageName)
                return
            }
        }
        
        // Check if rewards are locked
        if (!systemState.musicUnlocked && isMusicApp(packageName)) {
            blockApp(packageName)
            return
        }
        
        if (!systemState.youtubeUnlocked && isVideoApp(packageName)) {
            blockApp(packageName)
            return
        }
    }
    
    /**
     * Block app by returning to home screen
     * No error message. Just... unavailable.
     */
    private fun blockApp(packageName: String) {
        // Return to home screen
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        
        // Log the block attempt (for pattern detection)
        serviceScope.launch {
            logBlockAttempt(packageName)
        }
    }
    
    /**
     * Log blocked app attempt for pattern detection
     */
    private suspend fun logBlockAttempt(packageName: String) {
        // This could be stored in a separate table for analysis
        // For now, we'll just track it mentally
    }
    
    /**
     * Check if app is entertainment
     */
    private fun isEntertainmentApp(packageName: String): Boolean {
        val entertainmentApps = listOf(
            "com.instagram.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.snapchat.android",
            "com.zhiliaoapp.musically", // TikTok
            "com.reddit.frontpage",
            "com.netflix.mediaclient",
            "com.amazon.avod.thirdpartyclient", // Prime Video
            "com.disney.disneyplus"
        )
        return entertainmentApps.any { packageName.contains(it, ignoreCase = true) }
    }
    
    /**
     * Check if app is music
     */
    private fun isMusicApp(packageName: String): Boolean {
        val musicApps = listOf(
            "com.spotify.music",
            "com.google.android.apps.youtube.music",
            "com.apple.android.music",
            "com.amazon.mp3",
            "deezer.android.app"
        )
        return musicApps.any { packageName.contains(it, ignoreCase = true) }
    }
    
    /**
     * Check if app is video
     */
    private fun isVideoApp(packageName: String): Boolean {
        val videoApps = listOf(
            "com.google.android.youtube",
            "com.netflix.mediaclient",
            "com.amazon.avod.thirdpartyclient",
            "com.disney.disneyplus",
            "tv.twitch.android.app"
        )
        return videoApps.any { packageName.contains(it, ignoreCase = true) }
    }
    
    override fun onInterrupt() {
        // Service interrupted
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cleanup if needed
    }
}
