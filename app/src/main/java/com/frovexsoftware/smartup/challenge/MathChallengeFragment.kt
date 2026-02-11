package com.frovexsoftware.smartup.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.frovexsoftware.smartup.R
import java.util.Random

class MathChallengeFragment : Fragment() {
    internal var num1 = 0
    internal var num2 = 0
    internal var correctAnswer = 0
    private var operator = "+"

    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: TextView
    private lateinit var submitButton: Button
    private lateinit var tvFeedback: TextView

    var random: Random = Random()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_math_challenge, container, false)

        questionTextView = view.findViewById(R.id.questionTextView)
        answerEditText = view.findViewById(R.id.answerEditText)
        submitButton = view.findViewById(R.id.submitButton)
        tvFeedback = view.findViewById(R.id.tvMathFeedback)

        generateQuestion()

        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString().toIntOrNull()
            if (userAnswer == correctAnswer) {
                showFeedback(getString(R.string.challenge_complete), isError = false)
                (activity as? ChallengeCallback)?.onChallengeCompleted()
            } else {
                showFeedback(getString(R.string.math_wrong), isError = true)
                generateQuestion()
            }
        }

        return view
    }

    internal fun generateQuestion() {
        // Randomly pick +, -, or ×
        when (random.nextInt(3)) {
            0 -> {
                num1 = random.nextInt(20) + 1
                num2 = random.nextInt(20) + 1
                correctAnswer = num1 + num2
                operator = "+"
            }
            1 -> {
                num1 = random.nextInt(20) + 10
                num2 = random.nextInt(num1) + 1
                correctAnswer = num1 - num2
                operator = "−"
            }
            2 -> {
                num1 = random.nextInt(9) + 2
                num2 = random.nextInt(9) + 2
                correctAnswer = num1 * num2
                operator = "×"
            }
        }

        if (this::questionTextView.isInitialized) {
            questionTextView.text = "$num1 $operator $num2 = ?"
        }
    }

    private fun showFeedback(message: String, isError: Boolean) {
        tvFeedback.text = message
        tvFeedback.visibility = View.VISIBLE
        if (isError) {
            tvFeedback.setBackgroundResource(R.drawable.bg_error_chip)
            tvFeedback.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.challenge_error))
        } else {
            tvFeedback.setBackgroundResource(R.drawable.bg_success_chip)
            tvFeedback.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.challenge_success))
        }
    }
}
