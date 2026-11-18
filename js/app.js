/* ============ Stream Warrior app shell ============ */
"use strict";

const App = {
  VERSION: "v20",
  modules: {},
  order: ["tarot", "twentyq", "trivia", "soundboard", "madlibs", "jokes", "lines", "facts", "wyr", "laws"],
  meta: {
    tarot:      { icon: "🔮", label: "Tarot" },
    twentyq:    { icon: "❓", label: "20 Q's" },
    trivia:     { icon: "🧠", label: "Trivia" },
    soundboard: { icon: "🔊", label: "Sounds" },
    madlibs:    { icon: "📝", label: "Mad Libs" },
    jokes:      { icon: "😂", label: "Jokes" },
    lines:      { icon: "💘", label: "Lines" },
    facts:      { icon: "💡", label: "Facts" },
    wyr:        { icon: "⚔️", label: "WYR" },
    laws:       { icon: "⚖️", label: "Laws" }
  },
  current: null,
  barHidden: false,
  home: null,

  init() {
    this.modules = { home: Home, tarot: Tarot, twentyq: TwentyQ, trivia: Trivia, madlibs: MadLibs, jokes: Jokes, lines: Lines, facts: Facts, wyr: WYR, laws: WeirdLaws, soundboard: SoundboardUI, wheel: Wheel };
    AI.load();
    this.buildBar();
    this.bindChrome();
    this.updateAIDot();
    this.openModule("home");
    document.addEventListener("keydown", e => {
      if (e.key === "Escape") { this.closeModal(); }
      if (e.ctrlKey && e.key === "f") { e.preventDefault(); this.toggleFullscreen(); }
    });
  },

  buildBar() {
    const bar = document.getElementById("modbar");
    const wheel = document.getElementById("btn-wheel");
    const hide = document.getElementById("btn-hidebar");
    bar.querySelectorAll(".mod-btn:not(.wheel):not(.hide)").forEach(b => b.remove());
    const homeBtn = document.createElement("button");
    homeBtn.className = "mod-btn home-btn";
    homeBtn.dataset.module = "home";
    homeBtn.innerHTML = '🏠<span>Home</span>';
    homeBtn.addEventListener("click", () => this.openModule("home"));
    bar.insertBefore(homeBtn, wheel);
    this.order.forEach(id => {
      const b = document.createElement("button");
      b.className = "mod-btn";
      b.dataset.module = id;
      b.innerHTML = `${this.meta[id].icon}<span>${this.meta[id].label}</span>`;
      b.addEventListener("click", () => this.openModule(id));
      bar.insertBefore(b, hide);
    });
    wheel.addEventListener("click", () => this.openModule("wheel"));
    hide.addEventListener("click", () => this.hideBar());
  },

  bindChrome() {
    document.getElementById("btn-help").addEventListener("click", () => this.openHelp());
    document.getElementById("btn-settings").addEventListener("click", () => this.openSettings());
    document.getElementById("btn-settings-close").addEventListener("click", () => document.getElementById("settings-modal").classList.add("hidden"));
    document.getElementById("ai-dot").addEventListener("click", () => this.openSettings());
    document.getElementById("peek-zone").addEventListener("click", () => this.showBar());
    document.getElementById("btn-ai-test").addEventListener("click", () => this.testAI());

    const nameInput = document.getElementById("set-name");
    nameInput.value = localStorage.getItem("sa_name") || "";
    nameInput.addEventListener("change", () => localStorage.setItem("sa_name", nameInput.value));

    const cfg = AI.config || {};
    document.getElementById("set-endpoint").value = cfg.endpoint || "";
    document.getElementById("set-model").value = cfg.model || "";
    document.getElementById("set-key").value = cfg.key || "";

    document.getElementById("set-sound-file").addEventListener("change", e => this.addSoundFile(e));
  },

  /* ---------- scene ---------- */
  scene(html, bind) {
    const s = document.getElementById("scene");
    s.innerHTML = html;
    if (bind) bind();
  },

  openModule(id) {
    const m = this.modules[id];
    if (!m) return;
    this.current = id;
    document.querySelectorAll(".mod-btn").forEach(b => b.classList.toggle("active", b.dataset.module === id));
    if (id === "soundboard") SoundboardUI.render();
    else if (id === "wheel") Wheel.render();
    else m.render();
  },

  /* ---------- bar hide/show ---------- */
  hideBar() {
    this.barHidden = true;
    document.getElementById("modbar").classList.add("bar-hidden");
    document.getElementById("peek-zone").classList.add("on");
    this.toast("Bar hidden — tap top edge to bring it back");
  },

  showBar() {
    this.barHidden = false;
    document.getElementById("modbar").classList.remove("bar-hidden");
    document.getElementById("peek-zone").classList.remove("on");
  },

  toggleFullscreen() {
    if (!document.fullscreenElement) document.documentElement.requestFullscreen().catch(() => {});
    else document.exitFullscreen();
  },

  /* ---------- modal / toast ---------- */
  modal(html) {
    let m = document.getElementById("quick-modal");
    if (!m) {
      m = document.createElement("div");
      m.id = "quick-modal"; m.className = "modal";
      m.innerHTML = '<div class="modal-card"><div id="quick-modal-content"></div><div class="modal-actions"><button class="btn" id="quick-modal-close">Close</button></div></div>';
      document.body.appendChild(m);
      m.querySelector("#quick-modal-close").addEventListener("click", () => this.closeModal());
      m.addEventListener("click", e => { if (e.target === m) this.closeModal(); });
    }
    m.querySelector("#quick-modal-content").innerHTML = html;
    m.classList.remove("hidden");
  },

  closeModal() {
    const m = document.getElementById("quick-modal");
    if (m) m.classList.add("hidden");
  },

  toast(msg, ms = 2200) {
    const t = document.getElementById("toast");
    t.textContent = msg;
    t.classList.add("show");
    clearTimeout(this._tt);
    this._tt = setTimeout(() => t.classList.remove("show"), ms);
  },

  /* ---------- help / works-with guide ---------- */
  openHelp() {
    const name = localStorage.getItem("sa_name") || "You";
    this.modal(`
      <h2>📺 Works With — How to stream with Stream Warrior</h2>
      <div class="help-block">
        <h3>✅ Apps with screen share (best experience)</h3>
        <p><b>TikTok Live</b> (needs 1,000 followers + 18+), <b>BIGO Live</b> (no follower gate), <b>Twitch</b> mobile, <b>Tango</b>, <b>Mico</b>.</p>
        <p class="hint">Flow: set up your scene BEFORE sharing → go live → tap the screen-share option in the live app → pick Stream Warrior (Chrome tab = viewers see only the scene) → drive it with the mod bar.</p>
      </div>
      <div class="help-block">
        <h3>📱 Camera-only apps (MeetMe, Instagram, etc.)</h3>
        <p><b>Two-device trick:</b> run Stream Warrior on a tablet, old phone or laptop and point your live camera at it for game moments. Prop it on a stand for tarot pulls.</p>
        <p><b>Soundboard anywhere:</b> play sounds out loud on a second device near your streaming phone's mic — instant production value, no screen share needed.</p>
        <p><b>No-screen games:</b> jokes, facts, pickup lines, 20 Questions and weird laws work with zero screen share — just read and narrate.</p>
      </div>
      <div class="help-block">
        <h3>💻 Desktop / OBS</h3>
        <p>Open Stream Warrior in your browser, press <b>Ctrl+F</b> for fullscreen, and share that window. Pro move: two windows — one fullscreen scene for viewers, one for controls.</p>
      </div>
      <div class="help-block">
        <h3>🔊 Audio notes</h3>
        <p><b>Android:</b> screen share can capture soundboard audio directly. <b>iPhone:</b> iOS blocks that — sounds play through the speaker and your mic picks them up.</p>
      </div>
      <div class="help-block">
        <h3>💡 Streamer tips</h3>
        <p>• Hide the mod bar (👁️ button) while shared — tap the top edge to bring it back.<br>
        • Wheel of Chaos kills dead air in one tap.<br>
        • ${name === "You" ? "Set your streamer name in ⚙️ settings" : "Good luck tonight, " + name + "!"}</p>
      </div>
    `);
  },

  /* ---------- settings ---------- */
  openSettings() {
    document.getElementById("settings-modal").classList.remove("hidden");
    this.renderCustomSounds();
  },

  async testAI() {
    const endpoint = document.getElementById("set-endpoint").value;
    const model = document.getElementById("set-model").value;
    const key = document.getElementById("set-key").value;
    AI.save(endpoint, model, key);
    const out = document.getElementById("ai-test-result");
    if (!AI.enabled) { out.textContent = "⚠️ endpoint + key required"; return; }
    out.textContent = "⏳ testing…";
    try {
      await AI.chat("Reply with exactly: OK", "ping", 10);
      out.textContent = "✅ connected";
      this.updateAIDot();
    } catch (e) { out.textContent = "❌ " + e.message; }
  },

  updateAIDot() {
    const dot = document.getElementById("ai-dot");
    dot.classList.toggle("on", AI.enabled);
    dot.title = AI.enabled ? "AI connected" : "AI off — tap to connect";
  },

  /* ---------- custom sounds ---------- */
  addSoundFile(e) {
    const file = e.target.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      const r = Sound.addCustom(file.name.replace(/\.[^.]+$/, "").slice(0, 12), reader.result);
      if (r === "max") this.toast("Max 12 custom sounds");
      else if (r === "big") this.toast("File too big — keep sounds short");
      else { this.toast("Added 🔊"); SoundboardUI.render(); this.renderCustomSounds(); }
      e.target.value = "";
    };
    reader.readAsDataURL(file);
  },

  renderCustomSounds() {
    const list = document.getElementById("custom-sounds-list");
    if (!list) return;
    Sound.loadCustom();
    list.innerHTML = Sound.custom.map((s, i) => `<div class="custom-sound-row"><button class="btn tiny" data-play="${i}">▶</button><span>${s.name}</span><button class="btn tiny danger" data-del="${i}">✕</button></div>`).join("") || '<p class="hint">No custom sounds yet.</p>';
    list.querySelectorAll("[data-play]").forEach(b => b.addEventListener("click", () => Sound.playCustom(parseInt(b.dataset.play))));
    list.querySelectorAll("[data-del]").forEach(b => b.addEventListener("click", () => { Sound.removeCustom(parseInt(b.dataset.del)); this.renderCustomSounds(); SoundboardUI.render(); }));
  }
};

