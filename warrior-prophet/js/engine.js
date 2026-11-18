/* Warrior Prophet — engine: seeded daily RNG, blessing composer, AI helper */
"use strict";
var WP = (function () {
  function hash(str) {
    var h = 2166136261;
    for (var i = 0; i < str.length; i++) {
      h ^= str.charCodeAt(i);
      h = Math.imul(h, 16777619);
    }
    return h >>> 0;
  }
  function mulberry32(seed) {
    var a = seed;
    return function () {
      a |= 0; a = (a + 0x6D2B79F5) | 0;
      var t = Math.imul(a ^ (a >>> 15), 1 | a);
      t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }
  function rngFor(key) { return mulberry32(hash(key)); }
  function pick(rng, arr) { return arr[Math.floor(rng() * arr.length)]; }

  function todayStr() {
    var d = new Date();
    return d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
  }
  function todayPretty() {
    var d = new Date();
    var mon = ["January","February","March","April","May","June","July","August","September","October","November","December"];
    var days = ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];
    return days[d.getDay()] + ", " + mon[d.getMonth()] + " " + d.getDate() + ", " + d.getFullYear();
  }
  function shuffle(arr, rng) {
    var a = arr.slice();
    for (var i = a.length - 1; i > 0; i--) {
      var j = Math.floor((rng ? rng() : Math.random()) * (i + 1));
      var t = a[i]; a[i] = a[j]; a[j] = t;
    }
    return a;
  }

  var BLESSINGS = [
    { m:"Today is not a day to be strong on your own. Set the weight down; it was never yours to carry alone.", v:"Come to me, all you who are weary and burdened, and I will give you rest.", r:"Matthew 11:28" },
    { m:"What looks like a delay is preparation in disguise. The door is being fitted to your size.", v:"For I know the plans I have for you, plans to give you hope and a future.", r:"Jeremiah 29:11" },
    { m:"You are allowed to rest without guilt. Even the stars keep shining while they sleep behind the clouds.", v:"He makes me lie down in green pastures, He leads me beside quiet waters.", r:"Psalm 23:2" },
    { m:"The thing you are afraid to pray about is the first thing to pray about.", v:"Do not be anxious about anything, but in every situation, present your requests to God.", r:"Philippians 4:6" },
    { m:"Someone is praying for you today, whether you know their name or not. You are not forgotten.", v:"I have called you by name; you are Mine.", r:"Isaiah 43:1" },
    { m:"Small obedience today outweighs big plans for tomorrow. Take the next right step.", v:"Trust in the Lord with all your heart and lean not on your own understanding.", r:"Proverbs 3:5" },
    { m:"Your tears are not wasted; they are seeds. Grief and hope can live in the same house.", v:"Weeping may stay for the night, but rejoicing comes in the morning.", r:"Psalm 30:5" },
    { m:"You do not need to see the whole staircase. The next step is lit, and that is enough.", v:"Your word is a lamp to my feet and a light to my path.", r:"Psalm 119:105" },
    { m:"The love you gave away quietly has been banking interest in heaven. It always comes back.", v:"Give, and it will be given to you; good measure, pressed down, shaken together.", r:"Luke 6:38" },
    { m:"Forgiveness is the key that unlocks your own cell. Use it today, even if it is only a start.", v:"Be kind and compassionate, forgiving each other, just as in Christ God forgave you.", r:"Ephesians 4:32" },
    { m:"You are not behind. You are on a road with no traffic, and heaven walks at your pace.", v:"The Lord will guide you always; He will satisfy your needs.", r:"Isaiah 58:11" },
    { m:"Say the honest prayer. Heaven is not offended by your real voice.", v:"Pour out your heart to Him, for God is our refuge.", r:"Psalm 62:8" },
    { m:"What you feed grows. Feed your faith today with one small yes.", v:"Faith is confidence in what we hope for and assurance about what we do not see.", r:"Hebrews 11:1" },
    { m:"The storm is real, but so is the shelter. Run in; the door was never locked.", v:"God is our refuge and strength, an ever-present help in trouble.", r:"Psalm 46:1" },
    { m:"You were placed in this exact day on purpose. Somebody needs the light you almost hid.", v:"Let your light shine before others, that they may see your good deeds.", r:"Matthew 5:16" },
    { m:"Rest is not quitting; it is trusting. Lie down tonight like someone who is watched over.", v:"In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety.", r:"Psalm 4:8" },
    { m:"The answer is already moving toward you. Your job today is to stay soft and stay ready.", v:"See, I am doing a new thing! Now it springs up; do you not perceive it?", r:"Isaiah 43:19" },
    { m:"Grace covers the thing you keep re-confessing. Put it down for good.", v:"As far as the east is from the west, so far has He removed our transgressions from us.", r:"Psalm 103:12" },
    { m:"Strength for today was delivered this morning. Borrow none from tomorrow's worries.", v:"His compassions never fail. They are new every morning.", r:"Lamentations 3:22-23" },
    { m:"You are loved with an everlasting love, on your best day and your worst. Nothing changes that.", v:"I have loved you with an everlasting love; I have drawn you with unfailing kindness.", r:"Jeremiah 31:3" }
  ];

  var PRAYERS = [
    "Lord, walk with me through this day. When I forget You are near, remind me gently.",
    "Father, make me brave enough for today and tired enough tonight to sleep in Your peace.",
    "God, I hand You what I cannot fix. Guard my heart while You work.",
    "Jesus, thank You for this breath, this morning, this chance to begin again.",
    "Lord, let me see one person today the way You see them. Send me where I'm needed.",
    "Father, quiet the noise in me so I can hear Yours.",
    "God, give me today's bread, today's strength, and today's joy. That's enough.",
    "Lord, heal what is hidden in me and make my waiting fruitful."
  ];

  function dailyBlessing() {
    var rng = rngFor("blessing|" + todayStr());
    var b = pick(rng, BLESSINGS);
    var prayer = pick(rng, PRAYERS);
    return { blessing: b, prayer: prayer, date: todayPretty() };
  }

  function aiKey() { return localStorage.getItem("wp_key") || ""; }

  function aiChat(system, user, maxTokens) {
    var key = aiKey();
    if (!key) return Promise.resolve(null);
    return fetch("https://api.mistral.ai/v1/chat/completions", {
      method: "POST",
      headers: { "Content-Type": "application/json", "Authorization": "Bearer " + key },
      body: JSON.stringify({
        model: "mistral-small-latest",
        messages: [{ role: "system", content: system }, { role: "user", content: user }],
        max_tokens: maxTokens || 400,
        temperature: 0.8
      })
    }).then(function (r) { return r.ok ? r.json() : null; })
      .then(function (j) { return j && j.choices && j.choices[0] ? j.choices[0].message.content.trim() : null; })
      .catch(function () { return null; });
  }

  return {
    rngFor: rngFor, pick: pick, shuffle: shuffle, todayStr: todayStr, todayPretty: todayPretty,
    dailyBlessing: dailyBlessing, aiKey: aiKey, aiChat: aiChat
  };
})();
