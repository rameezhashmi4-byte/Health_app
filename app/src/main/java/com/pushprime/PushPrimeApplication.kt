package com.pushprime

import android.app.Application
import com.pushprime.data.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RamboostApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncScheduler.schedule(this)
    }
}
