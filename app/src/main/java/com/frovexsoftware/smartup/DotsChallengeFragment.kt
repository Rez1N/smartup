package com.frovexsoftware.smartup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.frovexsoftware.smartup.databinding.FragmentDotsChallengeBinding

class DotsChallengeFragment : Fragment() {

    private var _binding: FragmentDotsChallengeBinding? = null
    private val binding get() = _binding!!

    private val dots = mutableListOf<Button>()
    private var currentDotIndex = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDotsChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegenerateDots.setOnClickListener {
            regenerateDots()
        }

        generateDots()
    }

    private fun generateDots() {
        binding.gridDots.removeAllViews()
        dots.clear()

        val numbers = (1..16).shuffled()

        for (number in numbers) {
            val dot = Button(requireContext())
            dot.text = number.toString()
            dot.setOnClickListener { onDotClicked(it as Button) }
            dots.add(dot)
            binding.gridDots.addView(dot)
        }

        currentDotIndex = 1
        updateProgress()
    }

    private fun onDotClicked(dot: Button) {
        val dotNumber = dot.text.toString().toIntOrNull() ?: return

        if (dotNumber == currentDotIndex) {
            dot.isEnabled = false
            dot.alpha = 0.3f
            currentDotIndex++
            updateProgress()

            if (currentDotIndex > 16) {
                (activity as? StopAlarmActivity)?.onChallengeCompleted()
            }
        }
    }

    private fun regenerateDots() {
        generateDots()
    }

    private fun updateProgress() {
        binding.tvDotsProgress.text = "Прогресс: ${currentDotIndex - 1}/16"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
