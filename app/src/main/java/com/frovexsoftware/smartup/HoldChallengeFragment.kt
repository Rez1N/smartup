package com.frovexsoftware.smartup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import androidx.fragment.app.Fragment

class HoldChallengeFragment : Fragment() {

    private lateinit var btnHold: MaterialButton
    private lateinit var progress: CircularProgressIndicator

    private val handler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var running = false

    private val ticker = object : Runnable {
        override fun run() {
            if (!running) return
            val elapsed = System.currentTimeMillis() - startTime
            val percent = (elapsed.coerceAtMost(HOLD_DURATION_MS) * 1000L / HOLD_DURATION_MS).toInt()
            progress.progress = percent
            if (elapsed >= HOLD_DURATION_MS) {
                running = false
                (activity as? StopAlarmActivity)?.onChallengeCompleted()
                return
            }
            handler.postDelayed(this, 16L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_hold_challenge, container, false)
        btnHold = view.findViewById(R.id.btnHold)
        progress = view.findViewById(R.id.holdProgress)
        progress.max = 1000
        progress.progress = 0

        btnHold.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> startHold()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> cancelHold()
            }
            true
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        cancelHold()
    }

    private fun startHold() {
        if (running) return
        running = true
        startTime = System.currentTimeMillis()
        handler.post(ticker)
    }

    private fun cancelHold() {
        running = false
        handler.removeCallbacks(ticker)
        progress.progress = 0
    }

    private companion object {
        const val HOLD_DURATION_MS = 10_000L
    }
}
