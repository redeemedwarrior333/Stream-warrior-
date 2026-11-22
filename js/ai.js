/* ============ Optional AI connection (OpenAI-compatible) ============ */
"use strict";

const AI = {
  config: null,
  enabled: false,
  TIMEOUT: 25000,

  PRESETS: [
    { label: "Small",  model: "mistral-small-latest"  },
    { label: "Medium", model: "mistral-medium-latest" },
    { label: "Large",  model: "mistral-large-latest"  }
  ],

  load() {
    try {
      const raw = localStorage.getItem("sa_ai_config");
      this.config = raw ? JSON.parse(raw) : null;
      this.enabled = !!(this.config && this.config.key && this.config.endpoint);
    } catch (e) { this.config = null; this.enabled = false; }
    return this.enabled;
  },

  save(endpoint, model, key) {
    this.config = { endpoint: (endpoint || "").trim().replace(/\/+$/, ""), model: (model || "").trim(), key: (key || "").trim() };
    this.enabled = !!(this.config.key && this.config.endpoint);
    try { localStorage.setItem("sa_ai_config", JSON.stringify(this.config)); } catch (e) {}
    App.updateAIDot();
  },

  /* Short, user-facing reason string for a failed AI call */
  errText(e) {
    const m = (e && e.message) || String(e);
    if (m.indexOf("AI not connected") >= 0) return "not connected (settings)";
    if (m.indexOf("timed out") >= 0) return "timed out, try again";
    if (m.indexOf("bad API key") >= 0) return "bad API key";
    if (m.indexOf("not found") >= 0) return "wrong URL or model name";
    if (m.indexOf("rate limited") >= 0) return "rate limited, wait a bit";
    return m;
  },

  describeStatus(res) {
    if (res.status === 401 || res.status === 403) return new Error("bad API key");
    if (res.status === 404) return new Error("endpoint or model not found");
    if (res.status === 429) return new Error("rate limited");
    return new Error("AI server error " + res.status);
  },

  async chat(system, user, maxTokens = 400, opts = {}) {
    if (!this.enabled) throw new Error("AI not connected");
    const url = this.config.endpoint.replace(/\/+$/, "") + "/chat/completions";
    const body = JSON.stringify({
      model: this.config.model || "mistral-small-latest",
      messages: [{ role: "system", content: system }, { role: "user", content: user }],
      max_tokens: maxTokens,
      temperature: 0.9,
      ...(opts.stream ? { stream: true } : {})
    });

    const once = async () => {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), this.TIMEOUT);
      try {
        const res = await fetch(url, {
          method: "POST",
          headers: { "Content-Type": "application/json", "Authorization": "Bearer " + this.config.key },
          body: body,
          signal: controller.signal
        });
        if (!res.ok) throw this.describeStatus(res);
        const ctype = res.headers.get("content-type") || "";
        if (opts.stream && ctype.indexOf("event-stream") >= 0) {
          return await this.readStream(res, opts.onToken);
        }
        const data = await res.json();
        return (data.choices && data.choices[0] && data.choices[0].message.content || "").trim();
      } catch (e) {
        if (e.name === "AbortError") throw new Error("AI request timed out");
        throw e;
      } finally {
        clearTimeout(timer);
      }
    };

    /* Streaming calls go straight through; plain calls get one quiet retry */
    if (opts.stream || opts.noRetry) return once();
    try { return await once(); }
    catch (e) {
      const m = (e && e.message) || "";
      if (m.indexOf("timed out") >= 0 || m.indexOf("server error") >= 0 || m.indexOf("rate limited") >= 0 || m.indexOf("Failed to fetch") >= 0) {
        return await once();
      }
      throw e;
    }
  },

  async readStream(res, onToken) {
    const reader = res.body.getReader();
    const dec = new TextDecoder();
    let buf = "", full = "";
    for (;;) {
      const r = await reader.read();
      if (r.done) break;
      buf += dec.decode(r.value, { stream: true });
      const lines = buf.split("\n");
      buf = lines.pop();
      for (const line of lines) {
        const s = line.trim();
        if (s.indexOf("data:") !== 0) continue;
        const payload = s.slice(5).trim();
        if (payload === "[DONE]") continue;
        try {
          const j = JSON.parse(payload);
          const ch = j.choices && j.choices[0];
          const t = (ch && ((ch.delta && ch.delta.content) || ch.text)) || "";
          if (t) { full += t; if (onToken) onToken(t); }
        } catch (e) { /* partial line, ignore */ }
      }
    }
    return full.trim();
  },

  /* ---------- tiny local cache so AI content survives reloads ---------- */
  cacheGet(key) {
    try { return JSON.parse(localStorage.getItem("sa_ai_cache_" + key) || "null"); } catch (e) { return null; }
  },
  cacheSet(key, val) {
    try { localStorage.setItem("sa_ai_cache_" + key, JSON.stringify(val)); } catch (e) {}
  },

  /* Ask AI for a list of items. Returns array of strings or trivia objects (or throws). */
  async list(kind, count = 10, extra = "") {
    const prompts = {
      jokes: "family-friendly short jokes, one per line, no numbering",
      lines: "cheesy pickup lines, one per line, no numbering",
      facts: "surprising true fun facts, one per line, no numbering",
      wyr: "would-you-rather questions, format 'A or B', one per line, no numbering",
      trivia: `trivia questions as JSON array of objects {"c":"category","q":"question","a":["correct","wrong","wrong","wrong"]}, ${extra}`
    };
    const sys = "You generate live-stream party game content. Output ONLY the requested items, nothing else.";
    const text = await this.chat(sys, `Generate ${count} ${prompts[kind] || kind}. ${extra}`, 900);
    if (kind === "trivia") {
      const m = text.match(/\[[\s\S]*\]/);
      if (!m) throw new Error("AI sent a bad format, try again");
      let arr;
      try { arr = JSON.parse(m[0]); } catch (e) { throw new Error("AI sent a bad format, try again"); }
      return arr.filter(t => t && t.q && Array.isArray(t.a)).map(t => ({ c: t.c || "AI", q: t.q, a: t.a.slice(0, 4), k: 0, ai: true }));
    }
    return text.split("\n").map(s => s.replace(/^[\s\d.*\-•]+/, "").trim()).filter(s => s.length > 8).map(s => ({ text: s, ai: true }));
  }
};
