package com.goldwidget

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
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

    // Swissquote public spot feed — same source as cTrader liquidity providers
    private const val URL =
        "https://forex-data-feed.swissquote.com/public-quotes/bboquotes/instrument/XAU/USD"

    fun fetchGoldData(ctx: android.content.Context): GoldData? {
        return try {
            val request = Request.Builder()
                .url(URL)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13)")
                .addHeader("Accept", "application/json")
                .build()

            val body = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                response.body?.string()
            } ?: return null

            // Find the "prime" spread profile (tightest institutional spread)
            val arr = JSONArray(body)
            var bid = 0.0
            var ask = 0.0

            outer@ for (i in 0 until arr.length()) {
                val entry = arr.getJSONObject(i)
                val profiles = entry.getJSONArray("spreadProfilePrices")
                for (j in 0 until profiles.length()) {
                    val p = profiles.getJSONObject(j)
                    if (p.getString("spreadProfile") == "prime") {
                        bid = p.getDouble("bid")
                        ask = p.getDouble("ask")
                        break@outer
                    }
                }
            }
            if (bid == 0.0) return null

            val price = (bid + ask) / 2.0

            // Update tracked day stats in SharedPreferences
            val stats = PriceHistory.update(ctx, price)
            val changePct = if (stats.prevClose != 0.0)
                ((price - stats.prevClose) / stats.prevClose) * 100.0 else 0.0

            GoldData(
                price = price,
                bid = bid,
                ask = ask,
                dayHigh = stats.high,
                dayLow = stats.low,
                open = stats.open,
                previousClose = stats.prevClose,
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
