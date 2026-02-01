package com.consisteso.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.consisteso.app.data.local.dao.*
import com.consisteso.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room Database
 * Local-only, encrypted storage
 */
@Database(
    entities = [
        Rule::class,
        Execution::class,
        TimeDebt::class,
        DebtTransaction::class,
        SystemState::class,
        BehaviorPattern::class,
        Reward::class,
        SkipToken::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ConsistEsoDatabase : RoomDatabase() {
    
    abstract fun ruleDao(): RuleDao
    abstract fun executionDao(): ExecutionDao
    abstract fun timeDebtDao(): TimeDebtDao
    abstract fun systemStateDao(): SystemStateDao
    abstract fun rewardDao(): RewardDao
    
    companion object {
        @Volatile
        private var INSTANCE: ConsistEsoDatabase? = null
        
        fun getDatabase(context: Context): ConsistEsoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConsistEsoDatabase::class.java,
                    "consisteso_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // For development
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Database callback for initialization
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            // Initialize default data
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
        
        suspend fun populateDatabase(database: ConsistEsoDatabase) {
            // Initialize system state
            val systemState = SystemState(
                id = 1,
                isBoringModeActive = false,
                boringModeLevel = 0,
                colorModeUnlocked = true,
                musicUnlocked = false,
                youtubeUnlocked = false,
                skipTokensAvailable = 0,
                uninstallBlocked = true,
                appInstalledAt = System.currentTimeMillis()
            )
            database.systemStateDao().insertSystemState(systemState)
            
            // Initialize time debt
            val timeDebt = TimeDebt(
                totalDebtMinutes = 0,
                activeDebtMinutes = 0,
                clearedDebtMinutes = 0,
                currentMultiplier = 1.5f,
                maxMultiplier = 3.0f
            )
            database.timeDebtDao().insertDebt(timeDebt)
            
            // Initialize default rewards
            val defaultRewards = listOf(
                Reward(
                    type = RewardType.MUSIC_UNLOCK,
                    name = "Music Access",
                    description = "Unlock music apps",
                    requiresPerfectDays = 1,
                    requiresDebtFree = true
                ),
                Reward(
                    type = RewardType.YOUTUBE_UNLOCK,
                    name = "YouTube Access",
                    description = "Unlock YouTube",
                    requiresPerfectDays = 1,
                    requiresDebtFree = true
                ),
                Reward(
                    type = RewardType.COLOR_RESTORE,
                    name = "Color Mode",
                    description = "Restore full color",
                    requiresPerfectDays = 0,
                    requiresDebtFree = false,
                    isUnlocked = true
                ),
                Reward(
                    type = RewardType.SKIP_TOKEN,
                    name = "Skip Token",
                    description = "Skip one rule once",
                    requiresPerfectDays = 7,
                    requiresDebtFree = true,
                    requiresStreakLength = 7
                )
            )
            
            defaultRewards.forEach { reward ->
                database.rewardDao().insertReward(reward)
            }
        }
    }
}
