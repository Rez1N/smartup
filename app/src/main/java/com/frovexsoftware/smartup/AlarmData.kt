package com.frovexsoftware.smartup

import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializable alarm model shared between UI and background receivers.
 */
data class AlarmData(
    val id: Int,
    var timeInMillis: Long,
    var weekdays: Set<Int>,
    var dateMillis: Long?,
    var enabled: Boolean,
    var description: String = "",
    var challengeType: String = ChallengeType.NONE.name
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("time", timeInMillis)
        if (dateMillis == null) put("date", JSONObject.NULL) else put("date", dateMillis)
        put("weekdays", JSONArray(weekdays.toList()))
        put("enabled", enabled)
        put("description", description)
        put("challenge_type", challengeType)
    }

    companion object {
        fun fromJson(obj: JSONObject): AlarmData {
            val id = obj.getInt("id")
            val time = obj.getLong("time")
            val dateMillis = if (obj.isNull("date")) null else obj.getLong("date")
            val enabled = obj.optBoolean("enabled", true)
            val description = obj.optString("description", "")
            val challengeType = obj.optString("challenge_type", ChallengeType.NONE.name)
            val weekdaysJson = obj.optJSONArray("weekdays") ?: JSONArray()
            val weekdaysSet = mutableSetOf<Int>()
            for (j in 0 until weekdaysJson.length()) {
                weekdaysSet.add(weekdaysJson.getInt(j))
            }
            return AlarmData(id, time, weekdaysSet, dateMillis, enabled, description, challengeType)
        }
    }
}
