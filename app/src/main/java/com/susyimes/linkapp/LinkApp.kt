package com.susyimes.linkapp

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cn.jiguang.android.BuildConfig
import cn.jpush.android.api.JPushInterface
import java.util.concurrent.TimeUnit

class LinkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application.onCreate()
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)


    }
}