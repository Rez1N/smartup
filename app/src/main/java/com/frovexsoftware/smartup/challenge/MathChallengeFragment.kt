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

    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: TextView
    private lateinit var submitButton: Button

    var random: Random = Random() // Allow injecting a predictable random for tests

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_math_challenge, container, false)

        questionTextView = view.findViewById(R.id.questionTextView)
        answerEditText = view.findViewById(R.id.answerEditText)
        submitButton = view.findViewById(R.id.submitButton)

        generateQuestion()

        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString().toIntOrNull()
            if (userAnswer == correctAnswer) {
                (activity as? ChallengeCallback)?.onChallengeCompleted()
            }
        }

        return view
    }

    internal fun generateQuestion() {
        num1 = random.nextInt(10) + 1
        num2 = random.nextInt(10) + 1
        correctAnswer = num1 + num2

        // This will crash in a unit test, but we are calling the method directly
        // and won't be in a scenario where questionTextView is used without a view.
        if (this::questionTextView.isInitialized) {
            questionTextView.text = "$num1 + $num2 = ?"
        }
    }
}
