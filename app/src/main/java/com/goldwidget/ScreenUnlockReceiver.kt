package com.goldwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val prefs = ctx.getSharedPreferences("gold_widget", Context.MODE_PRIVATE)
            val lastFetch = prefs.getLong("last_fetch_ts", 0L)
            if (System.currentTimeMillis() - lastFetch >= 60_000L) {
                val result = goAsync()
                Thread {
                    try {
                        val data = GoldApiService.fetchGoldData(ctx)
                        if (data != null) WidgetUpdateWorker.updateAllWidgets(ctx, data)
                    } finally {
                        result.finish()
                    }
                }.start()
            }
        }
    }
}
