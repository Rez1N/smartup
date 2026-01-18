package com.frovexsoftware.smartup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frovexsoftware.smartup.databinding.FragmentDotsChallengeBinding
import com.frovexsoftware.smartup.PatternLockView.OnPatternCompleteListener

class DotsChallengeFragment : Fragment() {
    private var _binding: FragmentDotsChallengeBinding? = null
    private val binding get() = _binding!!

    private data class DotPattern(val name: String, val sequence: List<Int>)

    private val availablePatterns = listOf(
        // Г: левая колонка вниз + нижняя строка вправо
        DotPattern("Г", listOf(0, 3, 6, 7, 8)),
        // Z: верхний ряд, диагональ, нижний ряд
        DotPattern("Z", listOf(0, 1, 2, 4, 6, 7, 8)),
        // N: диагональ снизу слева вверх + диагональ вниз вправо
        DotPattern("N", listOf(6, 3, 0, 4, 8, 5, 2)),
        // П: верхняя строка и боковые вниз
        DotPattern("П", listOf(0, 1, 2, 5, 8, 7, 6, 3)),
        // L: правая колонка вниз + нижняя строка влево (зеркало Г)
        DotPattern("L", listOf(2, 5, 8, 7, 6)),
        // U: боковые вниз и вверх по диагонали вправо
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
            (activity as? StopAlarmActivity)?.onChallengeCompleted()
        } else {
            binding.tvDotsError.text = getString(R.string.dots_wrong_pattern)
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
