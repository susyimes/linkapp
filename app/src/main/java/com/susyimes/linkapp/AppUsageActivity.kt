package com.susyimes.linkapp

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.TimeUnit

class AppUsageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    UsageScreen()
                }
            }
        }
    }
}

// 权限检查
fun hasUsageAccessPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

// 获取使用数据
fun getUsageStatsList(context: Context): List<UsageStats> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - TimeUnit.DAYS.toMillis(1)

    return usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        endTime
    ).filter { it.totalTimeInForeground > 0 }
        .sortedByDescending { it.totalTimeInForeground }
}

// Composable 页面入口
@Composable
fun UsageScreen() {
    val context = LocalContext.current
    val usageStats = remember { mutableStateListOf<UsageStats>() }

    LaunchedEffect(Unit) {
        if (!hasUsageAccessPermission(context)) {
            context.startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        } else {
            usageStats.clear()
            usageStats.addAll(getUsageStatsList(context))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("应用使用统计", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(usageStats) { stat ->
                AppUsageItem(stat, context)
            }
        }
    }
}

// 每项应用的显示组件
@Composable
fun AppUsageItem(stat: UsageStats, context: Context) {
    val pm = context.packageManager
    val appInfo = remember(stat.packageName) {
        try {
            pm.getApplicationInfo(stat.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    val appName = appInfo?.let { pm.getApplicationLabel(it).toString() } ?: stat.packageName
    val icon = appInfo?.let {
        try {
            pm.getApplicationIcon(it)
        } catch (e: Exception) {
            null
        }
    }

    val timeInSeconds = stat.totalTimeInForeground / 1000

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (icon != null) {
            AndroidView(
                factory = {
                    ImageView(it).apply {
                        setImageDrawable(icon)
                        layoutParams = ViewGroup.LayoutParams(96, 96)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 12.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(appName, fontWeight = FontWeight.Bold)
            Text("使用时间：${timeInSeconds / 60} 分钟")
        }
    }

    Divider(modifier = Modifier.padding(top = 4.dp))
}

