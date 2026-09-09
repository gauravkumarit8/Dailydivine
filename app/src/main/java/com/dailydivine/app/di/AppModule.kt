package com.dailydivine.app.di

import android.content.Context
import com.dailydivine.app.data.content.ContentLoader
import com.dailydivine.app.data.content.ContentMigrationManager
import com.dailydivine.app.data.local.dao.CategoryDao
import com.dailydivine.app.data.local.dao.ContentVersionDao
import com.dailydivine.app.data.local.dao.VerseDao
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideContentLoader(@ApplicationContext context: Context): ContentLoader =
        ContentLoader(context)

    @Provides
    @Singleton
    fun provideContentMigrationManager(
        contentLoader: ContentLoader,
        verseDao: VerseDao,
        categoryDao: CategoryDao,
        contentVersionDao: ContentVersionDao,
        gson: Gson
    ): ContentMigrationManager = ContentMigrationManager(
        contentLoader, verseDao, categoryDao, contentVersionDao, gson
    )
}
