/* Warrior Prophet smoke tests — jsdom, mirrors test/headless.js approach */
const fs = require("fs");
const path = require("path");
const { JSDOM } = require(path.join(__dirname, "node_modules", "jsdom"));

const ROOT = path.resolve(__dirname, "..", "warrior-prophet");
let failures = 0;

function loadPage(file) {
  const html = fs.readFileSync(path.join(ROOT, file), "utf8");
  const dom = new JSDOM(html, { runScripts: "outside-only", url: "https://example.com/warrior-prophet/" + file });
  const parts = [];
  const re = /<script(?: src="(js\/[^"]+)")?>([\s\S]*?)<\/script>/g;
  let m;
  while ((m = re.exec(html)) !== null) {
    parts.push(m[1] ? fs.readFileSync(path.join(ROOT, m[1].split("?")[0]), "utf8") : m[2]);
  }
  dom.window.eval(parts.join("\n;\n") +
    "\n;try{window.WP=WP;window.MW_PREMIUM=MW_PREMIUM;window.ASK_PAGE=ASK_PAGE;window.DECKS=DECKS;}catch(e){}");
  return dom.window;
}

function ok(cond, msg) {
  if (cond) { console.log("PASS:", msg); }
  else { console.error("FAIL:", msg); failures++; }
}

function flush(ms) { return new Promise(r => setTimeout(r, ms)); }

(async function main() {
  /* index */
  let w = loadPage("index.html");
  let teaser = w.document.getElementById("teaser");
  ok(teaser && teaser.textContent.length > 80, "index: today's word teaser renders");
  ok(w.document.querySelectorAll(".navcard").length === 4, "index: 4 nav cards");

  /* daily */
  w = loadPage("daily.html");
  ok(w.document.getElementById("blessing").textContent.includes("—"), "daily: blessing + verse render");
  w.document.getElementById("praybtn").click();
  ok(!w.document.getElementById("prayer").hidden, "daily: prayer toggle opens");
  ok(w.document.getElementById("prayer").textContent.length > 20, "daily: prayer has content");

  /* cards */
  w = loadPage("cards.html");
  const decks = w.document.querySelectorAll(".deck");
  ok(decks.length === 3, "cards: 3 decks render");
  ok(decks[2].classList.contains("locked"), "cards: Angel Watch locked for free users");
  w.document.getElementById("draw1").click();
  await flush(1000);
  ok(w.document.getElementById("c0").classList.contains("flip"), "cards: single draw flips");
  ok(w.document.getElementById("p0").textContent === "Your message", "cards: single draw label");
  ok(!w.document.getElementById("reading").hidden, "cards: reading text appears");
  w.document.getElementById("draw3").click();
  await flush(2200);
  ok([0,1,2].every(i => w.document.getElementById("c"+i).classList.contains("flip")), "cards: 3-card draw flips all");
  ok(w.document.getElementById("p0").textContent === "Where you've been", "cards: 3-card positions");
  w.window.confirm = () => false;
  w.document.getElementById("deck-angels").click();
  ok(!w.document.getElementById("deck-angels").classList.contains("on"), "cards: locked deck refuses selection");

  /* premium unlock then deck access */
  w = loadPage("cards.html");
  w.localStorage.setItem("wp_premium", "1");
  w.document.getElementById("deck-angels").click();
  ok(w.document.getElementById("deck-angels").classList.contains("on"), "premium: Angel Watch unlocks");
  w.document.getElementById("draw1").click();
  await flush(1000);
  ok(w.document.getElementById("reading").textContent.length > 50, "premium: angel deck draws");

  /* angels */
  w = loadPage("angels.html");
  ok(w.document.querySelectorAll("#popular .chip").length === 15, "angels: popular chips render");
  w.document.getElementById("numin").value = "1111";
  w.document.getElementById("lookupbtn").click();
  ok(w.document.getElementById("rnum").textContent === "1111", "angels: 1111 resolves");
  ok(w.document.getElementById("rtitle").textContent === "Gateway", "angels: 1111 title");
  ok(w.document.getElementById("rbody").textContent.includes("Revelation 3:8"), "angels: 1111 verse shown");
  w.document.getElementById("numin").value = "5555";
  w.document.getElementById("lookupbtn").click();
  ok(w.document.getElementById("rbody").textContent.includes("Grace"), "angels: 5555 falls back to digit 5 meaning");

  /* ask */
  w = loadPage("ask.html");
  w.document.getElementById("q").value = "I'm scared about losing my job";
  w.document.getElementById("askbtn").click();
  await flush(300);
  const ans = w.document.getElementById("answer").textContent;
  ok(ans.length > 100, "ask: answer appears");
  ok(ans.includes("Fear") || ans.includes("fear"), "ask: scared question matches fear topic");
  ok(ans.includes("—"), "ask: verse reference present");
  w.document.getElementById("q").value = "I feel so much guilt about my past";
  w.document.getElementById("askbtn").click();
  await flush(300);
  const ans2 = w.document.getElementById("answer").textContent;
  ok(ans2.includes("Forgiveness"), "ask: guilt matches forgiveness topic — got: " + ans2.slice(0, 60));

  /* premium page */
  w = loadPage("premium.html");
  w.document.getElementById("code").value = "WRONG-CODE";
  w.document.getElementById("unlockbtn").click();
  ok(w.localStorage.getItem("wp_premium") !== "1", "premium: bad code rejected");
  w.document.getElementById("code").value = "mw-founder";
  w.document.getElementById("unlockbtn").click();
  ok(w.localStorage.getItem("wp_premium") === "1", "premium: valid code unlocks (case-insensitive)");
  ok(!w.document.getElementById("status").hidden, "premium: active status shows");

  /* engine determinism: same day => same blessing */
  w = loadPage("daily.html");
  const b1 = w.WP.dailyBlessing();
  const b2 = w.WP.dailyBlessing();
  ok(b1.blessing.m === b2.blessing.m && b1.prayer === b2.prayer, "engine: daily blessing is deterministic per day");

  console.log(failures ? ("\n" + failures + " FAILURES") : "\nALL PROPHET SMOKE TESTS PASSED");
  process.exit(failures ? 1 : 0);
})().catch(e => { console.error("CRASH:", e); process.exit(1); });
