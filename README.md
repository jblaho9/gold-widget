# Gold Widget

An Android home screen widget that shows live XAUUSD (gold) prices.

## Widgets

| Widget | Size | Shows |
|--------|------|-------|
| **Gold Price** | 2×2 | Current price · Daily change % · Timestamp |
| **Gold Price (Detailed)** | 4×2 | Price · Change % · Day High · Day Low · Open · Prev Close |

Both widgets auto-refresh every 15 minutes when connected to the internet.

## Screenshots

> Dark gold theme — open `preview.html` in a browser to see the design.

## Install (pre-built APK)

1. Download [`gold-price-widget-debug.apk`](app/build/outputs/apk/debug/gold-price-widget-debug.apk)
2. On your Android phone go to **Settings → Security → Install unknown apps** and allow your browser/file manager
3. Open the APK and install
4. Long-press your home screen → **Widgets** → search **Gold** → drag a widget onto the screen

Requires Android 8.0 (API 26) or higher.

## Data source

Live prices are fetched from the Yahoo Finance API (no API key required).
`XAUUSD=X` — spot price in USD per troy ounce, updated every 15 minutes.

## Build from source

**Requirements:** Java 17, Android SDK (API 34)

```bash
git clone https://github.com/jblaho9/gold-widget.git
cd gold-widget

export ANDROID_HOME=~/android-sdk   # path to your Android SDK

./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/gold-price-widget-debug.apk
```

## Project structure

```
app/src/main/
├── java/com/goldwidget/
│   ├── SimpleGoldWidget.kt       # 2×2 widget provider
│   ├── DetailedGoldWidget.kt     # 4×2 widget provider
│   ├── WidgetUpdateWorker.kt     # WorkManager background fetch & UI update
│   ├── GoldApiService.kt         # Yahoo Finance API + formatting helpers
│   └── GoldData.kt               # Data model
└── res/
    ├── layout/widget_simple.xml
    ├── layout/widget_detailed.xml
    └── xml/widget_simple_info.xml / widget_detailed_info.xml
```
