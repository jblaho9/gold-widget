package com.goldwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class DetailedGoldWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(ctx.packageName, R.layout.widget_detailed)
            views.setTextViewText(R.id.tv_price, "Loading…")
            mgr.updateAppWidget(id, views)
        }
        // Reuse the same periodic worker as the simple widget
        SimpleGoldWidget.scheduleUpdates(ctx)
    }

    override fun onEnabled(ctx: Context) = SimpleGoldWidget.scheduleUpdates(ctx)
}
