package com.example.movieinfo.util

import android.content.Context
import androidx.core.content.edit
import com.example.movieinfo.repository.AuthRepository

object WatchlistManager {

    var hasChanged: Boolean = false

    private fun getPrefs(context: Context, uid: String) =
        context.getSharedPreferences("watchlist_prefs_$uid", Context.MODE_PRIVATE)

    fun addToWatchlist(context: Context, movieId: Int) {
        val uid = AuthRepository().getCurrentUserId() ?: return
        val prefs = getPrefs(context, uid)
        val current = prefs.getStringSet("movie_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(movieId.toString())
        prefs.edit { putStringSet("movie_ids", current) }
        hasChanged = true
    }

    fun removeFromWatchlist(context: Context, movieId: Int) {
        val uid = AuthRepository().getCurrentUserId() ?: return
        val prefs = getPrefs(context, uid)
        val current = prefs.getStringSet("movie_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(movieId.toString())
        prefs.edit { putStringSet("movie_ids", current) }
        hasChanged = true
    }

    fun getWatchlist(context: Context): Set<String> {
        val uid = AuthRepository().getCurrentUserId() ?: return emptySet()
        val prefs = getPrefs(context, uid)
        return prefs.getStringSet("movie_ids", emptySet()) ?: emptySet()
    }

    fun getWatchlistCount(context: Context): Int {
        return getWatchlist(context).size
    }

    fun isInWatchlist(context: Context, movieId: Int): Boolean {
        return getWatchlist(context).contains(movieId.toString())
    }
}