/* ---------- Main page / home ---------- */
const Home = {
  el() { return document.getElementById("m-home"); },

  render() {
    const name = localStorage.getItem("sa_name");
    App.scene(`
      <div class="module" id="m-home">
        <div class="home-hero">
          <img src="icon.svg" alt="" class="home-logo">
          <h1>Stream<span>Warrior</span></h1>
          <p class="hint">Your streamer mod menu — ${name ? "welcome back, <b>" + name + "</b>" : "set your name in ⚙️ settings"}.</p>
          <span class="version-badge">${App.VERSION}</span>
        </div>
        <div class="home-grid">
          ${App.order.map(id => `
            <button class="home-card" data-open="${id}">
              <span class="home-ico">${App.meta[id].icon}</span>
              <span class="home-label">${App.meta[id].label}</span>
            </button>
          `).join("")}
          <button class="home-card" data-open="wheel">
            <span class="home-ico">🎡</span><span class="home-label">Chaos</span>
          </button>
          <a class="home-card" href="warrior-prophet/" style="text-decoration:none">
            <span class="home-ico">🙏</span><span class="home-label">Warrior Prophet</span>
          </a>
          <a class="home-card" href="phone-warrior/" style="text-decoration:none">
            <span class="home-ico">🛡️</span><span class="home-label">Phone Warrior</span>
          </a>
          <a class="home-card" href="ads-manager/?v=15" style="text-decoration:none">
          <a class="home-card" href="life-coach/?v=16" style="text-decoration:none">
            <span class="home-ico">🪖</span><span class="home-label">Life Coach</span>
          </a>
          <a class="home-card" href="wealth-builder/?v=17" style="text-decoration:none">
            <span class="home-ico">💰</span><span class="home-label">Wealth Builder</span>
          </a>
          <a class="home-card" href="viral-creator/?v=18" style="text-decoration:none">
            <span class="home-ico">📸</span><span class="home-label">Viral Creator</span>
          </a>
          <a class="home-card" href="academy/?v=19" style="text-decoration:none">
            <span class="home-ico">📚</span><span class="home-label">Warrior Academy</span>
          </a>
          <a class="home-card" href="family-center/?v=20" style="text-decoration:none">
            <span class="home-ico">🚸</span><span class="home-label">Family Center</span>
          </a>
          <a class="home-card" href="warrior-survival/?v=15" style="text-decoration:none">
            <span class="home-ico">⚔️</span><span class="home-label">Survival AI</span>
          </a>
            <span class="home-ico">📣</span><span class="home-label">Warrior Ads AI</span>
          </a>
          <a class="home-card" href="den/" style="text-decoration:none">
            <span class="home-ico">🔴</span><span class="home-label">Warrior's Den</span>
          </a>
          <a class="home-card" href="flamekeeper/?v=11" style="text-decoration:none">
            <span class="home-ico">📖</span><span class="home-label">Bible Flamekeeper</span>
          </a>
          <a class="home-card" href="ai-builder/?v=10" style="text-decoration:none">
            <span class="home-ico">🔥</span><span class="home-label">Warrior Forge</span>
          </a>
          <a class="home-card" href="weirdlaw/" style="text-decoration:none">
            <span class="home-ico">⚖️</span><span class="home-label">Weird Law or Fake?</span>
          </a>
        </div>
        <div class="row center gap">
          <button class="btn small ghost" id="home-help">📺 How to use on stream</button>
          <button class="btn small ghost" id="home-full">⛶ Fullscreen</button>
        </div>
        <p class="hint center" style="margin-top:14px">Set up your scene before you share. Hide the bar with 👁️. Tap the top edge to get it back.</p>
      </div>
    `, () => {
      document.querySelectorAll("[data-open]").forEach(b => b.addEventListener("click", () => App.openModule(b.dataset.open)));
      document.getElementById("home-help").addEventListener("click", () => App.openHelp());
      document.getElementById("home-full").addEventListener("click", () => App.toggleFullscreen());
    });
  }
};

