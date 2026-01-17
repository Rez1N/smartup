package com.frovexsoftware.smartup

import android.content.Context
import org.json.JSONArray

object AlarmStorage {
    private const val PREFS_NAME = "alarm_prefs"
    private const val KEY = "alarms_json"

    fun load(context: Context): MutableList<AlarmData> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serialized = prefs.getString(KEY, null) ?: return mutableListOf()
        return runCatching {
            val arr = JSONArray(serialized)
            val list = mutableListOf<AlarmData>()
            for (i in 0 until arr.length()) {
                list.add(AlarmData.fromJson(arr.getJSONObject(i)))
            }
            list
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
