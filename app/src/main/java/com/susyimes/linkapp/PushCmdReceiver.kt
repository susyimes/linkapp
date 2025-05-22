package com.susyimes.linkapp



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import cn.jpush.android.api.JPushInterface
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.json.JSONObject


class PushCmdReceiver : BroadcastReceiver() {

    private val json = Json { ignoreUnknownKeys = true }   // 解析时忽略未知字段

    override fun onReceive(ctx: Context, intent: Intent) {
        intent.extras?.let { receivingNotification(ctx, it) }
        if (JPushInterface.ACTION_MESSAGE_RECEIVED == intent.action) {

            // ① 取自定义消息正文
            val raw = intent.getStringExtra(JPushInterface.EXTRA_MESSAGE) ?: return
            Log.e("jpushmessage", "自定义消息内容 = $raw")

            // ② 反序列化成 RemoteCmd（@Serializable）
            // ---------- ① JSON → RemoteCmd ----------
            val obj = try { JSONObject(raw) } catch (e: JSONException) {
                Log.e("TAG", "JSON 解析失败", e); return
            }

            val cmd = RemoteCmd(
                type = obj.optString("type", "").uppercase(),
                pkg  = obj.optString("pkg", null),
                x    = obj.optDouble("x", 0.0).toFloat(),
                y    = obj.optDouble("y", 0.0).toFloat()
            )


            // ③ 投递给无障碍服务
//            val svcIntent = Intent(ctx, CommandAccessibilityService::class.java).apply {
//                action = "CMD"
//                putExtra("payload", cmd)
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                ctx.startForegroundService(svcIntent)      // API 26+ 必须前台
//            } else {
//                ctx.startService(svcIntent)
//            }
            // ★ 直接携带 JSON 字符串；无 Parcelable 限制
            ctx.sendBroadcast(
                Intent("com.susyimes.linkapp.REMOTE_CMD")
                    .putExtra("payload", cmd)
            )
            Log.d("PUSH_RX", "sendBroadcast ok")
        }
    }
}

private fun receivingNotification(context: Context, bundle: Bundle) {
    val title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE)
    Log.d("TAG", " title : $title")
    val message = bundle.getString(JPushInterface.EXTRA_ALERT)
    Log.d("TAG", "message : $message")
    val extras = bundle.getString(JPushInterface.EXTRA_EXTRA)
    Log.d("TAG", "extras : $extras")
}