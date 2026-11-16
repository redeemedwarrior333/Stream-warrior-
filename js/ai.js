/* ============ Optional AI connection (OpenAI-compatible) ============ */
"use strict";

const AI = {
  config: null,
  enabled: false,

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
    localStorage.setItem("sa_ai_config", JSON.stringify(this.config));
    App.updateAIDot();
  },

  async chat(system, user, maxTokens = 400) {
    if (!this.enabled) throw new Error("AI not connected");
    const url = this.config.endpoint.replace(/\/+$/, "") + "/chat/completions";
    const res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json", "Authorization": "Bearer " + this.config.key },
      body: JSON.stringify({
        model: this.config.model || "mistral-small-latest",
        messages: [{ role: "system", content: system }, { role: "user", content: user }],
        max_tokens: maxTokens,
        temperature: 0.9
      })
    });
    if (!res.ok) throw new Error("AI error " + res.status);
    const data = await res.json();
    return (data.choices && data.choices[0] && data.choices[0].message.content || "").trim();
  },

  /* Ask AI for a JSON array of items. Returns array of strings (or throws). */
  async list(kind, count = 10, extra = "") {
    const prompts = {
      jokes: "family-friendly short jokes, one per line, no numbering",
      lines: "cheesy pickup lines, one per line, no numbering",
      facts: "surprising true fun facts, one per line, no numbering",
      wyr: "would-you-rather questions, format 'A — or — B', one per line, no numbering",
      trivia: `trivia questions as JSON array of objects {"c":"category","q":"question","a":["correct","wrong","wrong","wrong"]}, ${extra}`
    };
    const sys = "You generate live-stream party game content. Output ONLY the requested items, nothing else.";
    const text = await this.chat(sys, `Generate ${count} ${prompts[kind] || kind}. ${extra}`, 900);
    if (kind === "trivia") {
      const m = text.match(/\[[\s\S]*\]/);
      const arr = JSON.parse(m[0]);
      return arr.map(t => ({ c: t.c || "AI", q: t.q, a: t.a.slice(0, 4), k: 0, ai: true }));
    }
    return text.split("\n").map(s => s.replace(/^[\s\d.*\-•]+/, "").trim()).filter(s => s.length > 8).map(s => ({ text: s, ai: true }));
  }
};
