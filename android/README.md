# Stream Warrior — Android App

Native Android version with **overlay mode**: a floating mod menu (soundboard +
game cards) that runs ON TOP of any streaming app — MeetMe included — while your
camera stays on your face.

## What it does
- **Floating bubble** — drag it anywhere on your screen
- **Tap to open the mod menu panel:**
  - Soundboard: 14 synthesized sounds (airhorn, drumroll, applause, sad trombone, crickets…) played through your mic to viewers
  - Game cards: Jokes, Fun Facts, Pickup Lines, Would You Rather, Weird Laws (50 states), Tarot (classic + love decks, upright/reversed) — read them aloud on stream
- **Never steals focus** — your streaming app keeps working while the menu is open

## Build it (one-time, ~10 minutes)
1. Install [Android Studio](https://developer.android.com/studio) (free)
2. Open Android Studio → **Open** → select the `android/` folder
3. Let it sync (it downloads what it needs automatically)
4. Plug in your phone with USB debugging enabled (Settings → Developer options), or use the emulator
5. Press **Run ▶** — the app installs on your phone

## First launch
1. Tap **Grant overlay permission** → allow "Display over other apps"
2. (Android 13+) Tap **Allow notifications**
3. Tap **Start floating mod menu**
4. Open MeetMe → go live → tap the floating bubble

## Notes
- Sounds are synthesized at runtime (no audio files needed)
- The overlay cannot inject visuals into another app's stream (OS limitation) —
  for on-screen visuals use the web version on screen-share platforms
  (TikTok, BIGO, Twitch) or the two-device trick
