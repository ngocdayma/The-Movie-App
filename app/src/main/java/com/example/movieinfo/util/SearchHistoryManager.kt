package com.example.movieinfo.util

import android.content.Context

object SearchHistoryManager {
    private const val PREFS_NAME = "search_history"
    private const val KEY_HISTORY = "history"

    fun saveSearch(context: Context, query: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val history = getSearchHistory(context).toMutableList()

        // Xóa nếu trùng để đưa lên đầu
        history.remove(query)
        history.add(0, query)

        // Giới hạn tối đa 10 lịch sử
        if (history.size > 10) {
            history.removeAt(history.size - 1)
        }

        prefs.edit().putStringSet(KEY_HISTORY, history.toSet()).apply()
    }

    fun getSearchHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_HISTORY, emptySet())?.toList() ?: emptyList()
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
