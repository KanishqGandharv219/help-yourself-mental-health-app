package com.helpyourself.com.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.helpyourself.com.data.local.dao.ChatSessionDao
import com.helpyourself.com.data.local.dao.ChatHistoryDao
import com.helpyourself.com.data.local.dao.MessageDao
import com.helpyourself.com.data.local.entities.ChatSessionEntity
import com.helpyourself.com.data.local.entities.MessageEntity
import com.helpyourself.com.data.local.entities.ChatHistoryEntity
import com.helpyourself.com.data.local.dao.DepressionResultDao
import com.helpyourself.com.data.local.dao.AnxietyResultDao
import com.helpyourself.com.data.local.dao.StressResultDao
import com.helpyourself.com.data.local.entities.DepressionResultEntity
import com.helpyourself.com.data.local.entities.AnxietyResultEntity
import com.helpyourself.com.data.local.entities.StressResultEntity
import com.helpyourself.com.data.local.dao.DepressionAnswerDao
import com.helpyourself.com.data.local.entities.DepressionAnswerEntity
import com.helpyourself.com.data.local.dao.AnxietyAnswerDao
import com.helpyourself.com.data.local.entities.AnxietyAnswerEntity
import com.helpyourself.com.data.local.entities.StressAnswerEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        MessageEntity::class,
        ChatHistoryEntity::class,
        DepressionResultEntity::class,
        AnxietyResultEntity::class,
        StressResultEntity::class,
        DepressionAnswerEntity::class,
        AnxietyAnswerEntity::class,
        StressAnswerEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun messageDao(): MessageDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    // New DAOs
    abstract fun depressionResultDao(): DepressionResultDao
    abstract fun anxietyResultDao(): AnxietyResultDao
    abstract fun stressResultDao(): StressResultDao
    abstract fun depressionAnswerDao(): DepressionAnswerDao
    abstract fun anxietyAnswerDao(): AnxietyAnswerDao
    abstract fun stressAnswerDao(): com.helpyourself.com.data.local.dao.StressAnswerDao

    companion object {
        private const val TAG = "AppDatabase"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            Log.d(TAG, "Getting database instance")
            
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Creating new database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mental_health_assistant_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                Log.d(TAG, "Database instance created successfully")
                INSTANCE = instance
                instance
            }
        }
    }
}  
