package com.goldwidget

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GoldApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Uses Yahoo Finance unofficial API — no key required
    private const val URL =
        "https://query1.finance.yahoo.com/v8/finance/chart/XAUUSD=X?interval=1d&range=1d"

    fun fetchGoldData(): GoldData? {
        return try {
            val request = Request.Builder()
                .url(URL)
                .addHeader("User-Agent", "Mozilla/5.0 (Android)")
                .addHeader("Accept", "application/json")
                .build()

            val body = client.newCall(request).execute().use { it.body?.string() }
                ?: return null

            val meta = JSONObject(body)
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONObject("meta")

            val price = meta.getDouble("regularMarketPrice")
            val prevClose = meta.optDouble("previousClose", price)
            val high = meta.optDouble("regularMarketDayHigh", price)
            val low = meta.optDouble("regularMarketDayLow", price)
            val open = meta.optDouble("regularMarketOpen", price)
            val changePct = if (prevClose != 0.0) ((price - prevClose) / prevClose) * 100.0 else 0.0

            GoldData(
                price = price,
                dayHigh = high,
                dayLow = low,
                open = open,
                previousClose = prevClose,
                changePercent = changePct,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun formatPrice(price: Double): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        fmt.minimumFractionDigits = 2
        fmt.maximumFractionDigits = 2
        return fmt.format(price)
    }

    fun formatShortPrice(price: Double): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        fmt.minimumFractionDigits = 0
        fmt.maximumFractionDigits = 0
        return fmt.format(price)
    }

    fun formatChangePct(pct: Double): String {
        val sign = if (pct >= 0) "+" else ""
        return "$sign%.2f%%".format(pct)
    }

    fun formatTime(ts: Long): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
}
