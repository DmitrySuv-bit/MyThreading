package com.example.my_threading

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private companion object {
        const val COUNT_KEY = "com.speedometer_view.count"
    }

    private var count: Int = 0
    private var isStartedTimer = false

    private val timerText: TextView
        get() = findViewById(R.id.count_text)

    private val startButton: Button
        get() = findViewById(R.id.start_button)

    private val stopButton: Button
        get() = findViewById(R.id.stop_button)

    private val pauseButton: Button
        get() = findViewById(R.id.pause_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText.text = count.toString()

        startButton.setOnClickListener {
            startTimer()
        }

        stopButton.setOnClickListener {
            stopTimer()

            count = 0
            timerText.text = count.toString()
        }

        pauseButton.setOnClickListener {
            stopTimer()
        }
    }

    private fun stopTimer() {
        isStartedTimer = false
        startButton.isEnabled = true
    }

    private fun startTimer() {
        isStartedTimer = true
        startButton.isEnabled = false

        createTimer().start()
    }

    private fun createTimer(): Thread {
        return Thread {
            Thread.sleep(1000)

            while (isStartedTimer) {
                ++count

                runOnUiThread {
                    timerText.text = count.toString()
                }

                Thread.sleep(1000)
            }
        }
    }

    private fun startWork() {
        val dataWork = Data.Builder()
            .putInt(MyWorker.TIMER_VALUE__KEY, count)
            .build()

        val request = OneTimeWorkRequestBuilder<MyWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(dataWork)
            .build()

        WorkManager.getInstance(this).enqueue(request)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(COUNT_KEY, count)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        count = savedInstanceState.getInt(COUNT_KEY)

        timerText.text = count.toString()

        startTimer()
    }


    override fun onPause() {
        super.onPause()
        stopTimer()
        startWork()
    }
}