package com.frovexsoftware.smartup.ui

import android.app.Activity
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.frovexsoftware.smartup.R
import com.frovexsoftware.smartup.alarm.AlarmData
import com.frovexsoftware.smartup.alarm.AlarmScheduler
import com.frovexsoftware.smartup.alarm.AlarmStorage
import com.frovexsoftware.smartup.challenge.ChallengeType
import com.frovexsoftware.smartup.databinding.ActivityMainBinding
import com.frovexsoftware.smartup.util.LocaleHelper
import com.frovexsoftware.smartup.util.TimeFormatter
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private val defaultThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    private val alarms = mutableListOf<AlarmData>()
    private val notificationPermissionRequestCode = 1001

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

        updateLocalizedTexts()
        ensureNotificationPermission()

        setupSettingsPanel()
        binding.btnSettings.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.END) }
        binding.btnAiAgent.setOnClickListener { openAiAgent() }
        binding.btnAddAlarm.setOnClickListener { openCreateAlarm() }

        loadAlarms()
        if (alarms.isNotEmpty()) {
            AlarmScheduler.rescheduleAll(this, alarms)
            saveAlarms()
        }
        renderAlarms()
    }

    override fun onResume() {
        super.onResume()
        updateLocalizedTexts()
    }

    private fun updateLocalizedTexts() {
        binding.tvTitle.text = getString(R.string.alarms_title)
        binding.tvSubtitle.text = getString(R.string.alarms_subtitle)
        binding.tvEmptyState.text = getString(R.string.alarms_empty)
        // Settings button content description
        binding.btnSettings.contentDescription = getString(R.string.settings_title)
        binding.btnAiAgent.contentDescription = getString(R.string.ai_agent_open)

        val currentLocale = resources.configuration.locales[0].language
        binding.btnAddAlarm.text = if (currentLocale == "ru") {
            getString(R.string.edit_create_button)
        } else {
            getString(R.string.alarms_add)
        }
    }

    private fun openAiAgent() {
        startActivity(Intent(this, AiAgentActivity::class.java))
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), notificationPermissionRequestCode)
            }
        }
    }

    private fun scheduleAlarm(item: AlarmData) {
        AlarmScheduler.schedule(this, item, promptExactPermission = true)
    }

    private fun cancelScheduledAlarm(item: AlarmData) {
        AlarmScheduler.cancel(this, item)
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
        Toast.makeText(this, getString(R.string.alarm_added), Toast.LENGTH_SHORT).show()
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
        val dfs = java.text.DateFormatSymbols(resources.configuration.locales[0])
        val weekDayCalendar = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )
        val weekDayNames = weekDayCalendar.map { dfs.shortWeekdays[it] }
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

            val challengeType = ChallengeType.from(alarm.challengeType)

            ivChallengeIcon.setImageResource(challengeType.iconRes)
            if (challengeType == ChallengeType.NONE) {
                tvChallengeName.visibility = View.GONE
            } else {
                tvChallengeName.visibility = View.VISIBLE
                tvChallengeName.text = getString(challengeType.labelRes)
            }

            val metaText = alarm.description.trim()
            val hiddenMeta = setOf(
                getString(R.string.edit_morning_me),
                getString(R.string.challenge_none),
                getString(R.string.alarm_meta_none)
            )
            if (metaText.isNotBlank() && metaText !in hiddenMeta) {
                tvMeta.text = metaText
                tvMeta.visibility = View.VISIBLE
            } else {
                tvMeta.visibility = View.GONE
            }

            switchEnabled.isChecked = alarm.enabled
            switchEnabled.text = ""
            switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                updateAlarmEnabled(alarm, isChecked)
            }

            daysLayout.removeAllViews()
            val density = resources.displayMetrics.density
            val sizePx = (32 * density).toInt()
            val marginPx = (4 * density).toInt()
            for (i in 0..6) {
                val dayView = android.widget.TextView(this)
                val calendarDay = weekDayCalendar[i]
                val isActive = alarm.weekdays.contains(calendarDay)
                dayView.text = weekDayNames[i]
                dayView.textSize = 12f
                dayView.gravity = android.view.Gravity.CENTER
                dayView.includeFontPadding = false
                val params = android.widget.LinearLayout.LayoutParams(sizePx, sizePx)
                params.setMargins(0, 0, marginPx, 0)
                dayView.layoutParams = params
                if (isActive) {
                    dayView.setBackgroundResource(R.drawable.bg_day_active)
                    dayView.setTextColor(android.graphics.Color.WHITE)
                } else {
                    dayView.setBackgroundResource(R.drawable.bg_day_inactive)
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

            LocaleHelper.setAppLocale(this, selectedLang)
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
            renderAlarms()
        }

        val useSpinner = prefs.getBoolean("time_picker_spinner", false)
        binding.switchTimePickerStyle.isChecked = useSpinner
        binding.switchTimePickerStyle.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("time_picker_spinner", checked).apply()
            Toast.makeText(this, if (checked) getString(R.string.time_picker_style_spinner) else getString(R.string.time_picker_style_clock), Toast.LENGTH_SHORT).show()
        }

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START)
    }

    private fun formatTime(hour24: Int, minute: Int): CharSequence {
        val is24 = prefs.getBoolean("is24h", true)
        return TimeFormatter.format(this, hour24, minute, is24)
    }
}