package com.shieldher.vault.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EvidenceEntity::class], version = 1, exportSchema = true)
abstract class EvidenceDatabase : RoomDatabase() {
    abstract fun evidenceDao(): EvidenceDao

    companion object {
        @Volatile
        private var INSTANCE: EvidenceDatabase? = null

        fun getInstance(context: Context): EvidenceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EvidenceDatabase::class.java,
                    "shieldher_evidence.db"
                ).build().also { INSTANCE = it }
            }
        }

        /**
         * Replaces the singleton with [db] for the duration of a test.
         * Call [clearInstanceForTesting] in @After to restore the default.
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun setInstanceForTesting(db: EvidenceDatabase) {
            synchronized(this) { INSTANCE = db }
        }

        /**
         * Clears the singleton so the next call to [getInstance] builds a
         * fresh on-disk database. Must be called after every test that used
         * [setInstanceForTesting].
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun clearInstanceForTesting() {
            synchronized(this) { INSTANCE = null }
        }
    }
}

