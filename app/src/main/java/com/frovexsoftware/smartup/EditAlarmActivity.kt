package com.frovexsoftware.smartup

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Calendar
import java.util.ArrayList

class EditAlarmActivity : AppCompatActivity() {

    private var selectedDateMillis: Long? = null
    private lateinit var timePicker: TimePicker
    private lateinit var descriptionField: EditText
    private lateinit var switchEnabled: SwitchMaterial
    private lateinit var chipGroup: ChipGroup
    private lateinit var weekdayChips: Map<Chip, Int>
    private lateinit var challengeGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)

        timePicker = findViewById(R.id.timePickerEdit)
        descriptionField = findViewById(R.id.etDescription)
        switchEnabled = findViewById(R.id.switchEnabledEdit)
        chipGroup = findViewById(R.id.chipGroupWeekdaysEdit)
        challengeGroup = findViewById(R.id.rgChallengeTypeEdit)

        weekdayChips = mapOf(
            findViewById<Chip>(R.id.chipMonEdit) to Calendar.MONDAY,
            findViewById<Chip>(R.id.chipTueEdit) to Calendar.TUESDAY,
            findViewById<Chip>(R.id.chipWedEdit) to Calendar.WEDNESDAY,
            findViewById<Chip>(R.id.chipThuEdit) to Calendar.THURSDAY,
            findViewById<Chip>(R.id.chipFriEdit) to Calendar.FRIDAY,
            findViewById<Chip>(R.id.chipSatEdit) to Calendar.SATURDAY,
            findViewById<Chip>(R.id.chipSunEdit) to Calendar.SUNDAY
        )

        bindInitialData()
        setupDateButtons()
        findViewById<Button>(R.id.btnSaveAlarm).setOnClickListener { onSave() }
        findViewById<Button>(R.id.btnDeleteAlarm).setOnClickListener { onDelete() }
    }

    private fun bindInitialData() {
        val is24h = intent.getBooleanExtra("is24h", true)
        timePicker.setIs24HourView(is24h)

        val timeMillis = intent.getLongExtra("time", System.currentTimeMillis())
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        timePicker.hour = cal.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = cal.get(Calendar.MINUTE)

        descriptionField.setText(intent.getStringExtra("description") ?: "")

        selectedDateMillis = intent.getLongExtra("date", -1L).let { if (it == -1L) null else it }
        intent.getIntegerArrayListExtra("weekdays")?.toSet()?.let { setWeekdaysChecked(it) }

        val challengeType = ChallengeType.from(intent.getStringExtra("challenge_type"))
        when (challengeType) {
            ChallengeType.NONE -> challengeGroup.check(R.id.rbChallengeNoneEdit)
            ChallengeType.SNAKE -> challengeGroup.check(R.id.rbChallengeSnakeEdit)
            ChallengeType.DOTS -> challengeGroup.check(R.id.rbChallengeDotsEdit)
            ChallengeType.MATH -> challengeGroup.check(R.id.rbChallengeMathEdit)
            ChallengeType.DATE -> challengeGroup.check(R.id.rbChallengeDateEdit)
            ChallengeType.COLOR -> challengeGroup.check(R.id.rbChallengeColorEdit)
            else -> challengeGroup.check(R.id.rbChallengeNoneEdit)
        }

        switchEnabled.isChecked = intent.getBooleanExtra("enabled", true)
    }

    private fun setupDateButtons() {
        val btnSelect = findViewById<Button>(R.id.btnSelectDateEdit)
        val btnClear = findViewById<Button>(R.id.btnClearDateEdit)

        btnSelect.setOnClickListener { openDatePicker() }
        btnClear.setOnClickListener {
            selectedDateMillis = null
            btnSelect.text = "Выбрать дату"
        }

        selectedDateMillis?.let { updateDateButtonText(btnSelect, it) }
    }

    private fun openDatePicker() {
        val btnSelect = findViewById<Button>(R.id.btnSelectDateEdit)
        val now = Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _: DatePicker, y: Int, m: Int, d: Int ->
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
            updateDateButtonText(btnSelect, cal.timeInMillis)
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = now.timeInMillis
        dialog.show()
    }

    private fun updateDateButtonText(button: Button, millis: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        button.text = "Дата: ${cal.get(Calendar.DAY_OF_MONTH)}.${cal.get(Calendar.MONTH) + 1}.${cal.get(Calendar.YEAR)}"
    }

    private fun getSelectedWeekdays(): Set<Int> {
        return weekdayChips.filter { (chip, _) -> chip.isChecked }.values.toSet()
    }

    private fun setWeekdaysChecked(days: Set<Int>) {
        weekdayChips.forEach { (chip, day) -> chip.isChecked = days.contains(day) }
    }

    private fun getSelectedChallengeType(): ChallengeType {
        return when (challengeGroup.checkedRadioButtonId) {
            R.id.rbChallengeSnakeEdit -> ChallengeType.SNAKE
            R.id.rbChallengeDotsEdit -> ChallengeType.DOTS
            R.id.rbChallengeMathEdit -> ChallengeType.MATH
            R.id.rbChallengeDateEdit -> ChallengeType.DATE
            R.id.rbChallengeColorEdit -> ChallengeType.COLOR
            else -> ChallengeType.NONE
        }
    }

    private fun onSave() {
        val selectedWeekdays = getSelectedWeekdays()
        val triggerCal = TimeLogic.calculateNextTrigger(
            timePicker.hour,
            timePicker.minute,
            selectedWeekdays,
            selectedDateMillis
        )

        val result = Intent().apply {
            putExtra("alarm_id", intent.getIntExtra("alarm_id", -1))
            putExtra("time", triggerCal.timeInMillis)
            putExtra("date", selectedDateMillis ?: -1L)
            putExtra("enabled", switchEnabled.isChecked)
            putExtra("description", descriptionField.text.toString())
            putIntegerArrayListExtra("weekdays", ArrayList(selectedWeekdays))
            putExtra("challenge_type", getSelectedChallengeType().name)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    private fun onDelete() {
        val result = Intent().apply {
            putExtra("alarm_id", intent.getIntExtra("alarm_id", -1))
            putExtra("delete", true)
        }
        setResult(RESULT_OK, result)
        finish()
    }
}
