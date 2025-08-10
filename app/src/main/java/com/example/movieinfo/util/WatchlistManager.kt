package com.example.movieinfo.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object WatchlistManager {
    private const val PREF_NAME = "watchlist_prefs"
    private const val KEY_WATCHLIST = "movie_ids"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun addToWatchlist(context: Context, movieId: Int) {
        val prefs = getPrefs(context)
        val ids = getWatchlist(context).toMutableSet()
        ids.add(movieId.toString())
        prefs.edit() { putStringSet(KEY_WATCHLIST, ids) }
    }

    fun removeFromWatchlist(context: Context, movieId: Int) {
        val prefs = getPrefs(context)
        val ids = getWatchlist(context).toMutableSet()
        ids.remove(movieId.toString())
        prefs.edit() { putStringSet(KEY_WATCHLIST, ids) }
    }

    fun getWatchlist(context: Context): Set<String> {
        val prefs = getPrefs(context)
        return prefs.getStringSet(KEY_WATCHLIST, emptySet()) ?: emptySet()
    }

    fun isInWatchlist(context: Context, movieId: Int): Boolean {
        return getWatchlist(context).contains(movieId.toString())
    }
}
