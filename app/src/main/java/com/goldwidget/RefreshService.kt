package com.goldwidget

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Lightweight service for on-demand widget refresh triggered by the refresh button.
 * Bypasses WorkManager scheduling overhead for snappier manual refreshes.
 */
class RefreshService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            val data = GoldApiService.fetchGoldData(applicationContext)
            if (data != null) {
                WidgetUpdateWorker.updateAllWidgets(applicationContext, data)
            }
            stopSelf(startId)
        }.start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
