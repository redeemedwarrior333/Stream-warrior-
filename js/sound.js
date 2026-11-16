/* ============ Soundboard: WebAudio synth + custom sounds ============ */
"use strict";

const Sound = {
  ctx: null,
  custom: [],   // {name, dataUrl}

  init() {
    if (!this.ctx) {
      try { this.ctx = new (window.AudioContext || window.webkitAudioContext)(); } catch (e) {}
    }
    if (this.ctx && this.ctx.state === "suspended") this.ctx.resume();
  },

  loadCustom() {
    try { this.custom = JSON.parse(localStorage.getItem("sa_custom_sounds") || "[]"); } catch (e) { this.custom = []; }
    return this.custom;
  },

  saveCustom() { localStorage.setItem("sa_custom_sounds", JSON.stringify(this.custom)); },

  addCustom(name, dataUrl) {
    this.loadCustom();
    if (this.custom.length >= 12) return "max";
    if (dataUrl.length > 700000) return "big";
    this.custom.push({ name, dataUrl });
    this.saveCustom();
    return "ok";
  },

  removeCustom(i) { this.loadCustom(); this.custom.splice(i, 1); this.saveCustom(); },

  playCustom(i) {
    this.loadCustom();
    const s = this.custom[i]; if (!s) return;
    let el = document.getElementById("cst_" + i);
    if (!el) { el = document.createElement("audio"); el.id = "cst_" + i; el.src = s.dataUrl; document.body.appendChild(el); }
    el.currentTime = 0;
    el.play().catch(() => {});
  },

  /* ---- synthesized sounds ---- */
  env(gainNode, t0, attack, decay, peak = 1) {
    const g = gainNode.gain;
    g.setValueAtTime(0.0001, t0);
    g.exponentialRampToValueAtTime(peak, t0 + attack);
    g.exponentialRampToValueAtTime(0.0001, t0 + attack + decay);
  },

  tone(freq, type, t0, dur, peak = 0.5, slideTo = null) {
    const c = this.ctx;
    const o = c.createOscillator(), g = c.createGain();
    o.type = type; o.frequency.setValueAtTime(freq, t0);
    if (slideTo) o.frequency.exponentialRampToValueAtTime(slideTo, t0 + dur);
    this.env(g, t0, 0.01, dur, peak);
    o.connect(g).connect(c.destination);
    o.start(t0); o.stop(t0 + dur + 0.1);
  },

  noise(t0, dur, peak = 0.4, filterFreq = 1200, filterType = "lowpass") {
    const c = this.ctx;
    const len = Math.max(1, Math.floor(c.sampleRate * dur));
    const buf = c.createBuffer(1, len, c.sampleRate);
    const d = buf.getChannelData(0);
    for (let i = 0; i < len; i++) d[i] = Math.random() * 2 - 1;
    const src = c.createBufferSource(); src.buffer = buf;
    const f = c.createBiquadFilter(); f.type = filterType; f.frequency.value = filterFreq;
    const g = c.createGain(); this.env(g, t0, 0.01, dur, peak);
    src.connect(f).connect(g).connect(c.destination);
    src.start(t0);
  },

  play(name) {
    this.init();
    if (!this.ctx) return;
    const c = this.ctx, t = c.currentTime + 0.02;
    switch (name) {
      case "airhorn":
        [233, 466, 587].forEach(f => this.tone(f, "sawtooth", t, 0.9, 0.25));
        this.tone(349, "square", t, 0.9, 0.15);
        break;
      case "drumroll":
        for (let i = 0; i < 40; i++) this.noise(t + i * 0.045, 0.04, 0.25, 900);
        this.tone(80, "sine", t + 1.8, 0.4, 0.6, 40);
        break;
      case "applause":
        for (let i = 0; i < 90; i++) this.noise(t + Math.random() * 2.2, 0.03, 0.12, 2500, "highpass");
        break;
      case "trombone":
        this.tone(196, "sawtooth", t, 0.35, 0.3, 185);
        this.tone(185, "sawtooth", t + 0.35, 0.35, 0.3, 165);
        this.tone(165, "sawtooth", t + 0.7, 0.5, 0.3, 110);
        this.tone(110, "sawtooth", t + 1.2, 0.8, 0.3, 82);
        break;
      case "buzzer":
        this.tone(110, "square", t, 0.7, 0.35);
        this.tone(104, "square", t, 0.7, 0.25);
        break;
      case "ding":
        this.tone(1319, "sine", t, 0.5, 0.4);
        this.tone(1760, "sine", t + 0.02, 0.5, 0.25);
        break;
      case "laser":
        this.tone(1800, "sawtooth", t, 0.35, 0.3, 90);
        break;
      case "whoosh":
        this.noise(t, 0.5, 0.35, 600);
        this.noise(t + 0.1, 0.4, 0.25, 300);
        break;
      case "boing":
        this.tone(400, "sine", t, 0.5, 0.4, 80);
        break;
      case "heartbeat":
        this.tone(60, "sine", t, 0.12, 0.8, 40);
        this.tone(60, "sine", t + 0.28, 0.12, 0.7, 40);
        break;
      case "tada":
        [523, 659, 784, 1047].forEach((f, i) => this.tone(f, "triangle", t + i * 0.09, 0.4, 0.35));
        break;
      case "crickets":
        for (let i = 0; i < 6; i++) { this.tone(4200, "sine", t + i * 0.3, 0.05, 0.15); this.tone(4200, "sine", t + i * 0.3 + 0.08, 0.05, 0.15); }
        break;
      case "drum":
        this.tone(150, "sine", t, 0.25, 0.8, 50);
        this.noise(t, 0.08, 0.3, 400);
        break;
      case "rimshot":
        this.noise(t, 0.05, 0.5, 3000, "highpass");
        this.tone(180, "sine", t + 0.35, 0.3, 0.7, 60);
        break;
    }
  }
};
