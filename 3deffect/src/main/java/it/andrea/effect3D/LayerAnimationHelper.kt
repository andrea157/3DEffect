package it.andrea.effect3D

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Created by a.gerardi@wakala.it on 16-01-2020
 */
class LayerAnimationHelper(
    context: Context,
    private val viewLayers: ArrayList<View> = ArrayList()
) :
    LifecycleObserver,
    SensorEventListener2 {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    private val gravityVector = DoubleArray(3)
    private val lastGravity = DoubleArray(2)

    init {
        registerSensorListener()
    }

    fun addLayers(viewLayers: ArrayList<View>) {
        this.viewLayers.apply {
            clear()
            addAll(viewLayers)
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun registerSensorListener() {
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onFlushCompleted(p0: Sensor?) {}

    override fun onSensorChanged(sensorEvent: SensorEvent?) {

        sensorEvent?.values?.let { values ->

            //1. Get data from accelerometer
            gravityVector[0] = gravityVector[0] * gravityFilterK + (1 - gravityFilterK) * values[0]
            gravityVector[1] = gravityVector[1] * gravityFilterK + (1 - gravityFilterK) * values[1]
            gravityVector[2] = gravityVector[2] * gravityFilterK + (1 - gravityFilterK) * values[2]

            //2. Convert accelerometer data to rotation angles
            var gX = gravityVector[0]
            var gY = gravityVector[1]
            var gZ = gravityVector[2]
            var roll = atan2(gX, gZ) * 180 / PI
            var pitch = atan2(gY, sqrt(gX * gX + gZ * gZ)) * 180 / PI

            // normalize gravity vector at first
            sqrt(gX * gX + gY * gY + gZ * gZ).let { gSum ->
                gX /= gSum
                gY /= gSum
                gZ /= gSum
            }


            if (gZ != 0.0) roll = atan2(gX, gZ) * 180 / PI
            pitch = sqrt(gX * gX + gZ * gZ)

            if (pitch != 0.0) pitch = atan2(gY, pitch) * 180 / PI

            var dgX = roll - lastGravity[0]
            var dgY = pitch - lastGravity[1]

            // if device orientation is close to vertical – rotation around x is almost undefined – skip!
            if (gY > 0.99) dgX = 0.0
            // if rotation was too intensive – more than 180 degrees – skip it
            if (dgX > 180) dgX = 0.0
            if (dgX < -180) dgX = 0.0
            if (dgY > 180) dgY = 0.0
            if (dgY < -180) dgY = 0.0

            lastGravity[0] = roll
            lastGravity[1] = pitch

            //3. Perform interface shift
            // Parallax effect – if gravity vector was changed we shift particles
            if (dgX != 0.0 || dgY != 0.0) {

                Log.i(TAG, "onSensorChanged: dgX -> $dgX  dgY -> $dgY")
                viewLayers.forEach { layer ->
                    layer.x += dgX.toFloat() * (1.0f + 10.0f * layer.z)
                    layer.y += dgY.toFloat() * (1.0f + 10.0f * layer.z)
                }

            }

        }

    }
}

private const val TAG = "LayerAnimationHelper"