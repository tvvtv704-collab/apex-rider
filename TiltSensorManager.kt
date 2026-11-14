package com.example.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TiltSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val tiltSensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isAvailable: Boolean = tiltSensor != null

    private val _steerInput = MutableStateFlow(0f)
    val steerInput: StateFlow<Float> = _steerInput.asStateFlow()

    private var rawLateral = 0f
    private var calibrationOffset = 0f
    private var filteredSteer = 0f
    private var isListening = false

    fun start() {
        if (isListening || tiltSensor == null) return
        sensorManager?.registerListener(this, tiltSensor, SensorManager.SENSOR_DELAY_GAME)
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        filteredSteer = 0f
        _steerInput.value = 0f
    }

    fun calibrateZero() {
        calibrationOffset = rawLateral
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        @Suppress("DEPRECATION")
        val rotation = windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        val lateral = when (rotation) {
            Surface.ROTATION_90 -> y
            Surface.ROTATION_270 -> -y
            Surface.ROTATION_180 -> x
            else -> -x
        }
        rawLateral = lateral
        val adjusted = (lateral - calibrationOffset) / 4.2f
        val deadzone = if (kotlin.math.abs(adjusted) < 0.06f) 0f else adjusted
        val clamped = deadzone.coerceIn(-1f, 1f)
        filteredSteer = filteredSteer * 0.72f + clamped * 0.28f
        _steerInput.value = filteredSteer
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
