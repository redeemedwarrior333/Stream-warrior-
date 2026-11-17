# Warrior Prophet ⚔️🙏

Faith-based encouragement site: **Daily Blessings**, **spiritual Message Cards** (God's Promises, Words of Jesus, Angel Watch), **Angel Numbers**, and **Ask the Prophet**. Part of the Warrior family alongside [Stream Warrior](https://redeemedwarrior333.github.io/Stream-warrior-/).

Live after merge: `https://redeemedwarrior333.github.io/Stream-warrior-/warrior-prophet/`

## Pages

| Page | What it does |
|---|---|
| `index.html` | Landing page + today's word teaser |
| `daily.html` | Daily Blessing — seeded per day (same blessing for everyone that day), verse + prayer, share button |
| `cards.html` | Message decks — God's Promises & Words of Jesus (free), Angel Watch (premium). Draw 1 or 3 |
| `angels.html` | Angel number decoder — 16 popular numbers + any number via digit meanings, each with a scripture |
| `ask.html` | Ask the Prophet — built-in scripture library by topic (free) or AI-written answers (optional key) |
| `premium.html` | Subscription offer + unlock codes |

## Monetization setup (10 minutes)

**Option A — Stripe Payment Links (recommended):**
1. Create a free Stripe account → **Payment Links** → New.
2. Make two links: **$4.99/month** subscription and **$29.99** one-time.
3. In `premium.html`, replace `REPLACE_WITH_YOUR_MONTHLY_LINK` and `REPLACE_WITH_YOUR_LIFETIME_LINK` with your real links.

**Option B — Manual (CashApp/Venmo):** take payment, then send the buyer an unlock code.

**Unlock codes** live in `js/premium.js` (`CODES` array). Defaults: `MW-FOUNDER`, `PROPHET-777`, `ANGEL-1111`. Edit that array to add your own, then push. The code unlocks Premium in that person's browser (stored in their device's localStorage).

**Optional AI mode:** users can paste their own Mistral API key (stored on-device, key `wp_key`). No server, no keys shipped in the repo.

## Listing on There's An AI For That

Submit `https://redeemedwarrior333.github.io/Stream-warrior-/warrior-prophet/ask.html` as the tool URL — suggested name: **"Warrior Prophet — AI Messages of Faith"**. Category: Religion / Spirituality.
