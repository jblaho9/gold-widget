package com.goldwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import androidx.work.*
import java.util.concurrent.TimeUnit

class SimpleGoldWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        // Show loading placeholder immediately
        for (id in ids) {
            val views = RemoteViews(ctx.packageName, R.layout.widget_simple)
            views.setTextViewText(R.id.tv_price, "Loading…")
            views.setTextViewText(R.id.tv_change, "")
            views.setTextViewText(R.id.tv_updated, "")
            mgr.updateAppWidget(id, views)
        }
        scheduleUpdates(ctx)
    }

    override fun onEnabled(ctx: Context) = scheduleUpdates(ctx)

    override fun onDisabled(ctx: Context) {
        // Only cancel if the detailed widget is also gone
        WorkManager.getInstance(ctx).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "gold_widget_refresh"

        fun scheduleUpdates(ctx: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Trigger an immediate fetch
            val now = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setConstraints(constraints)
                .build()

            // Then refresh every 15 minutes (WorkManager minimum)
            val periodic = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(ctx).apply {
                enqueue(now)
                enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, periodic)
            }
        }
    }
}
