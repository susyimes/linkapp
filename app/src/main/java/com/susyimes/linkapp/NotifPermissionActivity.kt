package com.susyimes.linkapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class NotifPermissionActivity : AppCompatActivity() {

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        // 无论授权与否，退出即可
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 若系统 < 33 直接结束
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            finish(); return
        }

        // 已授权也结束
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            finish(); return
        }

        // 请求权限
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
