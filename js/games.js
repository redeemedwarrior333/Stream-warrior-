/* ============ Game modules: 20Q, Trivia, Mad Libs, Jokes, Lines, Facts, WYR, Wheel ============ */
"use strict";

/* ---------- 20 QUESTIONS ---------- */
const TwentyQ = {
  secret: null, count: 0, log: [], over: false,

  el() { return document.getElementById("m-twentyq"); },

  start() {
    this.secret = TWENTYQ_ENTITIES[Math.floor(Math.random() * TWENTYQ_ENTITIES.length)];
    this.count = 0; this.log = []; this.over = false;
    this.render();
  },

  render() {
    App.scene(`
      <div class="module" id="m-twentyq">
        <div class="module-head"><h2>❓ 20 Questions</h2>
          <span class="q-count ${this.count >= 15 ? "danger" : ""}">${this.over ? "GAME OVER" : "Question " + (this.count + 1) + "/20"}</span>
        </div>
        <p class="hint center">Chat asks questions out loud — you type them in, Stream Aid answers. Chat guesses the thing; you type the guess to check it.</p>
        <div class="q-log" id="tq-log">${this.log.map(l => `<div class="q-entry"><span class="q-q">${l.q}</span><span class="q-a ${l.a === "YES" ? "yes" : l.a === "NOPE" ? "no" : "maybe"}">${l.a}</span></div>`).join("") || '<div class="hint center">No questions yet.</div>'}</div>
        <div class="row gap">
          <input id="tq-input" class="text-input grow" type="text" placeholder="Type chat's question or guess…" autocomplete="off">
          <button class="btn" id="tq-ask">Ask</button>
        </div>
        <div class="row center gap">
          <button class="btn small" id="tq-reveal">👁️ Reveal</button>
          <button class="btn small" id="tq-new">↺ New Round</button>
        </div>
      </div>
    `, () => {
      const root = this.el();
      const input = root.querySelector("#tq-input");
      input.focus();
      input.addEventListener("keydown", e => { if (e.key === "Enter") this.ask(); });
      root.querySelector("#tq-ask").addEventListener("click", () => this.ask());
      root.querySelector("#tq-reveal").addEventListener("click", () => this.reveal());
      root.querySelector("#tq-new").addEventListener("click", () => this.start());
    });
  },

  parseQuestion(text) {
    const t = text.toLowerCase();
    for (const [re, attr] of TWENTYQ_KEYWORDS) if (re.test(t)) return attr;
    return null;
  },

  ask() {
    if (this.over) { App.toast("Round over — start a new one"); return; }
    const input = this.el().querySelector("#tq-input");
    const text = input.value.trim();
    if (!text) return;
    input.value = "";
    const t = text.toLowerCase();

    /* correct guess? */
    const guessWords = ["it is", "is it", "the answer", "i know", "guess"];
    const secretName = this.secret.name.replace(/^(a|an|the) /, "");
    if (t.includes(secretName)) {
      this.count++;
      this.log.push({ q: text, a: "🎉 YES!" });
      this.over = true;
      Sound.play("tada");
      this.render();
      App.toast(`Chat got it in ${this.count}: ${this.secret.name} ${this.secret.sym}`);
      return;
    }

    const attr = this.parseQuestion(text);
    let answer = "🤔 Hmm…";
    if (attr) {
      answer = this.secret.a[attr] ? "YES" : "NOPE";
    } else if (/^(is|are|does|do|can|would|will|has|have)\b/.test(t)) {
      answer = "🤔 Try: " + this.randomAttrHint();
    } else {
      answer = "❓ Ask a yes/no question";
    }
    this.count++;
    this.log.push({ q: text, a: answer });
    if (answer === "YES") Sound.play("ding"); else if (answer === "NOPE") Sound.play("buzzer");
    if (this.count >= 20) this.over = true;
    this.render();
  },

  randomAttrHint() {
    const keys = Object.keys(TWENTYQ_ATTRS);
    return TWENTYQ_ATTRS[keys[Math.floor(Math.random() * keys.length)]];
  },

  reveal() {
    App.modal(`<div class="reveal-card"><p class="hint">The secret was</p><div class="big-sym">${this.secret.sym}</div><h2>${this.secret.name}</h2></div>`);
  }
};

