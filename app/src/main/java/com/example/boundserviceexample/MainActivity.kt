package com.example.boundserviceexample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.boundserviceexample.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var service: MyService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        binding.btnToggleUpdates.setOnClickListener {
            toggleUpdates()
        }

        viewModel.getBinder().observe(this, Observer {
            if (it != null) {
                Timber.d("OnChanged: connected to service")
                service = it.getService()
            } else {
                Timber.d("OnChanged: unbound from service")
            }
        })

        viewModel.getIsProgressUpdating().observe(this, Observer {

            val handler = Handler()
            val runnable = object : Runnable {
                override fun run() {
                    if (it) {
                        if (viewModel.getBinder().value != null) {
                            if (service.getProgress() == service.getMaxValue()) {
                                viewModel.setUpdating(false)
                            }
                            binding.progressBar.progress = service.getProgress()
                            binding.progressBar.max = service.getMaxValue()
                            val progress = "${100 * service.getProgress() / service.getMaxValue()}%"
                            binding.textView.text = progress
                            handler.postDelayed(this, 100)
                        }
                    } else {
                        handler.removeCallbacks(this)
                    }
                }
            }
            if (it) {
                binding.btnToggleUpdates.text = "Pause"
                handler.postDelayed(runnable, 100)
            } else {
                if (service.getProgress() == service.getMaxValue()) {
                    binding.btnToggleUpdates.text = "Restart"
                } else binding.btnToggleUpdates.text = "Start"
            }
        })
    }

    private fun toggleUpdates() {
        if (service.getProgress() == service.getMaxValue()) {
            service.resetTask()
            binding.btnToggleUpdates.text = "Start"
        } else {
            if (service.getIsPaused()) {
                service.unPausePretendLongRunningTask()
                viewModel.setUpdating(true)
            } else {
                service.pausePretendLongRunningTask()
                viewModel.setUpdating(false)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.getBinder() != null) {
            unbindService(viewModel.getServiceConnection())
        }
    }

    override fun onResume() {
        super.onResume()
        startService()
    }

    private fun startService() {
        val serviceIntent = Intent(this, MyService::class.java)
        startService(serviceIntent)
        bindService()
    }

    private fun bindService() {
        val serviceIntent = Intent(this, MyService::class.java)
        bindService(serviceIntent, viewModel.getServiceConnection(), Context.BIND_AUTO_CREATE)
    }
}