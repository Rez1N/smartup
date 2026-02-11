package com.frovexsoftware.smartup.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frovexsoftware.smartup.R
import com.frovexsoftware.smartup.databinding.FragmentDotsChallengeBinding
import com.frovexsoftware.smartup.challenge.view.PatternLockView.OnPatternCompleteListener

class DotsChallengeFragment : Fragment() {
    private var _binding: FragmentDotsChallengeBinding? = null
    private val binding get() = _binding!!

    private data class DotPattern(val name: String, val sequence: List<Int>)

    private val availablePatterns = listOf(
        // Г: вверх по левой стороне, потом вправо по верху
        DotPattern("Г", listOf(6, 3, 0, 1, 2)),
        // Z: верхний ряд, диагональ, нижний ряд
        DotPattern("Z", listOf(0, 1, 2, 4, 6, 7, 8)),
        // N: вверх по левой, диагональ вниз, вверх по правой
        DotPattern("N", listOf(6, 3, 0, 4, 8, 5, 2)),
        // П: вверх по левой, по верху, вниз по правой
        DotPattern("П", listOf(6, 3, 0, 1, 2, 5, 8)),
        // L: вниз по левой, вправо по низу
        DotPattern("L", listOf(0, 3, 6, 7, 8)),
        // U: вниз по левой, по низу, вверх по правой
        DotPattern("U", listOf(0, 3, 6, 7, 8, 5, 2))
    )

    private var targetPattern: DotPattern = availablePatterns.random()

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

        binding.patternView.listener = object : OnPatternCompleteListener {
            override fun onPatternCompleted(pattern: List<Int>) {
                handlePatternComplete(pattern)
            }
        }
        binding.patternView.previewEnabled = false
        binding.patternView.progressListener = { size ->
            updateProgress(size)
        }

        binding.btnRegenerateDots.setOnClickListener {
            regeneratePattern()
        }

        binding.btnResetDots.setOnClickListener {
            resetUserPattern(showMessage = false)
        }

        generatePattern()
    }

    private fun generatePattern() {
        targetPattern = availablePatterns.random()
        binding.tvDotsError.text = ""
        binding.tvDotsPatternName.text = getString(
            R.string.dots_pattern_name,
            targetPattern.name
        )
        binding.patternView.setTargetPattern(targetPattern.sequence)
        updateProgress(0)
    }

    private fun regeneratePattern() {
        binding.patternView.resetUserPattern()
        generatePattern()
    }

    private fun handlePatternComplete(pattern: List<Int>) {
        if (pattern == targetPattern.sequence) {
            binding.tvDotsError.text = getString(R.string.dots_pattern_complete)
            binding.tvDotsError.setBackgroundResource(R.drawable.bg_success_chip)
            binding.tvDotsError.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.challenge_success))
            (activity as? ChallengeCallback)?.onChallengeCompleted()
        } else {
            binding.tvDotsError.text = getString(R.string.dots_wrong_pattern)
            binding.tvDotsError.setBackgroundResource(R.drawable.bg_error_chip)
            binding.tvDotsError.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.challenge_error))
            binding.patternView.resetUserPattern()
            updateProgress(0)
        }
    }

    private fun resetUserPattern(showMessage: Boolean) {
        binding.patternView.resetUserPattern()
        if (!showMessage) {
            binding.tvDotsError.text = ""
        }
        updateProgress(0)
    }

    private fun updateProgress(selected: Int) {
        binding.tvDotsProgress.text = getString(
            R.string.dots_progress,
            selected,
            targetPattern.sequence.size
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
