package com.susyimes.linkapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 家长端发来的远程指令
 *
 * @param type  指令类型："HOME" | "TAP" | "OPEN" | "LOCK"
 * @param pkg   打开 App 时目标包名，仅 type == "OPEN" 时使用
 * @param x,y   点击坐标（像素），仅 type == "TAP" 时使用
 */
             // 供 kotlinx-serialization -> JSON
@Parcelize                  // 供 Intent / Bundle 传输
data class RemoteCmd(
    val type: String,
    val pkg: String? = null,
    val x: Float = 0f,
    val y: Float = 0f
) : Parcelable
