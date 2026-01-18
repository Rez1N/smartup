package com.frovexsoftware.smartup

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class EditAlarmActivity : AppCompatActivity() {

    private lateinit var tvTimeBig: TextView
    private lateinit var timeSpinnerContainer: LinearLayout
    private lateinit var tvEditTitle: TextView
    private lateinit var tvCareLabel: TextView
    private lateinit var switchCare: SwitchMaterial
    
    private lateinit var tabWeekdays: TextView
    private lateinit var tabEveryday: TextView
    private lateinit var tabWeekends: TextView
    
    private lateinit var chipSnake: LinearLayout
    private lateinit var chipDots: LinearLayout
    private lateinit var chipMath: LinearLayout
    private lateinit var chipColor: LinearLayout
    private lateinit var btnMainChallenge: LinearLayout
    
    private lateinit var btnCreate: MaterialButton
    private lateinit var btnDeleteAlarm: MaterialButton
    
    private val dayViews = mutableMapOf<Int, TextView>()
    private lateinit var prefs: SharedPreferences
    
    private var selectedHour = 7
    private var selectedMinute = 0
    private var selectedChallenge: ChallengeType = ChallengeType.NONE
    private var selectedDays = setOf<Int>()
    private var isEnabled = true
    private var alarmId = -1

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)
        
        prefs = getSharedPreferences("smartup_settings", Context.MODE_PRIVATE)
        
        initializeViews()
        setupListeners()
        loadAlarmData()
        updateUI()
    }

    private fun initializeViews() {
        tvTimeBig = findViewById(R.id.tvTimeBig)
        timeSpinnerContainer = findViewById(R.id.timeSpinnerContainer)
        tvEditTitle = findViewById(R.id.tvEditTitle)
        tvCareLabel = findViewById(R.id.tvCareLabel)
        switchCare = findViewById(R.id.switchCare)
        
        tabWeekdays = findViewById(R.id.tabWeekdays)
        tabEveryday = findViewById(R.id.tabEveryday)
        tabWeekends = findViewById(R.id.tabWeekends)
        
        chipSnake = findViewById(R.id.chipSnake)
        chipDots = findViewById(R.id.chipDots)
        chipMath = findViewById(R.id.chipMath)
        chipColor = findViewById(R.id.chipColor)
        btnMainChallenge = findViewById(R.id.btnMainChallenge)
        
        btnCreate = findViewById(R.id.btnCreate)
        btnDeleteAlarm = findViewById(R.id.btnDeleteAlarm)
        
        // Setup day views
        val dayIds = listOf(R.id.dayMon, R.id.dayTue, R.id.dayWed, R.id.dayThu, R.id.dayFri, R.id.daySat, R.id.daySun)
        dayIds.forEachIndexed { index, id ->
            dayViews[index] = findViewById(id)
        }
        
        setupInlineSpinnerIfNeeded()
    }

    private fun setupInlineSpinnerIfNeeded() {
        val useSpinner = prefs.getBoolean("time_picker_spinner", true) // Default true for drum
        if (useSpinner) {
            tvTimeBig.visibility = View.GONE
            timeSpinnerContainer.visibility = View.VISIBLE

            timeSpinnerContainer.removeAllViews()
            // Create wrapper with theme
            val contextWrapper = android.view.ContextThemeWrapper(this, R.style.TimePickerTheme)

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                // Add background to picker container for "design" request
                setBackgroundResource(R.drawable.bg_card_soft)
                setPadding(0, 12, 0, 12) // Slightly less internal padding for organic feel
            }
            
            val hourPicker = NumberPicker(contextWrapper).apply {
                minValue = 0
                maxValue = 23
                value = selectedHour
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setOnValueChangedListener { _, _, newVal -> selectedHour = newVal }
            }
            
            val minPicker = NumberPicker(contextWrapper).apply {
                minValue = 0
                maxValue = 59
                value = selectedMinute
                setFormatter { i -> String.format("%02d", i) }
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setOnValueChangedListener { _, _, newVal -> selectedMinute = newVal }
            }
            
            layout.addView(hourPicker)
            val spacer = TextView(this).apply { 
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = ":"
                textSize = 32f
                setTextColor(getColor(R.color.purple_dark))
                setPadding(16, 0, 16, 0)
            }
            layout.addView(spacer)
            layout.addView(minPicker)
            timeSpinnerContainer.addView(layout)
        } else {
            tvTimeBig.visibility = View.VISIBLE
            timeSpinnerContainer.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        tvTimeBig.setOnClickListener { openTimePicker() }
        
        chipSnake.setOnClickListener { selectChallenge(ChallengeType.SNAKE) }
        chipDots.setOnClickListener { selectChallenge(ChallengeType.DOTS) }
        chipMath.setOnClickListener { selectChallenge(ChallengeType.MATH) }
        chipColor.setOnClickListener { selectChallenge(ChallengeType.COLOR) }
        btnMainChallenge.setOnClickListener { openChallengeDialog() }
        
        switchCare.setOnCheckedChangeListener { _, isChecked ->
            isEnabled = isChecked
            updateCareText()
        }
        
        tabWeekdays.setOnClickListener { selectTab(0) }
        tabEveryday.setOnClickListener { selectTab(1) }
        tabWeekends.setOnClickListener { selectTab(2) }
        
        dayViews.forEach { (dayIndex, view) ->
            view.setOnClickListener {
                toggleDaySelection(dayIndex)
            }
        }
        
        btnCreate.setOnClickListener { saveAlarm() }
        btnDeleteAlarm.setOnClickListener { deleteAlarm() }
    }

    private fun selectChallenge(challenge: ChallengeType) {
        selectedChallenge = challenge
        updateChallengeUI()
    }

    private fun updateChallengeUI() {
        // Reset all chips
        listOf(chipSnake, chipDots, chipMath, chipColor).forEach { chip ->
            chip.alpha = 0.5f
        }
        
        // Highlight selected
        when (selectedChallenge) {
            ChallengeType.SNAKE -> chipSnake.alpha = 1f
            ChallengeType.DOTS -> chipDots.alpha = 1f
            ChallengeType.MATH -> chipMath.alpha = 1f
            ChallengeType.COLOR -> chipColor.alpha = 1f
            ChallengeType.NONE -> {}
            ChallengeType.TEXT -> {}
            ChallengeType.DATE -> {}
        }
        
        // Update main button text and icon
        val (iconRes, labelRes) = when (selectedChallenge) {
            ChallengeType.SNAKE -> R.drawable.ic_challenge_snake to R.string.chip_snake
            ChallengeType.DOTS -> R.drawable.ic_challenge_dots to R.string.chip_dots
            ChallengeType.MATH -> R.drawable.ic_challenge_math to R.string.chip_math
            ChallengeType.COLOR -> R.drawable.ic_challenge_color to R.string.chip_color
            ChallengeType.NONE -> R.drawable.ic_leaf to R.string.edit_morning_me
            ChallengeType.TEXT -> R.drawable.ic_leaf to R.string.edit_morning_me
            ChallengeType.DATE -> R.drawable.ic_leaf to R.string.edit_morning_me
        }
        
        val icon = findViewById<android.widget.ImageView>(R.id.ivMainChallengeIcon)
        val text = findViewById<TextView>(R.id.tvMainChallengeText)
        icon.setImageResource(iconRes)
        text.text = getString(labelRes)
    }

    private fun openChallengeDialog() {
        val challenges = arrayOf(
            getString(R.string.edit_morning_me),
            getString(R.string.chip_snake),
            getString(R.string.chip_dots),
            getString(R.string.chip_math),
            getString(R.string.chip_color)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.edit_how_to_start)
            .setSingleChoiceItems(challenges, selectedChallenge.ordinal) { dialog, which ->
                selectedChallenge = ChallengeType.values()[which]
                updateChallengeUI()
                dialog.dismiss()
            }
            .show()
    }

    private fun updateCareText() {
        tvCareLabel.text = if (isEnabled) {
            getString(R.string.edit_care_on)
        } else {
            getString(R.string.edit_care_off)
        }
    }

    private fun selectTab(tabIndex: Int) {
        // Clear all day selections
        selectedDays = setOf()
        dayViews.forEach { (_, view) -> view.alpha = 0.5f }
        
        val daysToSelect = when (tabIndex) {
            0 -> listOf(0, 1, 2, 3, 4) // Weekdays Mon-Fri
            1 -> listOf(0, 1, 2, 3, 4, 5, 6) // All days
            2 -> listOf(5, 6) // Weekends Sat-Sun
            else -> emptyList()
        }
        
        selectedDays = daysToSelect.toSet()
        updateDayUI()
        updateTabUI(tabIndex)
    }

    private fun toggleDaySelection(dayIndex: Int) {
        selectedDays = if (dayIndex in selectedDays) {
            selectedDays - dayIndex
        } else {
            selectedDays + dayIndex
        }
        updateDayUI()
        updateTabUI(-1) // No tab selected
    }

    private fun updateDayUI() {
        // Use color change instead of alpha for selection
        dayViews.forEach { (index, view) ->
            if (index in selectedDays) {
                view.setBackgroundResource(R.drawable.bg_day_circle_active)
                view.setTextColor(getColor(R.color.white))
                view.alpha = 1f
            } else {
                view.setBackgroundResource(R.drawable.bg_day_circle_inactive)
                view.setTextColor(getColor(R.color.purple_dark))
                view.alpha = 1f
            }
        }
    }

    private fun updateTabUI(activeTab: Int) {
        val activeBg = R.drawable.bg_tab_active
        val inactiveBg = R.drawable.bg_tab_inactive
        
        // Reset all to inactive style
        listOf(tabWeekdays, tabEveryday, tabWeekends).forEach { 
            it.setBackgroundResource(inactiveBg) 
            it.setTextColor(getColor(R.color.purple_dark))
            it.alpha = 1f
        }

        // Set active one
        val activeView = when(activeTab) {
            0 -> tabWeekdays
            1 -> tabEveryday
            2 -> tabWeekends
            else -> null
        }
        
        activeView?.apply {
            setBackgroundResource(activeBg)
            setTextColor(getColor(R.color.white))
        }
    }

    private fun openTimePicker() {
        val useSpinner = prefs.getBoolean("time_picker_spinner", false)
        
        if (useSpinner) {
            // Dialog with spinners
            val builder = AlertDialog.Builder(this)
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            
            val hourPicker = NumberPicker(this).apply {
                minValue = 0
                maxValue = 23
                value = selectedHour
            }
            val minPicker = NumberPicker(this).apply {
                minValue = 0
                maxValue = 59
                value = selectedMinute
            }
            
            layout.addView(hourPicker)
            layout.addView(minPicker)
            
            builder.setView(layout)
                .setPositiveButton("OK") { _, _ ->
                    selectedHour = hourPicker.value
                    selectedMinute = minPicker.value
                    updateTimeDisplay()
                }
                .show()
        } else {
            // Default TimePickerDialog
            val dialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedHour = hourOfDay
                    selectedMinute = minute
                    updateTimeDisplay()
                },
                selectedHour,
                selectedMinute,
                prefs.getBoolean("is24h", true)
            )
            dialog.show()
        }
    }

    private fun updateTimeDisplay() {
        tvTimeBig.text = String.format("%02d:%02d", selectedHour, selectedMinute)
    }

    private fun loadAlarmData() {
        alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId != -1) {
            selectedHour = intent.getIntExtra("hour", 7)
            selectedMinute = intent.getIntExtra("minute", 0)
            selectedChallenge = ChallengeType.values().getOrElse(intent.getIntExtra("challenge", 0)) { ChallengeType.NONE }
            isEnabled = intent.getBooleanExtra("enabled", true)
            selectedDays = intent.getSerializableExtra("weekdays") as? Set<Int> ?: setOf()
            
            tvEditTitle.text = intent.getStringExtra("description") ?: getString(R.string.edit_morning_ritual)
        } else {
            tvEditTitle.text = getString(R.string.edit_morning_ritual)
            selectedDays = setOf(0, 1, 2, 3, 4) // Default to weekdays
        }
    }

    private fun updateUI() {
        updateTimeDisplay()
        updateChallengeUI()
        updateCareText()
        updateDayUI()
        switchCare.isChecked = isEnabled
    }

    private fun saveAlarm() {
        val resultIntent = Intent().apply {
            putExtra("alarm_id", alarmId)
            putExtra("hour", selectedHour)
            putExtra("minute", selectedMinute)
            putExtra("challenge", selectedChallenge.ordinal)
            putExtra("enabled", isEnabled)
            putExtra("weekdays", selectedDays.toHashSet())
            putExtra("description", tvEditTitle.text.toString())
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun deleteAlarm() {
        val resultIntent = Intent().apply {
            putExtra("delete_alarm_id", alarmId)
        }
        setResult(2, resultIntent) // Custom result code for delete
        finish()
    }
}
