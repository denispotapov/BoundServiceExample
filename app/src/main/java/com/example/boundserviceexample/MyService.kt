package com.example.boundserviceexample

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import timber.log.Timber

class MyService : Service() {

    private val binder = MyBinder()
    private var progress = 0
    private var isPaused = true
    private val maxValue = 5000
    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        handler = Handler()

    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    inner class MyBinder : Binder() {
        fun getService(): MyService = this@MyService
    }

    fun startPretendLongRunningTask() {
        val runnable = object : Runnable {
            override fun run() {
                if (progress >= maxValue || isPaused) {
                    Timber.d("run: removing callbacks")
                    handler.removeCallbacks(this)
                    pausePretendLongRunningTask()
                } else {
                    Timber.d("run: progress: $progress")
                    progress += 100
                    handler.postDelayed(this, 100)
                }
            }
        }
        handler.postDelayed(runnable, 100)
    }

    fun pausePretendLongRunningTask() {
        isPaused = true
    }

    fun unPausePretendLongRunningTask() {
        isPaused = false
        startPretendLongRunningTask()
    }

    fun getIsPaused(): Boolean = isPaused

    fun getProgress(): Int = progress

    fun getMaxValue(): Int = maxValue

    fun resetTask() {
        progress = 0
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}