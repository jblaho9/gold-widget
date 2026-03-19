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

    // GC=F (Gold Futures) is reliably available; XAUUSD=X often returns 404
    private val URLS = listOf(
        "https://query2.finance.yahoo.com/v8/finance/chart/GC=F?interval=1d&range=5d",
        "https://query1.finance.yahoo.com/v8/finance/chart/GC=F?interval=1d&range=5d"
    )

    fun fetchGoldData(): GoldData? {
        for (url in URLS) {
            try {
                val data = fetchFromUrl(url)
                if (data != null) return data
            } catch (_: Exception) {}
        }
        return null
    }

    private fun fetchFromUrl(url: String): GoldData? {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .addHeader("Accept", "application/json, */*")
            .addHeader("Referer", "https://finance.yahoo.com/")
            .build()

        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            response.body?.string()
        } ?: return null

        val chart = JSONObject(body).getJSONObject("chart")
        val resultArr = chart.optJSONArray("result") ?: return null
        if (resultArr.length() == 0) return null
        val meta = resultArr.getJSONObject(0).getJSONObject("meta")

        val price = meta.getDouble("regularMarketPrice")
        val prevClose = meta.optDouble("regularMarketPreviousClose", meta.optDouble("previousClose", price))
        val high = meta.optDouble("regularMarketDayHigh", price)
        val low = meta.optDouble("regularMarketDayLow", price)
        val open = meta.optDouble("regularMarketOpen", prevClose)
        val changePct = if (prevClose != 0.0) ((price - prevClose) / prevClose) * 100.0 else 0.0

        return GoldData(
            price = price,
            dayHigh = high,
            dayLow = low,
            open = open,
            previousClose = prevClose,
            changePercent = changePct,
            timestamp = System.currentTimeMillis()
        )
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
