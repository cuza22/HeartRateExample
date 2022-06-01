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

class MainActivity : Activity() {

    val TAG : String = "HeartRate"

    private lateinit var binding: ActivityMainBinding

    lateinit var sensorManager : SensorManager
    lateinit var heartRateListener : SensorEventListener
    lateinit var heartRateSensor : Sensor

    lateinit var cw : CSVWrite
    var heartRateData = "year,month,date,hour,min,sec,millis,HR\n"
    var hr : Float = 0F

    lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // UI
        textView = findViewById(R.id.text)

        // heart rate
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        heartRateListener = HeartRateListener()
        sensorManager.registerListener(heartRateListener, heartRateSensor, SensorManager.SENSOR_DELAY_FASTEST)

        // csv
        cw = CSVWrite()
        cw.createCsv("", currentDate(), "HR")
        var isTime = false
        var isWritten = false
        var count = 0

        thread(start=true) {
            while (true) {
                runOnUiThread{
                    textView.text = "$hr"
                    val calendar = getInstance()
                    if (calendar.timeInMillis%100 < 10 && !isTime) {
                        Log.d(TAG, "hr: $hr   count: $count")
                        heartRateData += dataCollectedDate() + "$hr\n"
                        isTime = true
                        count++
                    }
                    if(calendar.timeInMillis%100 > 90){ isTime = false }
                    if(count == 660 * 10 && !isWritten) {
                        cw.writeCsv(heartRateData, "HR")
                        isWritten = true
                        finish()
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