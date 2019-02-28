package com.ydly.rankingalarm2.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.AnkoLogger

abstract class BaseReceiver: BroadcastReceiver(), AnkoLogger {

    lateinit var ctx: Context

    abstract override fun onReceive(context: Context?, intent: Intent?)

}