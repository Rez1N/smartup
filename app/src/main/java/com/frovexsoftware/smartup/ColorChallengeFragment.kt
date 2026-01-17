package com.frovexsoftware.smartup

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Random

class ColorChallengeFragment : Fragment() {

    private lateinit var colorView: View
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button

    private var correctColorName: String = ""

    // Allow injecting a predictable random for tests
    var random: Random = Random()

    private val colors = linkedMapOf(
        Color.RED to R.string.color_red,
        Color.GREEN to R.string.color_green,
        Color.BLUE to R.string.color_blue,
        Color.YELLOW to R.string.color_yellow,
        Color.BLACK to R.string.color_black,
        Color.WHITE to R.string.color_white
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_color_challenge, container, false)

        colorView = view.findViewById(R.id.colorView)
        answerEditText = view.findViewById(R.id.answerEditText)
        submitButton = view.findViewById(R.id.submitButton)

        generateChallenge()

        submitButton.setOnClickListener {
            if (isCorrect(answerEditText.text.toString())) {
                (activity as? StopAlarmActivity)?.onChallengeCompleted()
            }
        }

        return view
    }

    internal fun generateChallenge() {
        val randomColor = colors.keys.elementAt(random.nextInt(colors.size))
        correctColorName = getString(colors.getValue(randomColor))
        if (this::colorView.isInitialized) {
            colorView.setBackgroundColor(randomColor)
        }
    }

    internal fun isCorrect(answer: String): Boolean {
        return answer.equals(correctColorName, ignoreCase = true)
    }
}
