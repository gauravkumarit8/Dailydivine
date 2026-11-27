package com.dailydivine.app.data.repository

import android.content.Context
import com.dailydivine.app.data.content.GitaBook
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Loads the Gita reader file from the app's assets once and keeps it in memory (about 330 KB of text). */
@Singleton
class GitaReaderRepository @Inject constructor(@ApplicationContext private val context: Context) {
    @Volatile private var cached: GitaBook? = null

    /** An empty book (no chapters) if the file is missing or damaged, so the screen can say so instead of crashing. */
    suspend fun book(): GitaBook = cached ?: withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(ASSET).bufferedReader(Charsets.UTF_8).use { Gson().fromJson(it, GitaBook::class.java) }
        }.getOrNull()?.also { cached = it } ?: GitaBook()
    }

    companion object {
        const val ASSET = "gita/gita_reader.json"
    }
}
