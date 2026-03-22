package com.goldwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class DetailedGoldWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(ctx.packageName, R.layout.widget_detailed)
            views.setTextViewText(R.id.tv_price, "Loading…")
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent(ctx))
            mgr.updateAppWidget(id, views)
        }
        SimpleGoldWidget.triggerRefresh(ctx)
        SimpleGoldWidget.schedulePeriodicUpdates(ctx)
    }

    override fun onEnabled(ctx: Context) {
        SimpleGoldWidget.schedulePeriodicUpdates(ctx)
        SimpleGoldWidget.triggerRefresh(ctx)
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        if (intent.action == ACTION_REFRESH) {
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

    companion object {
        const val ACTION_REFRESH = "com.goldwidget.ACTION_REFRESH_DETAILED"

        fun refreshPendingIntent(ctx: Context): PendingIntent {
            val intent = Intent(ctx, DetailedGoldWidget::class.java).apply {
                action = ACTION_REFRESH
            }
            return PendingIntent.getBroadcast(
                ctx, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
