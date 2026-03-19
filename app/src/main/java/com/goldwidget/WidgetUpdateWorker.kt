package com.goldwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.Worker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val data = GoldApiService.fetchGoldData() ?: return Result.retry()
        updateSimpleWidgets(data)
        updateDetailedWidgets(data)
        return Result.success()
    }

    private fun updateSimpleWidgets(data: GoldData) {
        val mgr = AppWidgetManager.getInstance(applicationContext)
        val ids = mgr.getAppWidgetIds(ComponentName(applicationContext, SimpleGoldWidget::class.java))
        for (id in ids) {
            val views = buildSimpleViews(data)
            mgr.updateAppWidget(id, views)
        }
    }

    private fun updateDetailedWidgets(data: GoldData) {
        val mgr = AppWidgetManager.getInstance(applicationContext)
        val ids = mgr.getAppWidgetIds(ComponentName(applicationContext, DetailedGoldWidget::class.java))
        for (id in ids) {
            val views = buildDetailedViews(data)
            mgr.updateAppWidget(id, views)
        }
    }

    private fun changeColor(pct: Double): Int {
        return if (pct >= 0)
            applicationContext.getColor(R.color.price_up)
        else
            applicationContext.getColor(R.color.price_down)
    }

    fun buildSimpleViews(data: GoldData): RemoteViews {
        val views = RemoteViews(applicationContext.packageName, R.layout.widget_simple)
        views.setTextViewText(R.id.tv_price, GoldApiService.formatPrice(data.price))
        views.setTextViewText(R.id.tv_change, GoldApiService.formatChangePct(data.changePercent))
        views.setTextColor(R.id.tv_change, changeColor(data.changePercent))
        views.setTextViewText(R.id.tv_updated, GoldApiService.formatTime(data.timestamp))
        views.setOnClickPendingIntent(R.id.btn_refresh, SimpleGoldWidget.refreshPendingIntent(applicationContext))
        return views
    }

    fun buildDetailedViews(data: GoldData): RemoteViews {
        val views = RemoteViews(applicationContext.packageName, R.layout.widget_detailed)
        views.setTextViewText(R.id.tv_price, GoldApiService.formatPrice(data.price))
        views.setTextViewText(R.id.tv_change, GoldApiService.formatChangePct(data.changePercent))
        views.setTextColor(R.id.tv_change, changeColor(data.changePercent))
        views.setTextViewText(R.id.tv_high, GoldApiService.formatShortPrice(data.dayHigh))
        views.setTextViewText(R.id.tv_low, GoldApiService.formatShortPrice(data.dayLow))
        views.setTextViewText(R.id.tv_open, GoldApiService.formatShortPrice(data.open))
        views.setTextViewText(R.id.tv_prev_close, GoldApiService.formatShortPrice(data.previousClose))
        views.setTextViewText(R.id.tv_updated, "Updated " + GoldApiService.formatTime(data.timestamp))
        views.setOnClickPendingIntent(R.id.btn_refresh, DetailedGoldWidget.refreshPendingIntent(applicationContext))
        return views
    }
}