/* ---------- TRIVIA ---------- */
const Trivia = {
  used: new Set(), current: null, revealed: false, pool: [],

  el() { return document.getElementById("m-trivia"); },

  pick() {
    if (!this.pool.length) this.pool = [...TRIVIA];
    let i;
    do { i = Math.floor(Math.random() * this.pool.length); } while (this.used.has(i) && this.used.size < this.pool.length);
    this.used.add(i);
    this.current = this.pool[i];
    this.revealed = false;
  },

  render() {
    if (!this.current) this.pick();
    const q = this.current;
    const opts = q.a.map((a, i) => `<button class="opt ${this.revealed ? (i === q.k ? "correct" : "dim") : ""}" data-opt="${i}">${String.fromCharCode(65 + i)}. ${a}</button>`).join("");
    App.scene(`
      <div class="module" id="m-trivia">
        <div class="module-head"><h2>🧠 Trivia</h2><span class="q-count">${q.c}${q.ai ? " ✨" : ""}</span></div>
        <div class="question-card"><p>${q.q}</p></div>
        <div class="opts">${opts}</div>
        <div class="row center gap">
          ${this.revealed ? '<button class="btn" id="triv-next">Next ▶</button>' : '<button class="btn" id="triv-reveal">Reveal Answer</button>'}
          <button class="btn small ${AI.enabled ? "" : "dim"}" id="triv-ai">✨ AI Question</button>
        </div>
      </div>
    `, () => {
      const root = this.el();
      root.querySelectorAll("[data-opt]").forEach(b => b.addEventListener("click", () => {
        if (!this.revealed) { this.revealed = true; Sound.play(parseInt(b.dataset.opt) === q.k ? "tada" : "buzzer"); this.render(); }
      }));
      const rv = root.querySelector("#triv-reveal"); if (rv) rv.addEventListener("click", () => { this.revealed = true; Sound.play("drumroll"); this.render(); });
      const nx = root.querySelector("#triv-next"); if (nx) nx.addEventListener("click", () => { this.pick(); this.render(); });
      const ai = root.querySelector("#triv-ai"); if (ai) ai.addEventListener("click", () => this.aiQuestion());
    });
  },

  async aiQuestion() {
    if (!AI.enabled) { App.toast("Connect AI in ⚙️ settings"); return; }
    App.toast("✨ Generating…");
    try {
      const arr = await AI.list("trivia", 5, "no repeats, fun categories");
      this.pool.push(...arr);
      this.current = arr[0]; this.revealed = false;
      this.render();
    } catch (e) { App.toast("AI unavailable"); }
  }
};

