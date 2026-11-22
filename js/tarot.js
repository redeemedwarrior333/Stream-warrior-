/* ============ Tarot module ============ */
"use strict";

const Tarot = {
  deck: "classic",
  spread: [],
  drawn: [],
  slots: ["PAST", "PRESENT", "FUTURE"],

  el() { return document.getElementById("m-tarot"); },

  render() {
    const decks = [
      { id: "classic", name: "🔮 Mystica (Classic)", cls: "deck-classic" },
      { id: "love", name: "💘 Love Oracle", cls: "deck-love" }
    ];
    App.scene(`
      <div class="module" id="m-tarot">
        <div class="module-head">
          <h2>🔮 Tarot</h2>
          <div class="deck-picker">
            ${decks.map(d => `<button class="btn small deck-btn ${this.deck === d.id ? "active" : ""} ${d.cls}" data-deck="${d.id}">${d.name}</button>`).join("")}
          </div>
        </div>
        <p class="hint center">Ask chat for questions → tap a card to draw → tap a placed card to flip it back or read it big.</p>
        <div class="spread-row" id="tarot-spread">
          ${this.slots.map((s, i) => this.spread[i]
            ? `<div class="tarot-slot filled"><span class="slot-label">${s}</span>${this.cardHTML(this.spread[i])}</div>`
            : `<div class="tarot-slot empty" data-slot="${i}"><span class="slot-label">${s}</span><div class="tarot-card back" data-draw="${i}"><div class="card-face back-face">✦</div><div class="card-face front-face"></div></div></div>`
          ).join("")}
        </div>
        <div class="row center gap">
          <button class="btn small" id="tarot-shuffle">🔀 Shuffle</button>
          <button class="btn small" id="tarot-reset">↺ Clear</button>
          <button class="btn small ${AI.enabled ? "" : "dim"}" id="tarot-ai-reading">✨ AI Reading</button>
        </div>
        <div class="reading-box hidden" id="tarot-reading"></div>
      </div>
    `, () => this.bind());
  },

  cardHTML(card) {
    const rev = card.reversed;
    return `
      <div class="tarot-card ${rev ? "reversed" : ""} drawn" data-card-idx="${card.idx}">
        <div class="card-face front-face">
          <div class="card-sym">${card.sym}</div>
          <div class="card-name">${card.name}</div>
          <div class="card-tag">${rev ? "REVERSED" : "UPRIGHT"}</div>
        </div>
      </div>`;
  },

  bind() {
    const root = this.el();
    root.querySelectorAll(".deck-btn").forEach(b => b.addEventListener("click", () => { this.deck = b.dataset.deck; this.render(); }));
    root.querySelectorAll("[data-draw]").forEach(b => b.addEventListener("click", () => this.draw(parseInt(b.dataset.draw))));
    root.querySelectorAll("[data-card-idx]").forEach(b => b.addEventListener("click", () => this.showCard(b.dataset.cardIdx)));
    const sh = root.querySelector("#tarot-shuffle"); if (sh) sh.addEventListener("click", () => { this.drawn = []; Sound.play("whoosh"); this.render(); });
    const rs = root.querySelector("#tarot-reset"); if (rs) rs.addEventListener("click", () => { this.spread = []; this.drawn = []; this.hideReading(); this.render(); });
    const ai = root.querySelector("#tarot-ai-reading"); if (ai) ai.addEventListener("click", () => this.aiReading());
  },

  draw(slotIdx) {
    if (this.spread[slotIdx]) return;
    Sound.init();
    const pool = TAROT_CARDS.map((c, idx) => ({ ...c, idx })).filter(c => !this.drawn.includes(c.idx));
    if (!pool.length) return;
    const card = pool[Math.floor(Math.random() * pool.length)];
    card.reversed = Math.random() < 0.35;
    this.drawn.push(card.idx);
    this.spread[slotIdx] = card;
    Sound.play("whoosh");
    setTimeout(() => Sound.play("drum"), 350);
    this.render();
  },

  deckVoice() {
    return this.deck === "love"
      ? "You are the LOVE ORACLE tarot deck for a live stream. Give short, punchy, dramatic love-focused readings (2-3 sentences). Address the chat directly. Be warm but honest."
      : "You are the MYSTICA classic tarot deck for a live stream. Give short, punchy, mystical readings (2-3 sentences). Address the chat directly. Be dramatic and fun.";
  },

  async aiReading() {
    const placed = this.spread.filter(Boolean);
    if (!placed.length) { App.toast("Draw some cards first"); return; }
    if (!AI.enabled) { App.toast("Connect AI in settings for endless readings"); return; }
    const box = this.el().querySelector("#tarot-reading");
    box.classList.remove("hidden");
    box.innerHTML = '<div class="reading-text"></div>';
    const out = box.querySelector(".reading-text");
    /* include the PAST/PRESENT/FUTURE position in the reading */
    const desc = this.spread.map((c, i) => c
      ? `${c.name} (${c.reversed ? "reversed" : "upright"}) in the ${this.slots[i]} position`
      : "").filter(Boolean).join("; ");
    let buf = "";
    try {
      const text = await AI.chat(this.deckVoice(),
        `Reading for the streamer's viewer. Cards: ${desc}. Give one combined reading.`, 420, {
          stream: true,
          onToken: t => { buf += t; out.textContent = buf; box.scrollTop = box.scrollHeight; }
        });
      buf = text || buf;
      out.textContent = buf;
      Sound.play("tada");
    } catch (e) {
      out.textContent = "AI unavailable (" + AI.errText(e) + "). Use the built-in meanings below each pull.";
      Sound.play("buzzer");
    }
  },

  showCard(idx) {
    const card = TAROT_CARDS[idx]; if (!card) return;
    const love = this.deck === "love";
    const up = love ? card.loveUp : card.up;
    const rv = love ? card.loveRev : card.rev;
    App.modal(`
      <div class="big-card ${love ? "deck-love" : "deck-classic"}">
        <div class="big-sym">${card.sym}</div>
        <h2>${card.name}</h2>
        <div class="meaning"><b>⬆️ Upright</b><p>${up}</p></div>
        <div class="meaning"><b>⬇️ Reversed</b><p>${rv}</p></div>
      </div>
    `);
  },

  hideReading() {
    const box = document.getElementById("tarot-reading");
    if (box) { box.classList.add("hidden"); box.textContent = ""; }
  }
};
