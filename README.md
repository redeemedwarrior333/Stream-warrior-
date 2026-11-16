# ⚡ Stream Aid — Streamer Mod Menu (v0)

A mobile-first web app that gives phone live-streamers a production toolkit.
Open it, share your screen (or full screen) in any live app — MeetMe, TikTok Live,
Instagram, Bigo, Twitch — and run games, tarot readings, sound effects and more
while you stream. No install for viewers. Works on any device with a browser.

## How to use it on stream
1. Open Stream Aid in your phone or desktop browser.
2. Start your live in any app that supports screen sharing.
3. Share your screen (Android can also share just the browser tab — viewers then see only the scene, not the controls).
4. Drive everything from the floating mod bar at the bottom. Tap **Hide** to tuck the bar away; tap the very top edge of the screen to bring it back.

## Modules (v0)
- 🔮 **Tarot** — Mystica (Classic) and Love Oracle decks, 22 Major Arcana, upright/reversed, PAST/PRESENT/FUTURE spread, tap a placed card for full meanings. Optional AI readings when connected.
- ❓ **20 Questions** — Stream Aid holds a secret; chat asks yes/no questions, you type them in and it answers. Reveal button for the endgame.
- 🧠 **Trivia** — 48-question built-in bank across 8 categories, reveal + next flow.
- 🔊 **Soundboard** — 14 synthesized sounds (airhorn, drumroll, applause, sad trombone, crickets…) + up to 12 of your own uploaded sounds, saved on-device.
- 📝 **Mad Libs** — 8 story templates, chip-guided word collection, animated reveal.
- 😂 **Jokes** / 💘 **Pickup Lines** / 💡 **Fun Facts** / ⚔️ **Would You Rather** — endless built-in packs.
- 🎡 **Wheel of Chaos** — spins to a random module for dead-air moments.

## Optional AI
Everything works offline with built-in content. To unlock endless generated
content (AI tarot readings, infinite trivia/jokes/facts), open **⚙️ Settings →
AI Connection** and point it at any OpenAI-compatible API (e.g. Mistral).
Your key is stored only on your device and sent only to the endpoint you choose.

## Run locally
Any static server works:
```bash
python3 -m http.server 8080
# open http://localhost:8080
```

## Roadmap
- v1: AI music mode (owner's catalog), more tarot decks, seasonal decks
- v2: native Android app (overlay control bar outside the browser)
- v3: deep mode inside the companion streaming platform (AI faces, gift reactions, chat-reading AI)

## Notes
- iOS blocks direct audio capture during screen share — soundboard audio plays
  through the speaker and is picked up by the mic. Android can capture it directly.
- This app never modifies another app's stream; it is a scene canvas you share.