/* ---------- MAD LIBS ---------- */
const MadLibs = {
  tpl: null, words: [], step: 0, filled: null,

  el() { return document.getElementById("m-madlibs"); },

  start() {
    this.tpl = MADLIBS[Math.floor(Math.random() * MADLIBS.length)];
    this.words = this.tpl.slots.map(s => "");
    this.step = 0; this.filled = false;
    this.render();
  },

  render() {
    if (!this.tpl) return this.start();
    const root = this.el.bind(this);
    if (this.filled) {
      let story = this.tpl.story;
      this.words.forEach((w, i) => { story = story.split("{" + i + "}").join(`<b class="hl">${w || "___"}</b>`); });
      App.scene(`
        <div class="module" id="m-madlibs">
          <div class="module-head"><h2>📝 ${this.tpl.title}</h2></div>
          <div class="story-card"><p>${story}</p></div>
          <div class="row center gap">
            <button class="btn" id="ml-again">🎲 New Story</button>
            <button class="btn small" id="ml-edit">✏️ Edit Words</button>
          </div>
        </div>
      `, () => {
        const r = root();
        r.querySelector("#ml-again").addEventListener("click", () => { Sound.play("whoosh"); this.start(); });
        r.querySelector("#ml-edit").addEventListener("click", () => { this.filled = false; this.render(); });
      });
      return;
    }
    App.scene(`
      <div class="module" id="m-madlibs">
        <div class="module-head"><h2>📝 ${this.tpl.title}</h2><span class="q-count">${this.step + 1}/${this.tpl.slots.length}</span></div>
        <p class="hint center">Ask chat for a <b>${this.tpl.slots[this.step]}</b></p>
        <div class="word-chips">${this.tpl.slots.map((s, i) => `<span class="chip ${i === this.step ? "active" : i < this.step ? "done" : ""}">${i < this.step ? this.words[i] : s}</span>`).join("")}</div>
        <div class="row gap">
          <input id="ml-input" class="text-input grow" type="text" placeholder="Type the word…" autocomplete="off">
          <button class="btn" id="ml-add">Add</button>
        </div>
        <div class="row center gap">
          <button class="btn small" id="ml-skip">Skip</button>
          <button class="btn small" id="ml-new">🎲 Different Story</button>
        </div>
      </div>
    `, () => {
      const r = root();
      const input = r.querySelector("#ml-input");
      input.focus();
      const add = () => {
        const v = input.value.trim();
        if (v) { this.words[this.step] = v; Sound.play("ding"); }
        this.step++;
        if (this.step >= this.tpl.slots.length) { this.filled = true; Sound.play("tada"); }
        this.render();
      };
      input.addEventListener("keydown", e => { if (e.key === "Enter") add(); });
      r.querySelector("#ml-add").addEventListener("click", add);
      r.querySelector("#ml-skip").addEventListener("click", () => { this.step++; if (this.step >= this.tpl.slots.length) { this.filled = true; Sound.play("tada"); } this.render(); });
      r.querySelector("#ml-new").addEventListener("click", () => this.start());
    });
  }
};

/* ---------- Simple list modules: Jokes, Lines, Facts, WYR ---------- */
function makeListModule(cfg) {
  return {
    cfg, idx: -1, aiExtra: [],
    el() { return document.getElementById("m-" + cfg.id); },
    pool() { return this.aiExtra.length && Math.random() < 0.5 ? this.aiExtra : cfg.data; },
    next() {
      const pool = this.pool();
      this.idx = Math.floor(Math.random() * pool.length);
      Sound.play(cfg.sfx || "ding");
      this.render();
    },
    render() {
      const pool = this.aiExtra.length ? this.aiExtra : cfg.data;
      if (this.idx < 0) this.idx = Math.floor(Math.random() * pool.length);
      const item = pool[this.idx];
      const text = typeof item === "string" ? item : item.text;
      App.scene(`
        <div class="module" id="m-${cfg.id}">
          <div class="module-head"><h2>${cfg.icon} ${cfg.title}</h2>${item.ai ? '<span class="q-count">✨ AI</span>' : ""}</div>
          <div class="question-card big-text"><p>${text}</p></div>
          <div class="row center gap">
            <button class="btn" id="lm-next">${cfg.nextLabel || "Next ▶"}</button>
            <button class="btn small ${AI.enabled ? "" : "dim"}" id="lm-ai">✨ More</button>
          </div>
        </div>
      `, () => {
        const r = this.el();
        r.querySelector("#lm-next").addEventListener("click", () => this.next());
        r.querySelector("#lm-ai").addEventListener("click", () => this.more());
      });
    },
    async more() {
      if (!AI.enabled) { App.toast("Connect AI in ⚙️ settings"); return; }
      App.toast("✨ Generating…");
      try {
        const items = await AI.list(cfg.id === "jokes" ? "jokes" : cfg.id === "lines" ? "lines" : cfg.id === "facts" ? "facts" : "wyr", 10);
        this.aiExtra = items.map(i => i.text || i);
        this.idx = 0;
        this.render();
      } catch (e) { App.toast("AI unavailable"); }
    }
  };
}

