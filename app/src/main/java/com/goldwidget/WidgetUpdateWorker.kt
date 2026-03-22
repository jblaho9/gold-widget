package com.goldwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.work.Worker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val data = GoldApiService.fetchGoldData(applicationContext) ?: return Result.retry()
        updateAllWidgets(applicationContext, data)
        return Result.success()
    }

    companion object {

        private const val CACHE_PREFS = "gold_widget_cache"

        fun saveCache(ctx: Context, data: GoldData) {
            ctx.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE).edit()
                .putFloat("price",       data.price.toFloat())
                .putFloat("change_pct",  data.changePercent.toFloat())
                .putFloat("high",        data.dayHigh.toFloat())
                .putFloat("low",         data.dayLow.toFloat())
                .putFloat("open",        data.open.toFloat())
                .putFloat("prev",        data.previousClose.toFloat())
                .putBoolean("market_closed", data.marketClosed)
                .putLong("timestamp",    data.timestamp)
                .apply()
        }

        fun loadCache(ctx: Context): GoldData? {
            val p = ctx.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
            val price = p.getFloat("price", 0f).toDouble()
            if (price == 0.0) return null
            return GoldData(
                price          = price,
                bid            = price,
                ask            = price,
                dayHigh        = p.getFloat("high",  price.toFloat()).toDouble(),
                dayLow         = p.getFloat("low",   price.toFloat()).toDouble(),
                open           = p.getFloat("open",  price.toFloat()).toDouble(),
                previousClose  = p.getFloat("prev",  price.toFloat()).toDouble(),
                changePercent  = p.getFloat("change_pct", 0f).toDouble(),
                timestamp      = p.getLong("timestamp", System.currentTimeMillis()),
                marketClosed   = p.getBoolean("market_closed", false)
            )
        }

        fun updateAllWidgets(ctx: Context, data: GoldData) {
            saveCache(ctx, data)
            val mgr = AppWidgetManager.getInstance(ctx)

            val simpleIds = mgr.getAppWidgetIds(ComponentName(ctx, SimpleGoldWidget::class.java))
            for (id in simpleIds) mgr.updateAppWidget(id, buildSimpleViews(ctx, data))

            val detailedIds = mgr.getAppWidgetIds(ComponentName(ctx, DetailedGoldWidget::class.java))
            for (id in detailedIds) mgr.updateAppWidget(id, buildDetailedViews(ctx, data))
        }

        private fun changeColor(ctx: Context, pct: Double): Int =
            if (pct >= 0) ctx.getColor(R.color.price_up) else ctx.getColor(R.color.price_down)

        private fun applyChangePill(ctx: Context, views: RemoteViews, data: GoldData) {
            val pillRes = if (data.changePercent >= 0) R.drawable.pill_up else R.drawable.pill_down
            views.setInt(R.id.tv_change, "setBackgroundResource", pillRes)
            views.setTextViewText(R.id.tv_change, GoldApiService.formatChangePct(data.changePercent))
            views.setTextColor(R.id.tv_change, changeColor(ctx, data.changePercent))
            views.setViewVisibility(
                R.id.iv_market_closed,
                if (data.marketClosed) View.VISIBLE else View.GONE
            )
        }

        fun buildSimpleViews(ctx: Context, data: GoldData): RemoteViews {
            val views = RemoteViews(ctx.packageName, R.layout.widget_simple)
            views.setTextViewText(R.id.tv_price, GoldApiService.formatPrice(data.price))
            applyChangePill(ctx, views, data)
            views.setTextViewText(R.id.tv_updated, GoldApiService.formatTime(data.timestamp))
            views.setOnClickPendingIntent(R.id.btn_refresh, SimpleGoldWidget.refreshPendingIntent(ctx))
            return views
        }

        fun buildDetailedViews(ctx: Context, data: GoldData): RemoteViews {
            val views = RemoteViews(ctx.packageName, R.layout.widget_detailed)
            views.setTextViewText(R.id.tv_price, GoldApiService.formatPrice(data.price))
            applyChangePill(ctx, views, data)
            views.setTextViewText(R.id.tv_high, GoldApiService.formatShortPrice(data.dayHigh))
            views.setTextViewText(R.id.tv_low, GoldApiService.formatShortPrice(data.dayLow))
            views.setTextViewText(R.id.tv_open, GoldApiService.formatShortPrice(data.open))
            views.setTextViewText(R.id.tv_prev_close, GoldApiService.formatShortPrice(data.previousClose))
            views.setTextViewText(R.id.tv_updated, "Updated " + GoldApiService.formatTime(data.timestamp))
            views.setOnClickPendingIntent(R.id.btn_refresh, DetailedGoldWidget.refreshPendingIntent(ctx))
            return views
        }
    }
}
