package com.frovexsoftware.smartup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import java.text.DateFormatSymbols
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.frovexsoftware.smartup.databinding.ActivityMainBinding
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private val defaultThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    private val alarms = mutableListOf<AlarmData>()

    private val editAlarmLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult
        handleEditResult(data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        prefs = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        applySavedTheme(prefs)
        setTheme(R.style.Theme_Smartup)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSettingsPanel()
        binding.btnSettings.setOnClickListener { binding.drawerLayout.openDrawer(Gravity.END) }
        binding.btnAddAlarm.setOnClickListener { openCreateAlarm() }

        loadAlarms()
        if (alarms.isNotEmpty()) {
            AlarmScheduler.rescheduleAll(this, alarms)
            saveAlarms()
        }
        renderAlarms()
        if (alarms.isEmpty()) {
            hideAlarmInfo()
        } else {
            showAlarmInfo()
        }
    }

    private fun scheduleAlarm(item: AlarmData) {
        AlarmScheduler.schedule(this, item, promptExactPermission = true)
    }

    private fun cancelScheduledAlarm(item: AlarmData) {
        AlarmScheduler.cancel(this, item)
    }

    private fun cancelAlarm(item: AlarmData) {
        cancelScheduledAlarm(item)
        alarms.removeAll { it.id == item.id }
        saveAlarms()
        renderAlarms()
    }

    private fun addNewAlarm(
        timeInMillis: Long,
        weekdays: Set<Int>,
        dateMillis: Long?,
        enabled: Boolean,
        description: String,
        challengeTypeName: String
    ) {
        val alarmId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val item = AlarmData(alarmId, timeInMillis, weekdays, dateMillis, enabled, description, challengeTypeName)
        alarms.add(item)
        if (enabled) {
            scheduleAlarm(item)
        }
        saveAlarms()
        renderAlarms()
        showAlarmInfo()
        Toast.makeText(this, getString(R.string.alarm_added), Toast.LENGTH_SHORT).show()
    }

    private fun showAlarmInfo() {
        // Removed for new design
    }

    private fun hideAlarmInfo() {
        // Removed for new design
    }

    private fun formatWeekdays(days: Set<Int>): String {
        if (days.isEmpty()) return ""
        val dfs = DateFormatSymbols(Locale.getDefault())
        val names = mapOf(
            Calendar.SUNDAY to dfs.shortWeekdays[Calendar.SUNDAY],
            Calendar.MONDAY to dfs.shortWeekdays[Calendar.MONDAY],
            Calendar.TUESDAY to dfs.shortWeekdays[Calendar.TUESDAY],
            Calendar.WEDNESDAY to dfs.shortWeekdays[Calendar.WEDNESDAY],
            Calendar.THURSDAY to dfs.shortWeekdays[Calendar.THURSDAY],
            Calendar.FRIDAY to dfs.shortWeekdays[Calendar.FRIDAY],
            Calendar.SATURDAY to dfs.shortWeekdays[Calendar.SATURDAY]
        )
        return days.sorted().joinToString(",") { names[it] ?: "" }
    }

    private fun saveAlarms() {
        AlarmStorage.save(this, alarms)
    }

    private fun loadAlarms() {
        alarms.clear()
        alarms.addAll(AlarmStorage.load(this))
    }

    private fun renderAlarms() {
        binding.alarmsContainer.removeAllViews()
        // binding.tvEmptyState.visibility = View.GONE // Removing as I am not sure if it exists in layout binding

        val inflater = layoutInflater
        alarms.sortedBy { it.timeInMillis }.forEach { alarm ->
            val view = inflater.inflate(R.layout.item_alarm, binding.alarmsContainer, false)
            val tvTime = view.findViewById<android.widget.TextView>(R.id.tvAlarmTime)
            val tvChallengeName = view.findViewById<android.widget.TextView>(R.id.tvChallengeName)
            val ivChallengeIcon = view.findViewById<android.widget.ImageView>(R.id.ivChallengeIcon)
            val tvMeta = view.findViewById<android.widget.TextView>(R.id.tvAlarmMeta)
            val switchEnabled = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchAlarmEnabled)
            val daysLayout = view.findViewById<android.widget.LinearLayout>(R.id.daysLayout)

            val cal = Calendar.getInstance().apply { timeInMillis = alarm.timeInMillis }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            tvTime.text = formatTime(hour, minute)

            val challengeType = try {
                 ChallengeType.valueOf(alarm.challengeType)
            } catch (e: Exception) { ChallengeType.NONE }
            
            val (iconRes, nameRes) = when(challengeType) {
                ChallengeType.SNAKE -> R.drawable.ic_challenge_snake to R.string.chip_snake
                ChallengeType.DOTS -> R.drawable.ic_challenge_dots to R.string.chip_dots
                ChallengeType.MATH -> R.drawable.ic_challenge_math to R.string.chip_math
                ChallengeType.COLOR -> R.drawable.ic_challenge_color to R.string.chip_color
                else -> R.drawable.ic_leaf to R.string.edit_morning_me
            }
            ivChallengeIcon.setImageResource(iconRes)
            tvChallengeName.text = getString(nameRes)

            if (alarm.description.isNotBlank()) {
                tvMeta.text = alarm.description
                tvMeta.visibility = View.VISIBLE
            } else {
                tvMeta.visibility = View.GONE
            }

            switchEnabled.isChecked = alarm.enabled
            // switchEnabled.text = if (alarm.enabled) getString(R.string.alarm_switch_on) else getString(R.string.alarm_switch_off) // Removing text from switch to match mockup
            switchEnabled.text = "" 
            switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                updateAlarmEnabled(alarm, isChecked)
            }

            daysLayout.removeAllViews()
            val weekDayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            val density = resources.displayMetrics.density
            val sizePx = (32 * density).toInt()
            val marginPx = (4 * density).toInt()
            
            for (i in 0..6) {
                val dayView = android.widget.TextView(this)
                val isActive = alarm.weekdays.contains(i)
                dayView.text = weekDayNames[i]
                dayView.textSize = 12f
                dayView.gravity = android.view.Gravity.CENTER
                dayView.includeFontPadding = false
                
                val params = android.widget.LinearLayout.LayoutParams(sizePx, sizePx) 
                params.setMargins(0, 0, marginPx, 0)
                dayView.layoutParams = params
                
                 if (isActive) {
                    dayView.setBackgroundResource(R.drawable.bg_day_active)
                    // We need to resolve the color if possible, or just parse it.
                    // Assuming bg_day_active uses the purple color.
                    dayView.setTextColor(android.graphics.Color.WHITE)
                } else {
                    dayView.setBackgroundResource(R.drawable.bg_day_inactive) // Need to ensure this drawable exists or use color
                     dayView.setTextColor(android.graphics.Color.parseColor("#8E7C9C"))
                }
                daysLayout.addView(dayView)
            }
            
            view.setOnClickListener { openEditAlarm(alarm) }
            binding.alarmsContainer.addView(view)
        }
    }

    private fun openCreateAlarm() {
        val intent = Intent(this, EditAlarmActivity::class.java).apply {
            putExtra("alarm_id", -1)
            putExtra("time", System.currentTimeMillis())
            putExtra("date", -1L)
            putExtra("enabled", true)
            putExtra("description", "")
            putExtra("is24h", prefs.getBoolean("is24h", true))
            putIntegerArrayListExtra("weekdays", ArrayList())
            putExtra("challenge_type", ChallengeType.NONE.name)
        }
        editAlarmLauncher.launch(intent)
    }

    private fun openEditAlarm(alarm: AlarmData) {
        val intent = Intent(this, EditAlarmActivity::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("time", alarm.timeInMillis)
            putExtra("date", alarm.dateMillis ?: -1L)
            putExtra("enabled", alarm.enabled)
            putExtra("description", alarm.description)
            putExtra("is24h", prefs.getBoolean("is24h", true))
            putIntegerArrayListExtra("weekdays", ArrayList(alarm.weekdays))
            putExtra("challenge_type", alarm.challengeType)
        }
        editAlarmLauncher.launch(intent)
    }

    private fun handleEditResult(data: Intent) {
        val id = data.getIntExtra("alarm_id", -1)
        val newTime = data.getLongExtra("time", System.currentTimeMillis())
        val newDate = data.getLongExtra("date", -1L).let { if (it == -1L) null else it }
        val newEnabled = data.getBooleanExtra("enabled", true)
        val newDescription = data.getStringExtra("description") ?: ""
        val newChallengeType = data.getStringExtra("challenge_type") ?: ChallengeType.NONE.name
        val weekdaysList = data.getIntegerArrayListExtra("weekdays") ?: arrayListOf<Int>()
        val newWeekdays = weekdaysList.toSet()

        if (id == -1) {
            addNewAlarm(newTime, newWeekdays, newDate, newEnabled, newDescription, newChallengeType)
            return
        }

        val target = alarms.find { it.id == id } ?: return

        if (data.getBooleanExtra("delete", false)) {
            cancelScheduledAlarm(target)
            alarms.removeAll { it.id == id }
            saveAlarms()
            renderAlarms()
            if (alarms.isEmpty()) hideAlarmInfo() else showAlarmInfo()
            Toast.makeText(this, getString(R.string.alarm_deleted), Toast.LENGTH_SHORT).show()
            return
        }

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

    private fun updateAlarmEnabled(item: AlarmData, enabled: Boolean) {
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

    private fun applySavedTheme(prefs: SharedPreferences) {
        val mode = prefs.getInt("theme_mode", defaultThemeMode)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupSettingsPanel() {
        val langByButton: Map<Int, String?> = mapOf(
            binding.btnLangSystem.id to null,
            binding.btnLangRu.id to "ru",
            binding.btnLangEn.id to "en"
        )

        val savedLang = LocaleHelper.getSavedLanguage(this)
        val initialButtonId = if (savedLang == null) {
            binding.btnLangSystem.id
        } else {
            langByButton.entries.find { it.value == savedLang }?.key ?: binding.btnLangSystem.id
        }
        binding.mbtgLang.check(initialButtonId)

        binding.mbtgLang.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val selectedLang = langByButton[checkedId]
            val currentSaved = LocaleHelper.getSavedLanguage(this)
            if (selectedLang == currentSaved) return@addOnButtonCheckedListener

            if (selectedLang == null) {
                LocaleHelper.clearLanguage(this)
            } else {
                LocaleHelper.saveLanguage(this, selectedLang)
            }
            recreate()
        }

        val mode = prefs.getInt("theme_mode", defaultThemeMode)
        when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.rbThemeLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val newMode = when (checkedId) {
                binding.rbThemeLight.id -> AppCompatDelegate.MODE_NIGHT_NO
                binding.rbThemeDark.id -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            prefs.edit().putInt("theme_mode", newMode).apply()
            AppCompatDelegate.setDefaultNightMode(newMode)
        }

        val is24h = prefs.getBoolean("is24h", true)
        binding.switch24h.isChecked = is24h
        binding.switch24h.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("is24h", checked).apply()
            Toast.makeText(this, if (checked) getString(R.string.time_format_24h_toast) else getString(R.string.time_format_12h_toast), Toast.LENGTH_SHORT).show()
        }

        val useSpinner = prefs.getBoolean("time_picker_spinner", false)
        binding.switchTimePickerStyle.isChecked = useSpinner
        binding.switchTimePickerStyle.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("time_picker_spinner", checked).apply()
            Toast.makeText(this, if (checked) getString(R.string.time_picker_style_spinner) else getString(R.string.time_picker_style_clock), Toast.LENGTH_SHORT).show()
        }

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START)
    }

    private fun formatTime(hour24: Int, minute: Int): String {
        val is24 = prefs.getBoolean("is24h", true)
        return if (is24) {
            "%02d:%02d".format(hour24, minute)
        } else {
            val hour12 = ((hour24 + 11) % 12) + 1
            val suffix = if (hour24 >= 12) "PM" else "AM"
            "%d:%02d %s".format(hour12, minute, suffix)
        }
    }
}