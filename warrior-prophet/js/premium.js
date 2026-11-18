/* Warrior Prophet — premium unlock */
"use strict";
var MW_PREMIUM = (function () {
  var CODES = ["MW-FOUNDER", "PROPHET-777", "ANGEL-1111"];

  function isPremium() { return localStorage.getItem("wp_premium") === "1"; }

  function unlock(code) {
    var c = (code || "").trim().toUpperCase();
    if (CODES.indexOf(c) !== -1) {
      localStorage.setItem("wp_premium", "1");
      return true;
    }
    return false;
  }

  function lock() { localStorage.removeItem("wp_premium"); }

  return { isPremium: isPremium, unlock: unlock, lock: lock };
})();
