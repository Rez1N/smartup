package com.frovexsoftware.smartup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateChallengeFragment : Fragment() {

    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button

    // Allow injecting a date for testing
    var dateProvider: () -> Date = { Date() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_date_challenge, container, false)

        answerEditText = view.findViewById(R.id.answerEditText)
        submitButton = view.findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            if (isCorrect(answerEditText.text.toString())) {
                (activity as? StopAlarmActivity)?.onChallengeCompleted()
            }
        }

        return view
    }

    internal fun isCorrect(answer: String): Boolean {
        val correctAnswer = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(dateProvider())
        return answer == correctAnswer
    }
}
