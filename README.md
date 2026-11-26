# Stream Warrior (Android)

Native Kotlin & Jetpack Compose rewrite of **Stream Warrior** — an all-in-one interactive livestream companion, game deck, audio soundboard, and Warrior Family suite.

## Modules & Features

- **Wheel of Chaos**: Animated 10-slice custom `Canvas` wheel with drumroll/ta-da SFX and one-tap module launch.
- **Tarot & Mystica**: Classic Mystica & Love Oracle decks, 9 spreads, Draw & Cut-the-Deck modes, full reading generator, and Mystica chat.
- **20 Questions**: 40 built-in secret entities, attribute hint chips, Q&A log, reveal option, and optional AI host.
- **Trivia Showdown**: 48 built-in questions across 9 categories with score/streak tracking and optional AI question generator.
- **Soundboard**: 14 real-time synthesized SFX (`AudioTrack` PCM) + up to 12 custom audio clips via Android's document picker.
- **Mad Libs Live**: 8 multi-slot stream stories with chip tracker and highlighted story reveal.
- **Jokes, Pickup Lines, Fun Facts & Would You Rather**: Interactive card decks with SFX and audience split choices.
- **Weird Laws & Weird Law or Fake?**: 50-state weird law browser plus a 54-card Real-or-Fake quiz with persisted score & streak.
- **Warrior Prophet**: Daily seeded blessing & prayer, 3 spiritual message decks, Angel Numbers lookup, and Ask the Prophet.
- **Warrior's Den Studio**: Live overlay & scrolling ticker preview, alert box simulator (`Follow`, `Sub`, `Tip`, `Raid`), and tip-link manager.
- **Warrior Coaches & Forge**: 15 built-in coaches (including Survival AI with BIFF reply drafter and volume-only Calm Meter, Ads Manager with 15s script builder, Bible Flamekeeper, Wealth Builder, Academy, Family Center, and TFT Coach) plus a custom tool builder.
- **Clean Scene Mode**: Toggle `"Hide"` in the bottom/side bar to clear controls for filming or screen-mirroring, and tap `"Show Controls"` to restore.

## Architecture & Tech Stack

- **UI**: Kotlin, Jetpack Compose, Material Design 3, edge-to-edge layout, adaptive navigation (`NavigationBar` / `NavigationRail`), local typography (`Cinzel`, `Space Grotesk`, `Plus Jakarta Sans`).
- **Navigation**: Type-safe Navigation Compose (`@Serializable` `AppRoute`).
- **State & Storage**: `ViewModel` + `StateFlow`, `DataStore Preferences`, app-private audio storage, optional BYO-key OpenAI-compatible HTTP client.
- **Build**: Gradle Kotlin DSL, AGP 9.1.1, Kotlin 2.2.21, Java 21, `compileSdk` / `targetSdk` 36, `minSdk` 26.
