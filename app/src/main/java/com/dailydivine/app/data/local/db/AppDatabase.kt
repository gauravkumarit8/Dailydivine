package com.dailydivine.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dailydivine.app.data.local.dao.*
import com.dailydivine.app.data.local.entity.*

@Database(
    entities = [
        UserProfile::class,
        Religion::class,
        Category::class,
        Verse::class,
        Alarm::class,
        StreakEntry::class,
        Bookmark::class,
        AlarmTone::class,
        ContentVersion::class // v1.1 (new)
    ],
    version = 2, // bumped from 1 -> 2 for the v1.1 content_versions table
    exportSchema = true
)
// No @TypeConverters(Converters::class) here: Room's KSP processor hard-fails
// if the referenced class has zero @TypeConverter methods, and every entity
// field is currently a Room-native primitive (String/Int/Long/Boolean).
// Converters.kt is kept as an intentionally-empty extension point -- re-add
// this annotation once it actually has a converter method to register.
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun verseDao(): VerseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun alarmDao(): AlarmDao
    abstract fun streakDao(): StreakDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun alarmToneDao(): AlarmToneDao
    abstract fun contentVersionDao(): ContentVersionDao

    companion object {
        /**
         * v1.1 — ADDITIVE migration only. This is the whole point of
         * F002-R13: a schema bump for new content must never fall back to
         * destructive migration (fallbackToDestructiveMigration()), or a
         * content update would wipe every user's streaks and bookmarks.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS content_versions (
                        religionId INTEGER NOT NULL PRIMARY KEY,
                        appliedVersion INTEGER NOT NULL,
                        lastMigratedAt INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}
