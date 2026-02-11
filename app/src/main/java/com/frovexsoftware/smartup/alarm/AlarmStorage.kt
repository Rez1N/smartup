package com.frovexsoftware.smartup.alarm

import android.content.Context
import org.json.JSONArray

object AlarmStorage {
    private const val PREFS_NAME = "alarm_prefs"
    private const val KEY = "alarms_json"

    fun load(context: Context): MutableList<AlarmData> {
        val serialized = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return mutableListOf()
        return runCatching {
            val arr = JSONArray(serialized)
            (0 until arr.length()).map { AlarmData.fromJson(arr.getJSONObject(it)) }.toMutableList()
        }.getOrElse { mutableListOf() }
    }

    fun save(context: Context, alarms: List<AlarmData>) {
        val json = JSONArray()
        alarms.forEach { json.put(it.toJson()) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, json.toString())
            .apply()
    }
}
