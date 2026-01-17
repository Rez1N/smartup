package com.frovexsoftware.smartup

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.frovexsoftware.smartup.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedDateMillis: Long? = null
    private val alarms = mutableListOf<AlarmItem>()
    private var selectedChallenge: ChallengeType = ChallengeType.NONE

    private val editAlarmLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult
        handleEditResult(data)
    }

    private val weekdayChips by lazy {
        mapOf(
            binding.chipMon to Calendar.MONDAY,
            binding.chipTue to Calendar.TUESDAY,
            binding.chipWed to Calendar.WEDNESDAY,
            binding.chipThu to Calendar.THURSDAY,
            binding.chipFri to Calendar.FRIDAY,
            binding.chipSat to Calendar.SATURDAY,
            binding.chipSun to Calendar.SUNDAY
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        applySavedTheme(prefs)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val is24h = prefs.getBoolean("is24h", true)
        binding.timePicker.setIs24HourView(is24h)
        binding.switch24h.isChecked = is24h

        binding.switch24h.setOnCheckedChangeListener { _, checked ->
            binding.timePicker.setIs24HourView(checked)
            prefs.edit().putBoolean("is24h", checked).apply()
        }

        binding.btnSettings.setOnClickListener {
            showThemeDialog(prefs)
        }

        binding.btnChooseChallenge.setOnClickListener {
            showChallengeChooser()
        }

        binding.btnSelectDate.setOnClickListener { openDatePicker() }
        binding.btnClearDate.setOnClickListener { clearDateSelection() }

        binding.btnSetAlarm.setOnClickListener {
            val triggerTime = TimeLogic.calculateNextTrigger(
                binding.timePicker.hour,
                binding.timePicker.minute,
                getSelectedWeekdays(),
                selectedDateMillis
            )

            val weekdays = getSelectedWeekdays()
            addAlarm(triggerTime.timeInMillis, weekdays, selectedDateMillis, selectedChallenge.name)
            showAlarmInfo()
        }

        binding.btnInlineCancel.setOnClickListener {
            cancelAllAlarms()
        }

        loadAlarms()
        renderAlarms()
        if (alarms.isEmpty()) {
            hideAlarmInfo()
        } else {
            showAlarmInfo()
        }
    }

    private fun addAlarm(
        timeInMillis: Long,
        weekdays: Set<Int>,
        dateMillis: Long?,
        challengeTypeName: String
    ) {
        val alarmId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val item = AlarmItem(alarmId, timeInMillis, weekdays, dateMillis, enabled = true, description = "", challengeType = challengeTypeName)
        alarms.add(item)
        scheduleAlarm(item)
        saveAlarms()
        renderAlarms()
        Toast.makeText(this, "Будильник добавлен", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleAlarm(item: AlarmItem) {
        cancelScheduledAlarm(item)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Требуется разрешение")
                    .setMessage("Для установки точного будильника требуется разрешение 'Schedule exact alarms'. Открыть настройки, чтобы предоставить его?")
                    .setPositiveButton("Открыть настройки") { _, _ ->
                        try {
                            val intent = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                .let { Intent(it) }
                            startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.parse("package:$packageName")
                            }
                            startActivity(intent)
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
                return
            }
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", item.id)
            putExtra("time", item.timeInMillis)
            putExtra("challenge_type", item.challengeType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            item.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val clockInfo = AlarmManager.AlarmClockInfo(item.timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(clockInfo, pendingIntent)
    }

    private fun cancelScheduledAlarm(item: AlarmItem) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val existing = PendingIntent.getBroadcast(
            this,
            item.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        existing?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun cancelAllAlarms() {
        if (alarms.isEmpty()) {
            hideAlarmInfo()
            Toast.makeText(this, "Будильников нет", Toast.LENGTH_SHORT).show()
            return
        }

        alarms.toList().forEach { cancelScheduledAlarm(it) }
        alarms.clear()
        saveAlarms()
        renderAlarms()
        hideAlarmInfo()
        Toast.makeText(this, "Все будильники отменены", Toast.LENGTH_SHORT).show()
    }

    private fun cancelAlarm(item: AlarmItem) {
        cancelScheduledAlarm(item)
        alarms.removeAll { it.id == item.id }
        saveAlarms()
        renderAlarms()
    }

    private fun showAlarmInfo() {
        if (alarms.isEmpty()) {
            hideAlarmInfo()
            return
        }
        val activeAlarms = alarms.filter { it.enabled }
        if (activeAlarms.isEmpty()) {
            hideAlarmInfo()
            return
        }
        val last = activeAlarms.last()
        val cal = Calendar.getInstance().apply { timeInMillis = last.timeInMillis }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeStr = "%02d:%02d".format(hour, minute)
        val dateStr = last.dateMillis?.let {
            val c = Calendar.getInstance().apply { timeInMillis = it }
            "${c.get(Calendar.DAY_OF_MONTH)}.${c.get(Calendar.MONTH) + 1}.${c.get(Calendar.YEAR)}"
        }
        val weekdaysStr = formatWeekdays(last.weekdays)
        val extra = when {
            dateStr != null -> " (дата: $dateStr)"
            weekdaysStr.isNotEmpty() -> " ($weekdaysStr)"
            else -> ""
        }
        val activeCount = activeAlarms.size
        binding.tvSelectedTime.text = "Активных будильников: $activeCount. Последний: $timeStr$extra"
        binding.tvAlarmInfo.text = "Активных будильников: $activeCount"
        binding.alarmInfoContainer.visibility = android.view.View.VISIBLE
    }

    private fun hideAlarmInfo() {
        binding.tvSelectedTime.text = "Будильник не установлен"
        binding.alarmInfoContainer.visibility = android.view.View.INVISIBLE
    }

    private fun openDatePicker() {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH)
        val day = now.get(Calendar.DAY_OF_MONTH)

        val dialog = android.app.DatePickerDialog(this, { _: DatePicker, y: Int, m: Int, d: Int ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDateMillis = cal.timeInMillis
            updateDateButtonText()
        }, year, month, day)
        dialog.datePicker.minDate = now.timeInMillis
        dialog.show()
    }

    private fun updateDateButtonText() {
        binding.btnSelectDate.text = selectedDateMillis?.let {
            val cal = Calendar.getInstance().apply { timeInMillis = it }
            "Дата: ${cal.get(Calendar.DAY_OF_MONTH)}.${cal.get(Calendar.MONTH) + 1}.${cal.get(Calendar.YEAR)}"
        } ?: "Выбрать дату"
    }

    private fun showChallengeChooser() {
        val challengeMap = linkedMapOf(
            "Без челленджа" to ChallengeType.NONE,
            "Ввести дату" to ChallengeType.DATE,
            "Змейка" to ChallengeType.SNAKE,
            "Точки 4x4" to ChallengeType.DOTS,
            "Математический пример" to ChallengeType.MATH,
            "Угадать цвет" to ChallengeType.COLOR
        )
        val options = challengeMap.keys.toTypedArray()
        val currentIndex = challengeMap.values.indexOf(selectedChallenge)

        MaterialAlertDialogBuilder(this)
            .setTitle("Челлендж")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                selectedChallenge = challengeMap.values.elementAt(which)
                binding.tvChallengeValue.text = options[which]
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearDateSelection() {
        selectedDateMillis = null
        updateDateButtonText()
    }

    private fun getSelectedWeekdays(): Set<Int> {
        return weekdayChips.filter { (chip, _) -> chip.isChecked }.values.toSet()
    }

    private fun clearWeekdays() {
        weekdayChips.keys.forEach { it.isChecked = false }
    }

    private fun formatWeekdays(days: Set<Int>): String {
        if (days.isEmpty()) return ""
        val names = mapOf(
            Calendar.MONDAY to "Пн",
            Calendar.TUESDAY to "Вт",
            Calendar.WEDNESDAY to "Ср",
            Calendar.THURSDAY to "Чт",
            Calendar.FRIDAY to "Пт",
            Calendar.SATURDAY to "Сб",
            Calendar.SUNDAY to "Вс"
        )
        return days.sorted().joinToString(",") { names[it] ?: "" }
    }

    private fun saveAlarms() {
        val prefs = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val json = JSONArray()
        alarms.forEach { alarm ->
            val obj = JSONObject()
            obj.put("id", alarm.id)
            obj.put("time", alarm.timeInMillis)
            obj.put("date", alarm.dateMillis ?: JSONObject.NULL)
            obj.put("weekdays", JSONArray(alarm.weekdays.toList()))
            obj.put("enabled", alarm.enabled)
            obj.put("description", alarm.description)
            obj.put("challenge_type", alarm.challengeType)
            json.put(obj)
        }
        prefs.edit().putString("alarms_json", json.toString()).apply()
    }

    private fun loadAlarms() {
        val prefs = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val serialized = prefs.getString("alarms_json", null) ?: return
        runCatching {
            val arr = JSONArray(serialized)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
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
                alarms.add(AlarmItem(id, time, weekdaysSet, dateMillis, enabled, description, challengeType))
            }
        }.onFailure { alarms.clear() }
    }

    private fun renderAlarms() {
        binding.alarmsContainer.removeAllViews()
        if (alarms.isEmpty()) return

        val inflater = layoutInflater
        alarms.sortedBy { it.timeInMillis }.forEach { alarm ->
            val view = inflater.inflate(R.layout.item_alarm, binding.alarmsContainer, false)
            val info = view.findViewById<android.widget.TextView>(R.id.tvAlarmInfo)
            val switchEnabled = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchAlarmEnabled)

            val cal = Calendar.getInstance().apply { timeInMillis = alarm.timeInMillis }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val timeStr = "%02d:%02d".format(hour, minute)
            val dateStr = alarm.dateMillis?.let {
                val c = Calendar.getInstance().apply { timeInMillis = it }
                "${c.get(Calendar.DAY_OF_MONTH)}.${c.get(Calendar.MONTH) + 1}.${c.get(Calendar.YEAR)}"
            }
            val weekdaysStr = formatWeekdays(alarm.weekdays)
            val extra = when {
                dateStr != null -> " (дата: $dateStr)"
                weekdaysStr.isNotEmpty() -> " ($weekdaysStr)"
                else -> ""
            }
            val descSuffix = if (alarm.description.isNotBlank()) " — ${alarm.description}" else ""
            info.text = "$timeStr$extra$descSuffix"

            switchEnabled.isChecked = alarm.enabled
            switchEnabled.text = if (alarm.enabled) "Вкл" else "Выкл"
            switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                switchEnabled.text = if (isChecked) "Вкл" else "Выкл"
                updateAlarmEnabled(alarm, isChecked)
            }

            view.setOnClickListener {
                openEditAlarm(alarm)
            }

            binding.alarmsContainer.addView(view)
        }
    }

    data class AlarmItem(
        val id: Int,
        var timeInMillis: Long,
        var weekdays: Set<Int>,
        var dateMillis: Long?,
        var enabled: Boolean,
        var description: String = "",
        var challengeType: String = ChallengeType.NONE.name
    )

    private fun updateAlarmEnabled(item: AlarmItem, enabled: Boolean) {
        item.enabled = enabled
        if (enabled) {
            scheduleAlarm(item)
        } else {
            cancelScheduledAlarm(item)
        }
        saveAlarms()
        renderAlarms()
        showAlarmInfo()
    }

    private fun openEditAlarm(alarm: AlarmItem) {
        val intent = Intent(this, EditAlarmActivity::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("time", alarm.timeInMillis)
            putExtra("date", alarm.dateMillis ?: -1L)
            putExtra("enabled", alarm.enabled)
            putExtra("description", alarm.description)
            putExtra("is24h", binding.switch24h.isChecked)
            putIntegerArrayListExtra("weekdays", ArrayList(alarm.weekdays))
            putExtra("challenge_type", alarm.challengeType)
        }
        editAlarmLauncher.launch(intent)
    }

    private fun handleEditResult(data: Intent) {
        val id = data.getIntExtra("alarm_id", -1)
        if (id == -1) return
        val target = alarms.find { it.id == id } ?: return

        if (data.getBooleanExtra("delete", false)) {
            cancelScheduledAlarm(target)
            alarms.removeAll { it.id == id }
            saveAlarms()
            renderAlarms()
            if (alarms.isEmpty()) hideAlarmInfo() else showAlarmInfo()
            Toast.makeText(this, "Будильник удалён", Toast.LENGTH_SHORT).show()
            return
        }

        val newTime = data.getLongExtra("time", target.timeInMillis)
        val newDate = data.getLongExtra("date", -1L).let { if (it == -1L) null else it }
        val newEnabled = data.getBooleanExtra("enabled", target.enabled)
        val newDescription = data.getStringExtra("description") ?: ""
        val newChallengeType = data.getStringExtra("challenge_type") ?: ChallengeType.NONE.name
        val weekdaysList = data.getIntegerArrayListExtra("weekdays") ?: arrayListOf<Int>()
        val newWeekdays = weekdaysList.toSet()

        cancelScheduledAlarm(target)

        target.timeInMillis = newTime
        target.dateMillis = newDate
        target.enabled = newEnabled
        target.description = newDescription
        target.weekdays = newWeekdays
        target.challengeType = newChallengeType

        if (target.enabled) {
            scheduleAlarm(target)
        }

        saveAlarms()
        renderAlarms()
        showAlarmInfo()
    }

    private fun applySavedTheme(prefs: android.content.SharedPreferences) {
        val mode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showThemeDialog(prefs: android.content.SharedPreferences) {
        val modes = listOf(
            "Как в системе" to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            "Светлая" to AppCompatDelegate.MODE_NIGHT_NO,
            "Тёмная" to AppCompatDelegate.MODE_NIGHT_YES
        )
        val current = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val checked = modes.indexOfFirst { it.second == current }.coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle("Тема")
            .setSingleChoiceItems(modes.map { it.first }.toTypedArray(), checked) { dialog, which ->
                val mode = modes[which].second
                prefs.edit().putInt("theme_mode", mode).apply()
                AppCompatDelegate.setDefaultNightMode(mode)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}