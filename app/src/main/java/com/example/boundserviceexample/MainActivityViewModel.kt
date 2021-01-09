package com.example.boundserviceexample

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class MainActivityViewModel : ViewModel() {

    private val _isProgressUpdating = MutableLiveData<Boolean>()
    private val _binder = MutableLiveData<MyService.MyBinder>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Timber.d("onServiceConnected: connected to service")
            val binder = iBinder as MyService.MyBinder
            _binder.postValue(binder)
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            _binder.postValue(null)
        }
    }

    fun getIsProgressUpdating(): LiveData<Boolean> = _isProgressUpdating

    fun getBinder(): LiveData<MyService.MyBinder> = _binder

    fun getServiceConnection(): ServiceConnection = serviceConnection

    fun setUpdating(isUpdating: Boolean) {
        _isProgressUpdating.postValue(isUpdating)
    }
}