const Jokes = makeListModule({ id: "jokes", icon: "😂", title: "Jokes", data: JOKES, sfx: "rimshot", nextLabel: "Another 😂" });
const Lines = makeListModule({ id: "lines", icon: "💘", title: "Pickup Lines", data: PICKUPLINES, sfx: "boing", nextLabel: "Another 💘" });
const Facts = makeListModule({ id: "facts", icon: "💡", title: "Fun Facts", data: FACTS, sfx: "ding", nextLabel: "Another 💡" });
const WYR  = makeListModule({ id: "wyr", icon: "⚔️", title: "Would You Rather", data: WYR, sfx: "whoosh", nextLabel: "Another ⚔️" });

/* ---------- WHEEL OF CHAOS ---------- */
const Wheel = {
  angle: 0, spinning: false,
  el() { return document.getElementById("m-wheel"); },
  segAngle() { return (Math.PI * 2) / WHEEL_SEGMENTS.length; },

  render() {
    App.scene(`
      <div class="module" id="m-wheel">
        <div class="module-head"><h2>🎡 Wheel of Chaos</h2></div>
        <p class="hint center">Dead air? Spin it. Whatever it lands on, you run.</p>
        <div class="wheel-wrap"><canvas id="wheel-canvas" width="640" height="640"></canvas></div>
        <div class="row center"><button class="btn big" id="wheel-spin">🎯 SPIN</button></div>
      </div>
    `, () => {
      this.draw();
      this.el().querySelector("#wheel-spin").addEventListener("click", () => this.spin());
    });
  },

  draw() {
    const cv = this.el().querySelector("#wheel-canvas"); if (!cv) return;
    const ctx = cv.getContext("2d");
    const cx = 320, cy = 320, r = 300;
    const seg = this.segAngle();
    const colors = ["#7c3aed", "#db2777", "#0ea5e9", "#f59e0b", "#10b981", "#ef4444", "#8b5cf6", "#ec4899", "#14b8a6"];
    WHEEL_SEGMENTS.forEach((s, i) => {
      const a0 = this.angle + i * seg, a1 = a0 + seg;
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.arc(cx, cy, r, a0, a1); ctx.closePath();
      ctx.fillStyle = colors[i % colors.length]; ctx.fill();
      ctx.strokeStyle = "#0b0b14"; ctx.lineWidth = 4; ctx.stroke();
      ctx.save();
      ctx.translate(cx + Math.cos(a0 + seg / 2) * r * 0.62, cy + Math.sin(a0 + seg / 2) * r * 0.62);
      ctx.rotate(a0 + seg / 2 + Math.PI / 2);
      ctx.fillStyle = "#fff"; ctx.font = "bold 34px sans-serif"; ctx.textAlign = "center";
      ctx.fillText(s.label, 0, 0);
      ctx.restore();
    });
    /* pointer */
    ctx.beginPath();
    ctx.moveTo(cx - 24, 8); ctx.lineTo(cx + 24, 8); ctx.lineTo(cx, 60); ctx.closePath();
    ctx.fillStyle = "#fff"; ctx.fill();
  },

  spin() {
    if (this.spinning) return;
    this.spinning = true;
    Sound.init(); Sound.play("drumroll");
    const target = Math.floor(Math.random() * WHEEL_SEGMENTS.length);
    const seg = this.segAngle();
    /* land pointer (top, angle -PI/2) on middle of chosen segment */
    const finalAngle = -Math.PI / 2 - (target + 0.5) * seg + (Math.PI * 2) * 6 - (Math.random() * seg * 0.4 - seg * 0.2);
    const start = this.angle, delta = finalAngle - start;
    const dur = 4200, t0 = performance.now();
    const step = (now) => {
      const p = Math.min(1, (now - t0) / dur);
      const ease = 1 - Math.pow(1 - p, 4);
      this.angle = start + delta * ease;
      this.draw();
      if (p < 1) requestAnimationFrame(step);
      else {
        this.spinning = false;
        Sound.play("tada");
        const chosen = WHEEL_SEGMENTS[target];
        App.toast(`🎡 ${chosen.label}!`);
        setTimeout(() => App.openModule(chosen.module), 900);
      }
    };
    requestAnimationFrame(step);
  }
};
