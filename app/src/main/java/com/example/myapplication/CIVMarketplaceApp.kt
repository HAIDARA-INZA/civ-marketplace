package com.example.myapplication

import android.app.Application
import com.example.myapplication.util.NotificationUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CIVMarketplaceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannels(this)
    }
}
