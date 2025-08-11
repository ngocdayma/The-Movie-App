package com.example.movieinfo.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object WatchlistManager {
    private const val PREF_NAME = "watchlist_prefs"
    private const val KEY_WATCHLIST = "movie_ids"

    var hasChanged: Boolean = false

    fun addToWatchlist(context: Context, movieId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getWatchlist(context).toMutableSet()
        current.add(movieId.toString())
        prefs.edit() { putStringSet(KEY_WATCHLIST, current) }
        hasChanged = true
    }

    fun removeFromWatchlist(context: Context, movieId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getWatchlist(context).toMutableSet()
        current.remove(movieId.toString())
        prefs.edit() { putStringSet(KEY_WATCHLIST, current) }
        hasChanged = true
    }

    fun getWatchlist(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_WATCHLIST, emptySet()) ?: emptySet()
    }

    fun isInWatchlist(context: Context, movieId: Int): Boolean {
        return getWatchlist(context).contains(movieId.toString())
    }
}
