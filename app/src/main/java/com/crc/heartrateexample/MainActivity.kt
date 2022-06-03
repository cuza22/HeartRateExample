package com.crc.heartrateexample

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import com.crc.heartrateexample.databinding.ActivityMainBinding
import java.util.*
import java.util.Calendar.getInstance
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class MainActivity : Activity() {

    val TAG : String = "HeartRate"

    private lateinit var binding: ActivityMainBinding

    lateinit var sensorManager : SensorManager

    lateinit var accelerometerListener : SensorEventListener
    lateinit var accelerometerSensor : Sensor
    private lateinit var gyrometerListener: SensorEventListener
    private lateinit var gyrometerSensor : Sensor
    private lateinit var magnetometerListener: SensorEventListener
    private lateinit var magnetometerSensor: Sensor
    private lateinit var gravityListener : SensorEventListener
    private lateinit var gravitySensor : Sensor
    lateinit var heartRateListener : SensorEventListener
    lateinit var heartRateSensor : Sensor

    lateinit var cw : CSVWrite
    var header = "year,month,date,hour,min,sec,millis,accX,accY,accZ,HR\n"
//    var header = "year,month,date,hour,min,sec,millis,accX,accY,accZ,gyroX,gyroY,gyroZ,HR\n"
//    var header = "year,month,date,hour,min,sec,millis,accX,accY,accZ,gyroX,gyroY,gyroZ,magX,magY,magZ,HR\n"
//    var header = "year,month,date,hour,min,sec,millis,accX,accY,accZ,gyroX,gyroY,gyroZ,magX,magY,magZ,graX,graY,graZ,HR\n"
    var data = ""
    var hr : Float = 0F
    var accX: Float = 0.0f
    var accY: Float = 0.0f
    var accZ: Float = 0.0f
    var gyroX : Float = 0.0f
    var gyroY : Float = 0.0f
    var gyroZ : Float = 0.0f
    var magX : Float = 0.0f
    var magY : Float = 0.0f
    var magZ : Float = 0.0f
    var graX : Float = 0.0f
    var graY : Float = 0.0f
    var graZ : Float = 0.0f


    lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // UI
        textView = findViewById(R.id.text)

        // sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometerListener = AccListener()
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST)
//        gyrometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//        gyrometerListener = GyroListener()
//        sensorManager.registerListener(gyrometerListener, gyrometerSensor, SensorManager.SENSOR_DELAY_FASTEST)
//        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//        magnetometerListener = MagListener()
//        sensorManager.registerListener(magnetometerListener, magnetometerSensor, SensorManager.SENSOR_DELAY_FASTEST)
//        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
//        gravityListener = GravityListener()
//        sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST)
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        heartRateListener = HeartRateListener()
        sensorManager.registerListener(heartRateListener, heartRateSensor, SensorManager.SENSOR_DELAY_FASTEST)

        // csv
        cw = CSVWrite()
        cw.createCsv(header, currentDate(), "SensorData")
        var isTime = false
        var isWritten = false
        var count = 0
        val startTime = System.currentTimeMillis()

        thread(start=true) {
            while (true) {
                runOnUiThread{
                    textView.text = "$hr"
                    val calendar = getInstance()

                    // data
                    if (calendar.timeInMillis%16 < 2 && !isTime) {
                        Log.d(TAG, "hr: $hr   accX: $accX   count: $count")
                        data += dataCollectedDate() + "$accX,$accY,$accZ,$hr\n"
//                        Log.d(TAG, "hr: $hr   accX: $accX  gyroX: $gyroX   count: $count")
//                        data += dataCollectedDate() + "$accX,$accY,$accZ,$gyroX,$gyroY,$gyroZ,$hr\n"
//                        Log.d(TAG, "hr: $hr   accX: $accX  gyroX: $gyroX  magX: $magX  count: $count")
//                        data += dataCollectedDate() + "$accX,$accY,$accZ,$gyroX,$gyroY,$gyroZ,$magX,$magY,$magZ,$hr\n"
//                        Log.d(TAG, "hr: $hr   accX: $accX  gyroX: $gyroX  magX: $magX  graX: $graX  count: $count")
//                        data += dataCollectedDate() + "$accX,$accY,$accZ,$gyroX,$gyroY,$gyroZ,$magX,$magY,$magZ,$graX,$graY,$graZ,$hr\n"
                        isTime = true
                        count++
                    }
                    if(calendar.timeInMillis%16 > 13){ isTime = false }
                    // write
                    if(count%(60*60) == 0 && !isWritten) {
                        cw.writeCsv(data, "SensorData")
                        data = ""
                        isWritten = true
                    }
                    else { isWritten = false }
                    // finish
                    if ( System.currentTimeMillis() - startTime > 1000 * 60 * 11) {
//                    if(count == 660 * 10 && !isWritten) {
                        cw.writeCsv(data,"SensorData")
                        finishAffinity()
                        System.runFinalization()
                        exitProcess(0)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    inner class HeartRateListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                hr = event.values[0]
            } else {
                Log.d(TAG, "sensor event is null")
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    inner class AccListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                accX = event.values[0]
                accY = event.values[1]
                accZ = event.values[2]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    inner class GyroListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    inner class MagListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                magX = event.values[0]
                magY = event.values[1]
                magZ = event.values[2]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
    inner class GravityListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                graX = event.values[0]
                graY = event.values[1]
                graZ = event.values[2]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    private fun currentDate(): String {
        val calendar : Calendar = GregorianCalendar(Locale.KOREA)
        return "${calendar.get(Calendar.YEAR)}_${calendar.get(Calendar.MONTH)+1}_${calendar.get(
            Calendar.DATE
        )}_${calendar.get(
            Calendar.HOUR_OF_DAY
        )}_${calendar.get(Calendar.MINUTE)}_${calendar.get(Calendar.SECOND)}_"
    }
    private fun dataCollectedDate(): String {
        val calendar : Calendar = GregorianCalendar(Locale.KOREA)
        return "${calendar.get(Calendar.YEAR)},${calendar.get(Calendar.MONTH)+1},${calendar.get(
            Calendar.DATE
        )},${calendar.get(
            Calendar.HOUR_OF_DAY
        )},${calendar.get(Calendar.MINUTE)},${calendar.get(Calendar.SECOND)},${calendar.get(Calendar.MILLISECOND)},"
    }
}