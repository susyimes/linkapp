package com.susyimes.linkapp

import android.content.Context
import android.util.Log
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.service.JPushMessageReceiver

class LinkReceiver: JPushMessageReceiver() {
    override fun onMessage(p0: Context?, p1: CustomMessage?) {
        super.onMessage(p0, p1)
        Log.e("jpush",p1.toString())
    }
}