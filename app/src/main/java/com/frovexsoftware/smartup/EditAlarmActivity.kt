package com.frovexsoftware.smartup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.ArrayList
import java.util.Calendar
import java.util.LinkedHashSet

class EditAlarmActivity : AppCompatActivity() {

    private var selectedDateMillis: Long? = null
    private var isNew: Boolean = false
    private lateinit var timePicker: TimePicker
    private lateinit var switchEnabled: SwitchMaterial
    private lateinit var tvRepeatValue: TextView
    private lateinit var tvNameValue: TextView
    private lateinit var tvMelodyValue: TextView
    private var selectedWeekdays: MutableSet<Int> = LinkedHashSet()
    private var descriptionText: String = "Будильник"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)

        timePicker = findViewById(R.id.timePickerEdit)
        switchEnabled = findViewById(R.id.switchEnabledEdit)
        tvRepeatValue = findViewById(R.id.tvRepeatValue)
        tvNameValue = findViewById(R.id.tvNameValue)
        tvMelodyValue = findViewById(R.id.tvMelodyValue)
        isNew = intent.getIntExtra("alarm_id", -1) == -1

        bindInitialData()
        bindUi()
    }

    private fun bindUi() {
        findViewById<View>(R.id.btnCancel).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSaveTop).setOnClickListener { onSave() }
        findViewById<View>(R.id.btnSaveAlarm).setOnClickListener { onSave() }
        findViewById<View>(R.id.btnDeleteAlarm).apply {
            visibility = if (isNew) View.GONE else View.VISIBLE
            setOnClickListener { onDelete() }
        }

        findViewById<View>(R.id.containerRepeat).setOnClickListener { openRepeatDialog() }
        findViewById<View>(R.id.containerName).setOnClickListener { openNameDialog() }
        findViewById<View>(R.id.containerMelody).setOnClickListener {
            Toast.makeText(this, "Выбор мелодии пока недоступен", Toast.LENGTH_SHORT).show()
        }

        updateRepeatValue()
        tvNameValue.text = descriptionText
    }

    private fun bindInitialData() {
        val is24h = intent.getBooleanExtra("is24h", true)
        timePicker.setIs24HourView(is24h)

        val timeMillis = intent.getLongExtra("time", System.currentTimeMillis())
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        timePicker.hour = cal.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = cal.get(Calendar.MINUTE)

        descriptionText = intent.getStringExtra("description")?.takeIf { it.isNotBlank() } ?: "Будильник"

        selectedDateMillis = intent.getLongExtra("date", -1L).let { if (it == -1L) null else it }
        intent.getIntegerArrayListExtra("weekdays")?.let { selectedWeekdays.addAll(it.toSet()) }

        switchEnabled.isChecked = intent.getBooleanExtra("enabled", true)
        tvMelodyValue.text = "Радиус"
    }

    private fun openRepeatDialog() {
        val names = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val values = listOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )
        val checked = values.map { selectedWeekdays.contains(it) }.toBooleanArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Повтор")
            .setMultiChoiceItems(names.toTypedArray(), checked) { _, which, isChecked ->
                val day = values[which]
                if (isChecked) selectedWeekdays.add(day) else selectedWeekdays.remove(day)
            }
            .setPositiveButton("Готово") { _, _ -> updateRepeatValue() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openNameDialog() {
        val input = EditText(this).apply {
            setText(descriptionText)
            setSelection(text.length)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Название")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                descriptionText = input.text.toString().ifBlank { "Будильник" }
                tvNameValue.text = descriptionText
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateRepeatValue() {
        val label = when {
            selectedWeekdays.isEmpty() -> "Никогда"
            selectedWeekdays.containsAll(listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)) && selectedWeekdays.size == 5 -> "Будние дни"
            selectedWeekdays.size == 7 -> "Каждый день"
            else -> selectedWeekdays.sorted().joinToString(" ") { dayToShort(it) }
        }
        tvRepeatValue.text = label
    }

    private fun dayToShort(day: Int): String = when (day) {
        Calendar.MONDAY -> "Пн"
        Calendar.TUESDAY -> "Вт"
        Calendar.WEDNESDAY -> "Ср"
        Calendar.THURSDAY -> "Чт"
        Calendar.FRIDAY -> "Пт"
        Calendar.SATURDAY -> "Сб"
        Calendar.SUNDAY -> "Вс"
        else -> ""
    }

    private fun onSave() {
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
            putExtra("description", descriptionText)
            putIntegerArrayListExtra("weekdays", ArrayList(selectedWeekdays))
            putExtra("challenge_type", ChallengeType.NONE.name)
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
