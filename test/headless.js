const { JSDOM } = require("jsdom");
const fs = require("fs");
const root = require("path").resolve(__dirname, "..");

const dom = new JSDOM(fs.readFileSync(root + "/index.html", "utf8"), {
  url: "https://example.com/",
  runScripts: "outside-only",
  pretendToBeVisual: true
});
const { window } = dom;
const errors = [];
window.addEventListener("error", e => errors.push("WINDOW ERROR: " + e.message));
window.fetch = () => Promise.reject(new Error("no network"));

const scripts = ["js/data.js","js/ai.js","js/sound.js","js/tarot.js","js/games.js","js/laws.js","js/app.js"];
const combined = scripts.map(s => fs.readFileSync(root + "/" + s, "utf8")).join("\n;\n");
try { window.eval(combined); } catch (e) { errors.push("SCRIPT ERROR: " + e.message); }

const evt = new window.Event("DOMContentLoaded", { bubbles: true });
try { window.document.dispatchEvent(evt); } catch (e) { errors.push("INIT ERROR: " + e.stack.split("\n").slice(0,3).join(" | ")); }

setTimeout(() => {
  const doc = window.document;
  console.log("=== RESULTS (browser-faithful) ===");
  console.log("errors:", errors.length ? errors.join("\n") : "none");
  console.log("scene has home:", doc.getElementById("scene").innerHTML.includes("m-home"));
  console.log("home cards:", doc.querySelectorAll(".home-card").length);
  console.log("mod buttons:", doc.querySelectorAll(".mod-btn").length);
  let clickErrors = [];
  doc.querySelectorAll(".home-card").forEach(c => {
    try { c.click(); } catch (e) { clickErrors.push(c.dataset.open + ": " + e.message); }
  });
  console.log("module click errors:", clickErrors.length ? clickErrors.join(" | ") : "none");
  try { doc.querySelector(".home-btn").click(); console.log("home button: OK"); }
  catch (e) { console.log("home button ERROR:", e.message); }
  try { doc.getElementById("btn-help").click(); console.log("help modal: OK"); } catch (e) { console.log("help ERROR:", e.message); }
  try { doc.getElementById("btn-settings").click(); console.log("settings modal: OK"); } catch (e) { console.log("settings ERROR:", e.message); }
  process.exit(0);
}, 100);
