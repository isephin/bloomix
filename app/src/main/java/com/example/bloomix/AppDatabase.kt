package com.example.bloomix

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Defines the database configuration
// entities: List of tables (currently only JournalEntry)
// version: Update this number if you change the table structure in the future
@Database(entities = [JournalEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Provides access to the DAO (Data Access Object)
    abstract fun journalDao(): JournalDao

    // Singleton Pattern Implementation
    companion object {
        // @Volatile ensures that changes to this property are immediately visible to other threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If instance exists, return it. If not, enter synchronized block.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bloomix_database" // The name of the actual file on the phone
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}