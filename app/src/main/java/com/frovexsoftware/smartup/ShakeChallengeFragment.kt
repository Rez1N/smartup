package com.frovexsoftware.smartup

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.math.sqrt

class ShakeChallengeFragment : Fragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var tvShakeCount: TextView

    private var lastShakeTime = 0L
    private var shakeCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_shake_challenge, container, false)
        tvShakeCount = view.findViewById(R.id.tvShakeCount)
        updateCounter()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
        val now = System.currentTimeMillis()

        if (gForce > SHAKE_THRESHOLD_G && now - lastShakeTime > SHAKE_DEBOUNCE_MS) {
            lastShakeTime = now
            shakeCount += 1
            updateCounter()
            if (shakeCount >= REQUIRED_SHAKES) {
                (activity as? StopAlarmActivity)?.onChallengeCompleted()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun updateCounter() {
        tvShakeCount.text = getString(R.string.shake_progress, shakeCount, REQUIRED_SHAKES)
    }

    private companion object {
        const val REQUIRED_SHAKES = 10
        const val SHAKE_THRESHOLD_G = 2.6f
        const val SHAKE_DEBOUNCE_MS = 600L
    }
}
