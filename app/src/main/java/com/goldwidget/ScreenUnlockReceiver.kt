package com.goldwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            SimpleGoldWidget.triggerRefresh(ctx)
        }
    }
}
