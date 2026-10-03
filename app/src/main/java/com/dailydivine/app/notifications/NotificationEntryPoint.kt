package com.dailydivine.app.notifications

import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.StreakRepository
import com.dailydivine.app.data.repository.VerseRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Lets the notification workers reach the app's Hilt singletons without
 * @HiltWorker, which would require replacing WorkManager's default
 * initializer in the manifest (a launch-time change we deliberately avoid).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun userPreferences(): UserPreferences
    fun verseRepository(): VerseRepository
    fun streakRepository(): StreakRepository
}
