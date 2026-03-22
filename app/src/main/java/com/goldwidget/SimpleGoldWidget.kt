package com.goldwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.*
import java.util.concurrent.TimeUnit

class SimpleGoldWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(ctx.packageName, R.layout.widget_simple)
            views.setTextViewText(R.id.tv_price, "Loading…")
            views.setTextViewText(R.id.tv_change, "")
            views.setTextViewText(R.id.tv_updated, "")
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent(ctx))
            mgr.updateAppWidget(id, views)
        }
        triggerRefresh(ctx)
    }

    override fun onEnabled(ctx: Context) {
        schedulePeriodicUpdates(ctx)
        triggerRefresh(ctx)
    }

    override fun onDisabled(ctx: Context) {
        WorkManager.getInstance(ctx).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "gold_widget_refresh"

        fun refreshPendingIntent(ctx: Context): PendingIntent {
            val intent = Intent(ctx, RefreshService::class.java)
            return PendingIntent.getService(
                ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun triggerRefresh(ctx: Context) {
            val work = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx).enqueue(work)
        }

        fun schedulePeriodicUpdates(ctx: Context) {
            val periodic = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, periodic
            )
        }
    }
}
