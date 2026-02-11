package com.frovexsoftware.smartup.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.frovexsoftware.smartup.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateChallengeFragment : Fragment() {
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var tvFeedback: android.widget.TextView

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
        tvFeedback = view.findViewById(R.id.tvDateFeedback)

        submitButton.setOnClickListener {
            if (isCorrect(answerEditText.text.toString())) {
                showFeedback(getString(R.string.challenge_complete), isError = false)
                (activity as? ChallengeCallback)?.onChallengeCompleted()
            } else {
                showFeedback(getString(R.string.date_wrong), isError = true)
            }
        }

        return view
    }

    internal fun isCorrect(answer: String): Boolean {
        val locale = Locale.getDefault()
        val date = dateProvider()
        val correctFull = SimpleDateFormat("ddMMyyyy", locale).format(date)
        val correctShort = SimpleDateFormat("ddMMyy", locale).format(date)

        val normalized = answer
            .trim()
            .replace("\\s+".toRegex(), "")
            .replace("[^0-9]".toRegex(), "")

        return normalized == correctFull || normalized == correctShort
    }

    private fun showFeedback(message: String, isError: Boolean) {
        tvFeedback.text = message
        tvFeedback.visibility = android.view.View.VISIBLE
        if (isError) {
            tvFeedback.setBackgroundResource(R.drawable.bg_error_chip)
            tvFeedback.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.challenge_error))
        } else {
            tvFeedback.setBackgroundResource(R.drawable.bg_success_chip)
            tvFeedback.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.challenge_success))
        }
    }
}
