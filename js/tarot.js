/* ============ Tarot module — v2: 9 spreads, Cut the Deck, Mystica chat ============ */
"use strict";

const Tarot = {
  deck: "classic",
  mode: "draw",                 /* "draw" | "chase" */
  spreadId: "ppf",
  spread: [],
  drawn: [],
  question: "",
  caught: [],
  chaseBuilt: false,
  chat: [],                     /* Mystica chat history [{role, text}] */
  lastReading: "",

  SPREADS: [
    { id: "ppf",       name: "Past • Present • Future",      icon: "🕰️", hint: "The classic arc",               slots: ["PAST", "PRESENT", "FUTURE"] },
    { id: "single",    name: "Single Pull",                   icon: "🎯", hint: "One card, one truth",           slots: ["YOUR MESSAGE"] },
    { id: "yesno",     name: "Yes / No Oracle",               icon: "✅", hint: "Straight answer",               slots: ["THE ANSWER"] },
    { id: "sao",       name: "Situation • Action • Outcome",  icon: "🧭", hint: "Problem → play → payoff",       slots: ["SITUATION", "ACTION", "OUTCOME"] },
    { id: "mindbod",   name: "Mind • Body • Spirit",          icon: "☯️", hint: "Full check-in",                 slots: ["MIND", "BODY", "SPIRIT"] },
    { id: "lovecross", name: "Love Cross",                    icon: "💘", hint: "You, them, spark, block",       slots: ["YOU", "THEM", "THE SPARK", "THE BLOCK"] },
    { id: "career",    name: "Career Compass",                icon: "💼", hint: "Stand, block, move, prize",     slots: ["STAND", "BLOCK", "NEXT MOVE", "PRIZE"] },
    { id: "shadow",    name: "Shadow Work",                   icon: "🌑", hint: "Hide, cost, free",              slots: ["WHAT YOU HIDE", "WHAT IT COSTS", "WHAT FREES YOU"] },
    { id: "week",      name: "Week Ahead",                    icon: "🗓️", hint: "A card per day + the lesson",   slots: ["MON", "TUE", "WED", "THU", "FRI", "WEEKEND", "LESSON"] }
  ],

  SUGGESTIONS: [
    { icon: "😂", label: "Jokes",     mod: "jokes" },
    { icon: "💡", label: "Facts",     mod: "facts" },
    { icon: "❓", label: "20 Q's",    mod: "twentyq" },
    { icon: "🧠", label: "Trivia",    mod: "trivia" },
    { icon: "⚔️", label: "WYR",       mod: "wyr" },
    { icon: "⚖️", label: "Laws",      mod: "laws" }
  ],

  get spreadObj() { return this.SPREADS.find(s => s.id === this.spreadId) || this.SPREADS[0]; },

  el() { return document.getElementById("m-tarot"); },

  ensureCSS() {
    if (document.getElementById("tarot2-css")) return;
    const st = document.createElement("style");
    st.id = "tarot2-css";
    st.textContent = `
      .tp-spreads { display:flex; flex-wrap:wrap; gap:6px; margin:10px 0; }
      .tp-spread { border:1px solid rgba(255,255,255,.14); background:rgba(255,255,255,.05); color:inherit;
        border-radius:10px; padding:7px 10px; font-size:12.5px; cursor:pointer; line-height:1.3; }
      .tp-spread.active { border-color:var(--accent, #f5c56b); box-shadow:0 0 10px rgba(245,197,107,.25) inset; }
      .tp-spread b { display:block; font-size:12.5px; }
      .tp-modes { display:flex; gap:8px; margin:8px 0; }
      .tp-modes .btn { flex:1; }
      .tp-q { width:100%; margin:6px 0; }
      .tp-chase { position:relative; height:280px; border-radius:14px; overflow:hidden; margin:10px 0;
        background:radial-gradient(420px 220px at 50% 40%, rgba(255,255,255,.06), rgba(0,0,0,.25));
        border:1px solid rgba(255,255,255,.12); }
      .tp-fly { position:absolute; width:56px; aspect-ratio:2/3.4; border-radius:8px; cursor:pointer;
        background:linear-gradient(160deg, rgba(255,255,255,.14), rgba(255,255,255,.05));
        border:1px solid rgba(255,255,255,.25); display:flex; align-items:center; justify-content:center;
        font-size:22px; transform:rotate(var(--r)); transition:left .5s ease, top .5s ease, opacity .4s, border-color .3s; }
      .tp-fly.caught { border-color:var(--accent, #f5c56b); box-shadow:0 0 12px rgba(245,197,107,.5); }
      .tp-fly.aftermath { opacity:.22; }
      .tp-meta { text-align:center; font-size:13px; font-weight:700; min-height:20px; }
      .tp-actions { display:flex; gap:8px; margin:8px 0; flex-wrap:wrap; }
      .tp-actions .btn { flex:1 1 auto; }
      .tp-chat { margin-top:14px; border:1px solid rgba(255,255,255,.12); border-radius:14px; padding:10px; }
      .tp-chat .tp-head { display:flex; align-items:center; gap:8px; cursor:default; }
      .tp-chat .tp-ava { width:36px; height:36px; border-radius:50%; display:flex; align-items:center; justify-content:center;
        font-size:20px; background:rgba(255,255,255,.08); border:1px solid rgba(255,255,255,.2); flex:none; }
      .tp-chatlog { max-height:220px; overflow-y:auto; display:flex; flex-direction:column; gap:8px; margin:10px 0 8px; }
      .tp-msg { max-width:88%; padding:8px 11px; border-radius:12px; font-size:13.5px; line-height:1.5; white-space:pre-wrap; }
      .tp-msg.m { align-self:flex-start; background:rgba(255,255,255,.07); }
      .tp-msg.u { align-self:flex-end; background:rgba(245,197,107,.15); }
      .tp-row { display:flex; gap:8px; }
      .tp-row input { flex:1; }
      .tp-go { display:flex; flex-wrap:wrap; gap:6px; margin-top:10px; align-items:center; }
      .tp-go .lbl { font-size:11px; opacity:.7; margin-right:2px; }
    `;
    document.head.appendChild(st);
  },

  /* ---------- render ---------- */
  render() {
    this.ensureCSS();
    const s = this.spreadObj;
    App.scene(`
      <div class="module" id="m-tarot">
        <div class="module-head">
          <h2>🔮 Tarot</h2>
          <div class="deck-picker">
            <button class="btn small deck-btn ${this.deck === "classic" ? "active deck-classic" : ""}" data-deck="classic">🔮 Mystica</button>
            <button class="btn small deck-btn ${this.deck === "love" ? "active deck-love" : ""}" data-deck="love">💘 Love Oracle</button>
          </div>
        </div>

        <div class="tp-spreads">
          ${this.SPREADS.map(sp => `<button class="tp-spread ${sp.id === this.spreadId ? "active" : ""}" data-spread="${sp.id}"><b>${sp.icon} ${sp.name}</b>${sp.slots.length} card${sp.slots.length > 1 ? "s" : ""} · ${sp.hint}</button>`).join("")}
        </div>

        <div class="tp-modes">
          <button class="btn small ${this.mode === "draw" ? "" : "ghost"}" data-mode="draw">🃏 Draw</button>
          <button class="btn small ${this.mode === "chase" ? "" : "ghost"}" data-mode="chase">🌀 Cut the Deck</button>
        </div>

        <input class="tp-q" id="tp-question" maxlength="140" placeholder="Viewer's question? (optional)" value="${this.question.replace(/"/g, "&quot;")}">

        ${this.mode === "draw" ? this.tableHTML() : `
          ${this.chaseHTML()}
          <div class="tp-meta" id="tp-chase-meta"></div>
        `}

        <div class="tp-actions">
          <button class="btn small" id="tp-go">${this.mode === "chase" ? "🌀 Start the Shuffle" : "✨ Reveal My Cards"}</button>
          <button class="btn small ghost" id="tp-clear">↺ Clear</button>
        </div>

        <div class="reading-box" id="tp-reading"></div>

        <div class="tp-go" id="tp-cross" hidden>
          <span class="lbl">Mystica sends you to:</span>
          ${this.SUGGESTIONS.map(g => `<button class="btn tiny ghost" data-mod="${g.mod}">${g.icon} ${g.label}</button>`).join("")}
        </div>

        <div class="tp-chat">
          <div class="tp-head"><div class="tp-ava">🔮</div><div><b>Mystica</b><br><span class="hint">Talk to her — she reads the cards and the chat.</span></div></div>
          <div class="tp-chatlog" id="tp-chatlog"></div>
          <div class="tp-row">
            <input id="tp-chat-input" maxlength="300" placeholder="Talk to Mystica...">
            <button class="btn small" id="tp-chat-send">Send</button>
          </div>
        </div>
      </div>
    `, () => this.bind());
  },

  tableHTML() {
    const s = this.spreadObj;
    return `<div class="spread-row" id="tp-table">
      ${s.slots.map((name, i) => this.spread[i]
        ? `<div class="tarot-slot filled"><span class="slot-label">${name}</span>${this.cardHTML(this.spread[i], i)}</div>`
        : `<div class="tarot-slot empty" data-slot="${i}"><span class="slot-label">${name}</span><div class="tarot-card back"><div class="card-face back-face">✦</div><div class="card-face front-face"></div></div></div>`
      ).join("")}
    </div>`;
  },

  chaseHTML() {
    return `<div class="tp-chase" id="tp-chase"></div>`;
  },

  cardHTML(card, idx) {
    const rev = card.reversed;
    return `
      <div class="tarot-card ${rev ? "reversed" : ""} drawn" data-card-idx="${card.idx}" data-flip="${idx}">
        <div class="card-face front-face">
          <div class="card-sym">${card.sym}</div>
          <div class="card-name">${card.name}</div>
          <div class="card-tag">${rev ? "REVERSED" : "UPRIGHT"}</div>
        </div>
      </div>`;
  },

  /* ---------- binding ---------- */
  bind() {
    const root = this.el();
    const q = root.querySelector("#tp-question");
    if (q) {
      q.addEventListener("change", () => { this.question = q.value.trim(); });
      q.addEventListener("keydown", e => { if (e.key === "Enter") root.querySelector("#tp-go").click(); });
    }
    root.querySelectorAll(".deck-btn").forEach(b => b.addEventListener("click", () => { this.deck = b.dataset.deck; this.render(); }));
    root.querySelectorAll("[data-spread]").forEach(b => b.addEventListener("click", () => { this.spreadId = b.dataset.spread; this.spread = []; this.render(); }));
    root.querySelectorAll("[data-mode]").forEach(b => b.addEventListener("click", () => { this.mode = b.dataset.mode; this.spread = []; this.caught = []; this.render(); }));
    root.querySelectorAll("[data-slot]").forEach(b => b.addEventListener("click", () => this.draw(parseInt(b.dataset.slot))));
    root.querySelectorAll("[data-flip]").forEach(b => b.addEventListener("click", () => this.showCard(parseInt(b.dataset.cardIdx))));
    root.querySelector("#tp-go").addEventListener("click", () => { this.question = q.value.trim(); this.mode === "chase" ? this.startChase() : this.fullDraw(); });
    root.querySelector("#tp-clear").addEventListener("click", () => { this.spread = []; this.drawn = []; this.caught = []; this.lastReading = ""; this.hideReading(); this.render(); });
    root.querySelectorAll("[data-mod]").forEach(b => b.addEventListener("click", () => App.openModule(b.dataset.mod)));
    root.querySelector("#tp-chat-send").addEventListener("click", () => this.sendChat());
    const ci = root.querySelector("#tp-chat-input");
    if (ci) ci.addEventListener("keydown", e => { if (e.key === "Enter") this.sendChat(); });
    this.paintChat();
  },

  /* ---------- drawing ---------- */
  draw(slotIdx) {
    if (this.spread[slotIdx]) return;
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

  /* full spread at once (draw mode button) */
  fullDraw() {
    const s = this.spreadObj;
    this.spread = [];
    this.drawn = [];
    const pool = [];
    TAROT_CARDS.forEach((c, idx) => pool.push(idx));
    for (let i = pool.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [pool[i], pool[j]] = [pool[j], pool[i]];
    }
    for (let i = 0; i < s.slots.length; i++) {
      const card = { ...TAROT_CARDS[pool[i]], idx: pool[i] };
      card.reversed = Math.random() < 0.35;
      this.spread[i] = card;
      this.drawn.push(pool[i]);
    }
    Sound.play("whoosh");
    this.render();
    setTimeout(() => this.reading(true), 500);
  },

  /* ---------- cut the deck ---------- */
  startChase() {
    const s = this.spreadObj;
    const table = this.el().querySelector("#tp-chase");
    const meta = this.el().querySelector("#tp-chase-meta");
    if (!table) return;
    table.innerHTML = "";
    meta.textContent = `Tap ${s.slots.length} card${s.slots.length > 1 ? "s" : ""} as the deck flies — the ones that leap out are the reading`;
    this.caught = [];
    const els = [];
    const pool = [];
    TAROT_CARDS.forEach((c, idx) => pool.push(idx));
    for (let i = pool.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [pool[i], pool[j]] = [pool[j], pool[i]];
    }
    const flying = pool.slice(0, 14);
    flying.forEach((idx, n) => {
      setTimeout(() => {
        const card = { ...TAROT_CARDS[idx], idx };
        card.reversed = Math.random() < 0.35;
        const el = document.createElement("div");
        el.className = "tp-fly";
        el.style.setProperty("--r", ((Math.random() * 22 - 11)).toFixed(1) + "deg");
        el.style.left = (4 + Math.random() * 80) + "%";
        el.style.top = (4 + Math.random() * 68) + "%";
        el.textContent = "🌙";
        const fc = { card, el };
        el.addEventListener("click", () => this.catchCard(fc, els, table, meta));
        table.appendChild(el);
        els.push(fc);
      }, n * 130);
    });
    Sound.play("whoosh");
  },

  catchCard(fc, els, table, meta) {
    const s = this.spreadObj;
    if (!fc || fc.caught || this.caught.length >= s.slots.length) return;
    fc.caught = true;
    fc.el.classList.add("caught");
    this.caught.push(fc);
    Sound.play("ding");
    /* scatter the rest */
    els.forEach(other => {
      if (!other.caught) {
        other.el.style.left = (4 + Math.random() * 80) + "%";
        other.el.style.top = (4 + Math.random() * 68) + "%";
      }
    });
    if (this.caught.length < s.slots.length) {
      meta.textContent = `${this.caught.length} of ${s.slots.length} caught — keep going…`;
      return;
    }
    meta.textContent = "✨ The deck has spoken.";
    els.forEach(o => { if (!o.caught) o.el.classList.add("aftermath"); });
    setTimeout(() => {
      this.spread = this.caught.map(fc => fc.card);
      this.drawn = this.spread.map(c => c.idx);
      this.render();
      this.reading(true);
    }, 800);
  },

  showCard(idx) {
    const card = TAROT_CARDS[idx];
    if (!card) return;
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

  /* ---------- reading ---------- */
  builtInReading() {
    const s = this.spreadObj;
    const lines = this.spread.map((c, i) => {
      const love = this.deck === "love";
      const text = c.reversed ? (love ? c.loveRev : c.rev) : (love ? c.loveUp : c.up);
      return `${c.name} (${c.reversed ? "reversed" : "upright"}) in the ${s.slots[i]} position: ${text}`;
    });
    return lines.join("\n\n");
  },

  async reading(fromButton) {
    if (!this.spread.filter(Boolean).length) { App.toast("Draw some cards first"); return; }
    const box = this.el().querySelector("#tp-reading");
    if (!box) return;
    box.classList.remove("hidden");
    box.innerHTML = '<div class="reading-text"></div>';
    const out = box.querySelector(".reading-text");
    const s = this.spreadObj;
    const desc = this.spread.filter(Boolean).map((c, i) =>
      `${c.name} (${c.reversed ? "reversed" : "upright"}) in the ${s.slots[i]} position`).join("; ");
    const voice = this.deck === "love"
      ? "You are the LOVE ORACLE tarot deck for a live stream."
      : "You are MYSTICA, the classic tarot deck, for a live stream.";
    const sys = `${voice} Write a short tarot reading (max 170 words): one intro line, one line per card weaving its meaning into the question, then a final line starting with "🔥 The Verdict:" that clearly answers yes/no/wait. Dramatic, fun, address the chat. No disclaimers.`;
    const prompt = `Question: ${this.question || "(no question — general energy)"} Spread: ${s.name}. Cards: ${desc}`;
    if (!AI.enabled) {
      out.textContent = this.builtInReading();
      this.lastReading = out.textContent;
      this.mysticaComment();
      return;
    }
    let buf = "";
    try {
      const text = await AI.chat(sys, prompt, 420, { stream: true, onToken: t => { buf += t; out.textContent = buf; box.scrollTop = box.scrollHeight; } });
      buf = text || buf;
      out.textContent = buf;
      this.lastReading = buf;
      Sound.play("tada");
      this.mysticaComment();
    } catch (e) {
      out.textContent = this.builtInReading() + "\n\n(AI unavailable: " + AI.errText(e) + ")";
      this.lastReading = out.textContent;
      this.mysticaComment();
    }
  },

  hideReading() {
    const box = document.getElementById("tp-reading");
    if (box) { box.classList.add("hidden"); box.textContent = ""; }
    const cross = document.getElementById("tp-cross");
    if (cross) cross.hidden = true;
  },

  /* ---------- Mystica cross-game nudge ---------- */
  suggest() {
    const cross = document.getElementById("tp-cross");
    if (!cross) return;
    cross.hidden = false;
    const picks = ["jokes", "facts", "wyr", "trivia", "twentyq"];
    const label = picks[Math.floor(Math.random() * picks.length)];
    return label;
  },

  /* ---------- Mystica chat ---------- */
  paintChat() {
    const log = this.el().querySelector("#tp-chatlog");
    if (!log) return;
    if (this.chat.length === 0) {
      this.chat.push({ role: "m", text: "Welcome to the candlelight. 🔮 Pick a spread, cut the deck, or just talk to me." });
    }
    log.innerHTML = this.chat.map(m => `<div class="tp-msg ${m.role === "m" ? "m" : "u"}"></div>`).join("");
    this.chat.forEach((m, i) => { log.children[i].textContent = m.text; });
    log.scrollTop = log.scrollHeight;
  },

  pushChat(role, text) {
    this.chat.push({ role, text });
    if (this.chat.length > 40) this.chat = this.chat.slice(-30);
  },

  async sendChat() {
    const input = document.getElementById("tp-chat-input");
    if (!input) return;
    const msg = input.value.trim();
    if (!msg) return;
    input.value = "";
    this.pushChat("u", msg);
    this.paintChat();
    const send = document.getElementById("tp-chat-send");
    if (send) { send.disabled = true; send.textContent = "…"; }
    let reply;
    if (AI.enabled) {
      const s = this.spreadObj;
      const ctx = this.lastReading ? `Current reading (${s.name} spread):\n${this.lastReading.slice(0, 600)}`
        : "No reading drawn yet this session.";
      const sys = "You are Mystica, a fun, warm, slightly mysterious tarot reader hosting a party game with a streamer and their chat. Reply in 1-4 short sentences, in character, playful and wise. If asked for a reading, tell them to pick a spread and draw. Never break character, no disclaimers. Occasionally (not every message) you may suggest another Stream Warrior module by name: Jokes, Facts, 20 Q's, Trivia, WYR (Would You Rather), or Weird Laws.";
      try {
        reply = await AI.chat(sys, `${ctx}\n\nGuest says: ${msg}`, 220);
      } catch (e) { reply = null; }
    }
    if (!reply) reply = this.fallbackChat(msg);
    this.pushChat("m", reply);
    this.paintChat();
    if (send) { send.disabled = false; send.textContent = "Send"; }
  },

  fallbackChat(msg) {
    const s = msg.toLowerCase();
    if (/\b(hi|hello|hey|yo)\b/.test(s)) return "Well met, wanderer. 🔮 Draw some cards and I'll read the story the deck tells — or keep talking to me.";
    if (/love|crush|ex\b|relationship|him\b|her\b/.test(s)) return "Ah, matters of the heart — my favorite chaos. Try the Love Oracle deck with the Love Cross spread, then tell me which card landed on 'The Block.' That one never lies. 💘";
    if (/yes|no answer|should i/.test(s)) return "The deck doesn't do maybe — tap ✨ Reveal with the Yes/No Oracle spread and let's hear the verdict properly.";
    if (/another game|bored|someth(ing)? else/.test(s)) {
      const go = this.suggest();
      const meta = App.meta[go];
      return `Ooh, the cards say variety. Try ${meta ? meta.icon : "✨"} ${meta ? meta.label : go} on the mod bar — Mystica's orders.`;
    }
    return "The air around that question is thick. Pull a card with me and we'll slice through it together. 🔮";
  },

  /* after a reading, Mystica comments + nudges another game */
  async mysticaComment() {
    const cross = document.getElementById("tp-cross");
    if (cross) cross.hidden = false;
    let text;
    const s = this.spreadObj;
    if (AI.enabled && this.lastReading) {
      try {
        text = await AI.chat("You are Mystica at a live-stream tarot party. React to the reading just given in 1-2 short playful sentences, then invite the chat to ask you about it. In character, no disclaimers.",
          `Spread: ${s.name}. Reading:\n${this.lastReading.slice(0, 700)}`, 160);
      } catch (e) { text = null; }
    }
    if (!text) {
      const pos = s.slots[Math.floor(Math.random() * s.slots.length)];
      text = `The card on ${pos} is the loudest one in this ${s.name} spread. Ask me why. 👀 And if the vibe wants a pickup, tap a module below — the deck insists.`;
    }
    this.pushChat("m", text);
    this.paintChat();
  }
};
