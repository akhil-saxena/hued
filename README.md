# hued

**your life in color**

hued is a privacy-first Android app that extracts dominant color palettes from your photo gallery, organized by time period — week, month, season, or year — and presents them as beautiful, shareable visual summaries.

Every photo you take carries color. Over weeks and months, those colors tell a story you've never seen: muted greys through a quiet winter, vibrant greens from a trip to the coast, warm amber from evenings spent indoors. hued reveals that story.

All processing happens on-device. Your photos never leave your phone.

## Features

- **Instant palette on launch** — current month's palette displayed immediately, zero taps
- **Color naming** — 18,000+ evocative color names ("Distant Horizon," "Burnt Sienna," "Forest Canopy")
- **Poetic descriptions** — each palette gets a one-line description matched to its properties
- **Weekly color ritual** — Monday notification: "Your week in color is ready"
- **Delight moments** — monochrome month acknowledgments, harmony detection, streaks, Year in Color
- **Share cards** — beautiful, social-native images generated in one tap
- **Home screen widget** — current weekly palette always visible
- **Time views** — switch between week, month, season, and year
- **Privacy first** — no network permission, no analytics, no cloud processing

## Tech Stack

- Kotlin + Jetpack Compose
- Material 3 (heavily customized — the palette IS the theme)
- Room (local palette cache)
- Hilt (dependency injection)
- WorkManager (weekly processing + notifications)
- Android Palette API (on-device color extraction)
- Glance (home screen widget)

## Design

- **Font:** Outfit (Light, Regular, Bold)
- **Canvas:** Neutral off-white `#F8F7F5`, dynamically tinted by palette
- **Philosophy:** design-first app, not an app with design. The palette strip is the interface.

## Building

Open in Android Studio, let Gradle sync, run on device or emulator.

```
./gradlew assembleDebug
```

Requires Android SDK with API 35 target. Min SDK 23.

## License

All rights reserved.
