package com.frovexsoftware.smartup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frovexsoftware.smartup.databinding.FragmentSnakeChallengeBinding

class SnakeChallengeFragment : Fragment() {

    private var _binding: FragmentSnakeChallengeBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSnakeChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSnakeUp.setOnClickListener { binding.snakeBoard.setDirection(SnakeBoardView.Direction.UP) }
        binding.btnSnakeDown.setOnClickListener { binding.snakeBoard.setDirection(SnakeBoardView.Direction.DOWN) }
        binding.btnSnakeLeft.setOnClickListener { binding.snakeBoard.setDirection(SnakeBoardView.Direction.LEFT) }
        binding.btnSnakeRight.setOnClickListener { binding.snakeBoard.setDirection(SnakeBoardView.Direction.RIGHT) }

        binding.snakeBoard.onGameWon = {
            (activity as? StopAlarmActivity)?.onChallengeCompleted()
        }

        runnable = Runnable {
            binding.snakeBoard.update()
            binding.tvSnakeScore.text = getString(
                R.string.snake_score,
                binding.snakeBoard.currentScore
            )
            handler.postDelayed(runnable, 280)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(runnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