/* ---------- Soundboard UI ---------- */
const SoundboardUI = {
  builtIn: [
    { id: "airhorn", label: "📢 Airhorn" }, { id: "drumroll", label: "🥁 Drumroll" },
    { id: "applause", label: "👏 Applause" }, { id: "trombone", label: "🎺 Sad Trombone" },
    { id: "buzzer", label: "❌ Buzzer" }, { id: "ding", label: "🔔 Ding" },
    { id: "laser", label: "🔫 Laser" }, { id: "whoosh", label: "💨 Whoosh" },
    { id: "boing", label: "🤸 Boing" }, { id: "heartbeat", label: "💓 Heartbeat" },
    { id: "tada", label: "🎉 Ta-da" }, { id: "crickets", label: "🦗 Crickets" },
    { id: "drum", label: "🪘 Drum Hit" }, { id: "rimshot", label: "🥁 Rimshot" }
  ],

  render() {
    Sound.loadCustom();
    App.scene(`
      <div class="module" id="m-soundboard">
        <div class="module-head"><h2>🔊 Soundboard</h2><span class="q-count">tap = play</span></div>
        <div class="sound-grid">
          ${this.builtIn.map(s => `<button class="sound-btn" data-sound="${s.id}">${s.label}</button>`).join("")}
        </div>
        ${Sound.custom.length ? `<h3 class="sub-head">Your sounds</h3><div class="sound-grid">${Sound.custom.map((s, i) => `<button class="sound-btn custom" data-custom="${i}">⭐ ${s.name}</button>`).join("")}</div>` : '<p class="hint center">Add your own sounds in ⚙️ settings</p>'}
      </div>
    `, () => {
      document.querySelectorAll("[data-sound]").forEach(b => b.addEventListener("click", () => Sound.play(b.dataset.sound)));
      document.querySelectorAll("[data-custom]").forEach(b => b.addEventListener("click", () => Sound.playCustom(parseInt(b.dataset.custom))));
    });
  }
};

document.addEventListener("DOMContentLoaded", () => App.init());
