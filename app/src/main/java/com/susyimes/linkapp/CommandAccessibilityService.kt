package com.susyimes.linkapp


import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.serialization.json.Json

/**
 * 负责执行家长端下发的 RemoteCmd
 */
@RequiresApi(Build.VERSION_CODES.N)      // dispatchGesture 需要 API 24+
class CommandAccessibilityService : AccessibilityService() {

    private val ACTION_REMOTE_CMD = "com.susyimes.linkapp.REMOTE_CMD"

    private val cmdReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            i.getParcelableExtra<RemoteCmd>("payload")?.let {
                Log.d("ACC_SVC", "收到指令: $it")
                exec(it)
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onServiceConnected() {
        registerReceiver(cmdReceiver, IntentFilter(ACTION_REMOTE_CMD))
        Log.d("ACC_SVC", "无障碍已连接并监听广播")
    }

    override fun onDestroy() { unregisterReceiver(cmdReceiver) }


//    /** 确保服务具备手势能力，可按需调 flags */
//    override fun onServiceConnected() {
//        serviceInfo = serviceInfo.apply {
//            // 让系统把本服务标记为“可执行手势”
//            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
//        }
//    }

    /** 通过 startService(Intent("CMD")) 的方式接收指令 */
    override fun onStartCommand(i: Intent?, flags: Int, id: Int): Int {
        Log.e("comnaaa",i?.action.toString())
        if (i?.action == "CMD") {
            i.getParcelableExtra<RemoteCmd>("payload")?.let(::exec)
        }
        return START_STICKY        // 进程被杀后自动重启
    }

    /** 核心执行分发 */
    private fun exec(c: RemoteCmd) = when (c.type) {
        "HOME" -> performGlobalAction(GLOBAL_ACTION_HOME)

        "LOCK" -> performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)

        "OPEN" -> c.pkg?.let { pkg ->
            packageManager.getLaunchIntentForPackage(pkg)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }

        "TAP" -> runTap(c.x, c.y)

        else -> Unit
    }

    /** 注入单点点击手势 */
    private fun runTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 60)) // 60 ms click
            .build()
        Toast.makeText(applicationContext,"点击了"+x+"//"+y,Toast.LENGTH_LONG).show()
        dispatchGesture(gesture, null, null)
    }

    /** 这里不需要监听事件，留空 */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() {}
}
