package com.frovexsoftware.smartup

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.FrameLayout
import android.animation.ValueAnimator
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.button.MaterialButton
import java.util.Calendar
import java.util.Locale

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
    private lateinit var chipShake: LinearLayout
    private lateinit var chipHold: LinearLayout
    private lateinit var chipRandom: LinearLayout
    private lateinit var btnMainChallenge: LinearLayout
    private lateinit var extraChallengesContainer: LinearLayout
    private lateinit var extraChallengesToggle: LinearLayout
    private lateinit var ivExtraToggle: android.widget.ImageView
    private lateinit var tvTurnOffHint: TextView
    private lateinit var tvExtraChallengesLabel: TextView
    private var isExtraChallengesExpanded = false
    
    private lateinit var btnCreate: androidx.appcompat.widget.AppCompatButton
    private lateinit var btnDeleteAlarm: MaterialButton
    
    private val dayViews = mutableMapOf<Int, TextView>()
    private lateinit var prefs: SharedPreferences
    
    private var selectedHour = 7
    private var selectedMinute = 0
    private var selectedChallenge: ChallengeType = ChallengeType.NONE
    private var selectedDays = setOf<Int>()
    private var isEnabled = true
    private var alarmId = -1
    private var selectedDateMillis: Long? = null
    private var descriptionText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)
        
        prefs = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        
        initializeViews()
        // Set all button texts programmatically for localization and update on locale change
        updateLocalizedTexts()
        setupListeners()
        loadAlarmData()
        updateUI()

    }

    override fun onResume() {
        super.onResume()
        updateLocalizedTexts()
        updateTimeDisplay()
    }

    private fun updateLocalizedTexts() {
        val currentLocale = resources.configuration.locales[0].language
        btnCreate.text = if (currentLocale == "ru") {
            getString(R.string.edit_create_button)
        } else {
            getString(R.string.alarms_add)
        }
        btnDeleteAlarm.text = getString(R.string.edit_delete)

        // Localized weekday names
        val dfs = java.text.DateFormatSymbols(resources.configuration.locales[0])
        val weekDayNames = listOf(
            dfs.shortWeekdays[Calendar.MONDAY],
            dfs.shortWeekdays[Calendar.TUESDAY],
            dfs.shortWeekdays[Calendar.WEDNESDAY],
            dfs.shortWeekdays[Calendar.THURSDAY],
            dfs.shortWeekdays[Calendar.FRIDAY],
            dfs.shortWeekdays[Calendar.SATURDAY],
            dfs.shortWeekdays[Calendar.SUNDAY]
        )
        val calendarDays = listOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )
        dayViews.forEach { (index, view) ->
            val calendarDay = calendarDays.getOrNull(index) ?: Calendar.MONDAY
            val nameIndex = when (calendarDay) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            view.text = weekDayNames[nameIndex]
        }

        // Localize all static UI texts
        tvEditTitle.text = getString(R.string.edit_morning_ritual)
        tvCareLabel.text = if (isEnabled) getString(R.string.edit_care_on) else getString(R.string.edit_care_off)
        tabWeekdays.text = getString(R.string.edit_tab_weekdays)
        tabEveryday.text = getString(R.string.edit_tab_everyday)
        tabWeekends.text = getString(R.string.edit_tab_weekends)
        // Main challenge label and chips will be updated in updateChallengeUI()
        // If you have more static labels, add them here
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
        chipShake = findViewById(R.id.chipShake)
        chipHold = findViewById(R.id.chipHold)
        chipRandom = findViewById(R.id.chipRandom)
        btnMainChallenge = findViewById(R.id.btnMainChallenge)
        extraChallengesContainer = findViewById(R.id.extraChallengesContainer)
        extraChallengesToggle = findViewById(R.id.extraChallengesToggle)
        ivExtraToggle = findViewById(R.id.ivExtraToggle)
        tvTurnOffHint = findViewById(R.id.tvTurnOffHint)
        tvExtraChallengesLabel = findViewById(R.id.tvExtraChallengesLabel)

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
        val useSpinner = prefs.getBoolean("time_picker_spinner", false)
        if (useSpinner) {
            tvTimeBig.visibility = View.GONE
            timeSpinnerContainer.visibility = View.VISIBLE

            timeSpinnerContainer.removeAllViews()
            // Create wrapper with theme
            val contextWrapper = android.view.ContextThemeWrapper(this, R.style.TimePickerTheme)

            val padding = (12 * resources.displayMetrics.density).toInt()
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                // Add background to picker container for "design" request
                setBackgroundResource(R.drawable.bg_card_soft)
                setPadding(padding, padding, padding, padding) // Consistent dp padding
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
                setFormatter { i -> String.format(Locale.getDefault(), "%02d", i) }
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setOnValueChangedListener { _, _, newVal -> selectedMinute = newVal }
            }

            styleNumberPicker(hourPicker)
            styleNumberPicker(minPicker)
            
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
        chipShake.setOnClickListener { selectChallenge(ChallengeType.SHAKE) }
        chipHold.setOnClickListener { selectChallenge(ChallengeType.HOLD) }
        chipRandom.setOnClickListener { selectRandomChallenge() }
        btnMainChallenge.setOnClickListener { openChallengeDialog() }

        extraChallengesToggle.setOnClickListener { toggleExtraChallenges() }
        
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

    private fun selectRandomChallenge() {
        val pool = listOf(
            ChallengeType.SNAKE,
            ChallengeType.DOTS,
            ChallengeType.MATH,
            ChallengeType.COLOR,
            ChallengeType.SHAKE,
            ChallengeType.HOLD
        )
        selectedChallenge = pool.random()
        updateChallengeUI()
    }

    private fun toggleExtraChallenges() {
        val expand = !isExtraChallengesExpanded
        isExtraChallengesExpanded = expand

        tvExtraChallengesLabel.visibility = if (expand) View.GONE else View.VISIBLE

        val startHeight = extraChallengesContainer.height
        if (expand) {
            extraChallengesContainer.visibility = View.VISIBLE
            extraChallengesContainer.alpha = 0f
        }

        extraChallengesContainer.measure(
            View.MeasureSpec.makeMeasureSpec(extraChallengesContainer.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = if (expand) extraChallengesContainer.measuredHeight else 0

        ValueAnimator.ofInt(startHeight, targetHeight).apply {
            duration = 220L
            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                extraChallengesContainer.layoutParams = extraChallengesContainer.layoutParams.apply {
                    height = value
                }
                extraChallengesContainer.requestLayout()
                if (expand) {
                    extraChallengesContainer.alpha = (value.toFloat() / targetHeight.coerceAtLeast(1))
                }
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (!expand) {
                        extraChallengesContainer.visibility = View.GONE
                    }
                    extraChallengesContainer.layoutParams = extraChallengesContainer.layoutParams.apply {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    extraChallengesContainer.alpha = 1f
                }
            })
            start()
        }

        ivExtraToggle.animate()
            .rotation(if (expand) 270f else 90f)
            .setDuration(220L)
            .start()
    }

    private fun updateChallengeUI() {
        // Reset all chips
        listOf(chipSnake, chipDots, chipMath, chipColor).forEach { chip ->
            chip.alpha = 0.5f
        }
        listOf(chipShake, chipHold, chipRandom).forEach { chip ->
            chip.alpha = 0.5f
        }
        
        // Highlight selected
        when (selectedChallenge) {
            ChallengeType.SNAKE -> chipSnake.alpha = 1f
            ChallengeType.DOTS -> chipDots.alpha = 1f
            ChallengeType.MATH -> chipMath.alpha = 1f
            ChallengeType.COLOR -> chipColor.alpha = 1f
            ChallengeType.SHAKE -> chipShake.alpha = 1f
            ChallengeType.HOLD -> chipHold.alpha = 1f
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
            ChallengeType.SHAKE -> R.drawable.ic_challenge_shake to R.string.challenge_shake
            ChallengeType.HOLD -> R.drawable.ic_challenge_hold to R.string.challenge_hold
        }
        
        val icon = findViewById<android.widget.ImageView>(R.id.ivMainChallengeIcon)
        val text = findViewById<TextView>(R.id.tvMainChallengeText)
        icon.setImageResource(iconRes)
        text.text = getString(labelRes)
    }

    private fun openChallengeDialog() {
        val challengeOrder = listOf(
            ChallengeType.NONE,
            ChallengeType.SNAKE,
            ChallengeType.DOTS,
            ChallengeType.MATH,
            ChallengeType.COLOR,
            ChallengeType.SHAKE,
            ChallengeType.HOLD,
            null
        )
        val challenges = arrayOf(
            getString(R.string.edit_morning_me),
            getString(R.string.chip_snake),
            getString(R.string.chip_dots),
            getString(R.string.chip_math),
            getString(R.string.chip_color),
            getString(R.string.challenge_shake),
            getString(R.string.challenge_hold),
            getString(R.string.challenge_random)
        )
        val initialIndex = challengeOrder.indexOf(selectedChallenge).coerceAtLeast(0)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.edit_how_to_start)
            .setSingleChoiceItems(challenges, initialIndex) { dialog, which ->
                val picked = challengeOrder.getOrNull(which)
                if (picked == null) {
                    selectRandomChallenge()
                } else {
                    selectedChallenge = picked
                    updateChallengeUI()
                }
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
            0 -> listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)
            1 -> listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
            2 -> listOf(Calendar.SATURDAY, Calendar.SUNDAY)
            else -> emptyList()
        }
        
        selectedDays = daysToSelect.toSet()
        updateDayUI()
        updateTabUI(tabIndex)
    }

    private fun toggleDaySelection(dayIndex: Int) {
        val calendarDay = when (dayIndex) {
            0 -> Calendar.MONDAY
            1 -> Calendar.TUESDAY
            2 -> Calendar.WEDNESDAY
            3 -> Calendar.THURSDAY
            4 -> Calendar.FRIDAY
            5 -> Calendar.SATURDAY
            6 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
        selectedDays = if (calendarDay in selectedDays) {
            selectedDays - calendarDay
        } else {
            selectedDays + calendarDay
        }
        updateDayUI()
        updateTabUI(-1) // No tab selected
    }

    private fun updateDayUI() {
        // Use color change instead of alpha for selection
        dayViews.forEach { (index, view) ->
            val calendarDay = when (index) {
                0 -> Calendar.MONDAY
                1 -> Calendar.TUESDAY
                2 -> Calendar.WEDNESDAY
                3 -> Calendar.THURSDAY
                4 -> Calendar.FRIDAY
                5 -> Calendar.SATURDAY
                6 -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
            if (calendarDay in selectedDays) {
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
        val is24h = prefs.getBoolean("is24h", true)
        
        if (useSpinner) {
            // Dialog with spinners
            val builder = MaterialAlertDialogBuilder(this, R.style.TimePickerTheme)
            val pickerContext = android.view.ContextThemeWrapper(this, R.style.TimePickerTheme)
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundResource(R.drawable.bg_card_soft)
                setPadding(16, 12, 16, 12)
            }
            
            val hourPicker = NumberPicker(pickerContext).apply {
                if (is24h) {
                    minValue = 0
                    maxValue = 23
                    value = selectedHour
                } else {
                    minValue = 1
                    maxValue = 12
                    value = ((selectedHour + 11) % 12) + 1
                }
            }
            val minPicker = NumberPicker(pickerContext).apply {
                minValue = 0
                maxValue = 59
                value = selectedMinute
            }

            styleNumberPicker(hourPicker)
            styleNumberPicker(minPicker)
            
            layout.addView(hourPicker)
            layout.addView(minPicker)

            val amPmPicker = if (!is24h) {
                NumberPicker(pickerContext).apply {
                    minValue = 0
                    maxValue = 1
                    displayedValues = arrayOf(getString(R.string.time_am), getString(R.string.time_pm))
                    value = if (selectedHour >= 12) 1 else 0
                }
            } else {
                null
            }
            amPmPicker?.let { styleNumberPicker(it) }
            amPmPicker?.let { layout.addView(it) }
            
            builder.setView(layout)
                .setPositiveButton("OK") { _, _ ->
                    selectedMinute = minPicker.value
                    if (is24h) {
                        selectedHour = hourPicker.value
                    } else {
                        val hour12 = hourPicker.value
                        val isPm = (amPmPicker?.value ?: 0) == 1
                        selectedHour = if (isPm) {
                            if (hour12 == 12) 12 else hour12 + 12
                        } else {
                            if (hour12 == 12) 0 else hour12
                        }
                    }
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
                is24h
            )
            dialog.show()
        }
    }

    private fun styleNumberPicker(picker: NumberPicker) {
        val textColor = getColor(R.color.purple_dark)
        for (i in 0 until picker.childCount) {
            val child = picker.getChildAt(i)
            if (child is EditText) {
                child.setTextColor(textColor)
                child.setHintTextColor(textColor)
            }
        }

        picker.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        picker.invalidate()
    }

    private fun updateTimeDisplay() {
        tvTimeBig.text = formatTime(selectedHour, selectedMinute)
    }

    private fun formatTime(hour24: Int, minute: Int): CharSequence {
        val is24 = prefs.getBoolean("is24h", true)
        return if (is24) {
            "%02d:%02d".format(hour24, minute)
        } else {
            val hour12 = ((hour24 + 11) % 12) + 1
            val suffix = if (hour24 >= 12) getString(R.string.time_pm) else getString(R.string.time_am)
            val text = "%d:%02d %s".format(hour12, minute, suffix)
            val spannable = SpannableString(text)
            val suffixStart = text.lastIndexOf(' ') + 1
            if (suffixStart in 1 until text.length) {
                spannable.setSpan(RelativeSizeSpan(0.7f), suffixStart, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(getColor(R.color.purple_medium)), suffixStart, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            spannable
        }
    }

    private fun loadAlarmData() {
        alarmId = intent.getIntExtra("alarm_id", -1)
        val timeMillis = intent.getLongExtra("time", System.currentTimeMillis())
        selectedDateMillis = intent.getLongExtra("date", -1L).let { if (it == -1L) null else it }
        val cal = if (alarmId == -1) {
            Calendar.getInstance().apply {
                add(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } else {
            Calendar.getInstance().apply { timeInMillis = timeMillis }
        }
        selectedHour = cal.get(Calendar.HOUR_OF_DAY)
        selectedMinute = cal.get(Calendar.MINUTE)

        val challengeName = intent.getStringExtra("challenge_type")
        selectedChallenge = ChallengeType.from(challengeName)
        isEnabled = intent.getBooleanExtra("enabled", true)

        val weekdaysList = intent.getIntegerArrayListExtra("weekdays") ?: arrayListOf()
        selectedDays = weekdaysList.toSet()

        descriptionText = ""
        tvEditTitle.text = getString(R.string.edit_morning_ritual)
    }

    private fun updateUI() {
        updateTimeDisplay()
        updateChallengeUI()
        updateCareText()
        updateDayUI()
        updateTabUI(getActiveTabIndex())
        switchCare.isChecked = isEnabled
    }

    private fun getActiveTabIndex(): Int {
        if (selectedDays.isEmpty()) return -1

        val weekdays = setOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY
        )
        val weekends = setOf(Calendar.SATURDAY, Calendar.SUNDAY)
        val everyday = weekdays + weekends

        return when {
            selectedDays == everyday -> 1
            selectedDays == weekdays -> 0
            selectedDays == weekends -> 2
            else -> -1
        }
    }

    private fun saveAlarm() {
        descriptionText = ""
        val cal = TimeLogic.calculateNextTrigger(
            selectedHour,
            selectedMinute,
            selectedDays,
            selectedDateMillis
        )
        val resultIntent = Intent().apply {
            putExtra("alarm_id", alarmId)
            putExtra("time", cal.timeInMillis)
            putExtra("date", selectedDateMillis ?: -1L)
            putExtra("enabled", isEnabled)
            putIntegerArrayListExtra("weekdays", ArrayList(selectedDays))
            putExtra("description", descriptionText)
            putExtra("challenge_type", selectedChallenge.name)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun deleteAlarm() {
        val resultIntent = Intent().apply {
            putExtra("alarm_id", alarmId)
            putExtra("delete", true)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
