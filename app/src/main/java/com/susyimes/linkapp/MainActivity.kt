package com.susyimes.linkapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cn.jpush.android.api.JPushInterface
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
       Log.e("jpush",JPushInterface.getRegistrationID(this))

        // 注册：15 min 周期（系统最短） :contentReference[oaicite:3]{index=3}
        val req = PeriodicWorkRequestBuilder<PushWatchWorker>(15, TimeUnit.MINUTES)
            //.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "push_watchdog", ExistingPeriodicWorkPolicy.UPDATE, req)

        ensureAccessibility(this)
    }


    fun ensureAccessibility(ctx: Context): Boolean {
        val cn = ComponentName(ctx, CommandAccessibilityService::class.java)
        val enabled = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )?.contains(cn.flattenToString()) == true

        if (!enabled) {
            // 引导到系统无障碍设置
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
            Toast.makeText(ctx, "请在列表中找到“孩子守护服务”并开启", Toast.LENGTH_LONG).show()
        }
        return enabled
    }
}