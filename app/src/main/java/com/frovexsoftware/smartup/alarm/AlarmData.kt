package com.frovexsoftware.smartup.alarm

import com.frovexsoftware.smartup.challenge.ChallengeType
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
            val weekdaysJson = obj.optJSONArray("weekdays") ?: JSONArray()
            val weekdays = (0 until weekdaysJson.length()).map { weekdaysJson.getInt(it) }.toSet()
            return AlarmData(
                id = obj.getInt("id"),
                timeInMillis = obj.getLong("time"),
                weekdays = weekdays,
                dateMillis = if (obj.isNull("date")) null else obj.getLong("date"),
                enabled = obj.optBoolean("enabled", true),
                description = obj.optString("description", ""),
                challengeType = obj.optString("challenge_type", ChallengeType.NONE.name)
            )
        }
    }
}
