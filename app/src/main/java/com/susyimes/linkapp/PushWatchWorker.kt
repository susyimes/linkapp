package com.susyimes.linkapp

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import cn.jpush.android.api.JPushInterface
import java.util.concurrent.TimeUnit

class PushWatchWorker(ctx: Context, params: WorkerParameters)
    : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        // 1. 极光推送掉线自恢复
        if (cn.jpush.android.api.JPushInterface.isPushStopped(applicationContext)) {
            cn.jpush.android.api.JPushInterface.resumePush(applicationContext)
        }

        // 2. 检测无障碍
        val enabled = Settings.Secure.getString(
            applicationContext.contentResolver,                    // ← 关键
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )?.contains(CommandAccessibilityService::class.java.name) == true

        if (!enabled) {
            showEnableAccessibilityNotification()
        }
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showEnableAccessibilityNotification() {
        val nm = NotificationManagerCompat.from(applicationContext)

        // ---------- ① API < 33 直接发 ------------
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            nm.notify(1002, buildNotif(applicationContext))      // buildNotif() 见下
            return
        }

        // ---------- ② 已授权也直接发 ----------
        val granted = ActivityCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            nm.notify(1002, buildNotif(applicationContext))
            return
        }

        // ---------- ③ 未授权 → 引导到申请 Activity ----------
        val intent = Intent(applicationContext, NotifPermissionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        applicationContext.startActivity(intent)     // 交给前台 UI 申请权限
    }
}
/** 把通知构建单独抽出来，方便复用 */
private fun buildNotif(ctx: Context): Notification =
    NotificationCompat.Builder(ctx, "watchdog")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("无障碍权限已关闭")
        .setContentText("点击重新开启，以便家长控制正常工作")
        .setAutoCancel(true)
        .setContentIntent(
            PendingIntent.getActivity(
                ctx, 0,
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()
