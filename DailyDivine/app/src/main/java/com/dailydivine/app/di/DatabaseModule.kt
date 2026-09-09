package com.dailydivine.app.di

import android.content.Context
import androidx.room.Room
import com.dailydivine.app.data.local.dao.*
import com.dailydivine.app.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "dailydivine.db")
            // v1.1: additive migration only — see AppDatabase.MIGRATION_1_2.
            // Deliberately NOT calling fallbackToDestructiveMigration(): that
            // would silently satisfy F002-R13's "don't lose user data" intent
            // right up until the first real schema change, then violate it.
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideVerseDao(db: AppDatabase): VerseDao = db.verseDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideAlarmDao(db: AppDatabase): AlarmDao = db.alarmDao()
    @Provides fun provideStreakDao(db: AppDatabase): StreakDao = db.streakDao()
    @Provides fun provideBookmarkDao(db: AppDatabase): BookmarkDao = db.bookmarkDao()
    @Provides fun provideAlarmToneDao(db: AppDatabase): AlarmToneDao = db.alarmToneDao()
    @Provides fun provideContentVersionDao(db: AppDatabase): ContentVersionDao = db.contentVersionDao()
}
