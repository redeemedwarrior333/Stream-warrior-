/* Weird Law or Fake? — embeddable widget (v1)
   Everything client-side. Scores in localStorage under "wl_".
   IIFE wrapper so nothing leaks into the host page's global scope. */
(() => {
  "use strict";

  var LAWS = [
    { state: "Alaska", text: "It's illegal to push a live moose out of a moving airplane.", real: true, why: "Real — it's been on the books in Alaska since 1939. (How the FIRST guy got caught is the better question.)" },
    { state: "Alaska", text: "In Alaska it's illegal to ice-fish for penguins.", real: false, why: "Fake — penguins live at the South Pole. There are no penguins in Alaska, and it's not illegal to fish for nothing." },
    { state: "Alabama", text: "Wearing a fake mustache in church that causes laughter is illegal.", real: true, why: "Real — an old Alabama code. Church giggles were apparently a public menace." },
    { state: "Alabama", text: "It's illegal to park your spaceship on the state capitol lawn.", real: false, why: "Fake — Alabama jokes about UFOs, but there's no parking ban for them on the books." },
    { state: "Arizona", text: "Hunting camels is illegal.", real: true, why: "Real — left over from the U.S. Camel Corps experiments in the 1800s." },
    { state: "Arizona", text: "In Arizona, cactus theft is only a crime if the cactus bloomed that year.", real: false, why: "Fake — damaging or stealing a saguaro IS illegal, bloomed or not." },
    { state: "Arkansas", text: "It's technically illegal to mispronounce 'Arkansas'.", real: true, why: "Real-ish — an 1881 law declared the official pronunciation (AR-kan-saw). It's a pronunciation RULING more than a jailable offense." },
    { state: "Arkansas", text: "In Arkansas it's illegal to yell 'free pie' in a crowded diner.", real: false, why: "Fake — probably should be, though." },
    { state: "California", text: "Frogs that die during frog-jumping contests must be destroyed — eating them is illegal.", real: true, why: "Real — Calaveras County frog-jump rules. The frogs are competing athletes, not snacks." },
    { state: "California", text: "In California it's illegal to surf after dark without a headlamp.", real: false, why: "Fake — nighttime surfing is ill-advised, but no headlamp law exists." },
    { state: "Colorado", text: "Riding a horse while under the influence is illegal.", real: true, why: "Real — horses count as vehicles for DUI purposes in Colorado." },
    { state: "Colorado", text: "In Denver it's illegal to lend your vacuum cleaner to a neighbor's cat.", real: false, why: "Fake — neighbors get the vac, cats stay off the guest list entirely." },
    { state: "Connecticut", text: "A pickle is only officially a pickle if it bounces.", real: true, why: "Real — Connecticut food inspectors dropped pickles from a height to test if they were real pickles. Legend, but it's the famous story." },
    { state: "Connecticut", text: "In Connecticut it's illegal to whisper during fireworks.", real: false, why: "Fake — with fireworks going, nobody would hear you anyway." },
    { state: "Delaware", text: "It's illegal to get married on a dare.", real: true, why: "Real — Delaware annuls dare-marriages. Choose your truths, never your dares." },
    { state: "Florida", text: "It's illegal to sing in a public place while wearing a swimsuit.", real: true, why: "Real — an old Florida statute. Beach-town karaoke was getting out of hand, apparently." },
    { state: "Florida", text: "In Florida, alligators have the right of way in crosswalks.", real: false, why: "Fake — but the smarter move is giving them the right of way anyway." },
    { state: "Georgia", text: "It's illegal to eat fried chicken with a fork.", real: true, why: "Real — a 1961 Gainesville, Georgia 'consistency' ordinance. Southern fried chicken is a hands-only food by LAW." },
    { state: "Hawaii", text: "It's illegal to place coins in your ears.", real: true, why: "Real — old Hawaii statute. Blame a past coin shortage and very patient police." },
    { state: "Hawaii", text: "In Hawaii it's illegal to take a coconut off a beach without saying aloha to it.", real: false, why: "Fake — coconuts don't care, but the beach cleanup does." },
    { state: "Idaho", text: "It's illegal to give someone a box of candy heavier than 50 pounds.", real: true, why: "Real — Idaho candy-cap. Fifty pounds of candy is a structural hazard, folks." },
    { state: "Illinois", text: "It's illegal to give a lit cigar to a dog, cat or other pet.", real: true, why: "Real — it's in the Illinois code. Pets can't legally get cigars, even if THEY want one." },
    { state: "Illinois", text: "In Illinois it's illegal to ice-skate backwards in a library.", real: false, why: "Fake — libraries have their own way of handling that. Shhh means shhh." },
    { state: "Iowa", text: "One-armed piano players must perform for free.", real: true, why: "Real — old Iowa ordinance (next to never enforced, but it's REAL code history)." },
    { state: "Iowa", text: "In Iowa it's illegal to corn-dog anything except corn.", real: false, why: "Fake — deep-fried chaos remains fully legal in Iowa." },
    { state: "Kansas", text: "Catching fish with your bare hands is illegal.", real: true, why: "Real — 'noodling' catfish bare-handed is banned in Kansas (legal in some southern states)." },
    { state: "Kentucky", text: "It's illegal to carry an ice cream cone in your back pocket.", real: true, why: "Real — old anti-horse-theft trick: pocket ice cream lured someone else's horse away. Georgia had the same idea." },
    { state: "Louisiana", text: "It's illegal to gargle in public.", real: true, why: "Real-ish — old New Orleans ordinance, allegedly about public decency at the water fountain." },
    { state: "Maine", text: "It's illegal to keep Christmas decorations up after January 14.", real: true, why: "Real — old town ordinance. Ahem. Look at your neighbors' wreath in March. AH-HEM." },
    { state: "Maine", text: "In Maine it's illegal to misname a lobster in a contest.", real: false, why: "Fake — lobsters get named plenty in Maine, usually right before dinner." },
    { state: "Maryland", text: "It's illegal to take a lion to the movies.", real: true, why: "Real — old Baltimore code. The popcorn budget alone should have been the crime." },
    { state: "Massachusetts", text: "It's illegal to eat peanuts in church.", real: true, why: "Real — old Massachusetts law. Pews weren't built for shell debris." },
    { state: "Massachusetts", text: "In Boston it's illegal to walk backward on the Freedom Trail during September.", real: false, why: "Fake — history moves forward, and so must you." },
    { state: "Michigan", text: "It's illegal to tie an alligator to a fire hydrant.", real: true, why: "Real — Michigan law. It's practically a lifestyle rule in Detroit." },
    { state: "Minnesota", text: "It's illegal to cross state lines with a duck on your head.", real: true, why: "Real — famous old Minnesota code. The hat industry remains baffled." },
    { state: "Minnesota", text: "In Minnesota it's illegal to sled into another state without a passport.", real: false, why: "Fake — though the blizzard will absolutely stop and check you." },
    { state: "Missouri", text: "Bear wrestling matches are prohibited.", real: true, why: "Real — Missouri law. Apparently somebody tried it enough times to require legislation." },
    { state: "Missouri", text: "In Missouri it's illegal to honk at cows in Morse code.", real: false, why: "Fake — cows ignore both regular honks and coded ones." },
    { state: "Montana", text: "It's illegal to have a sheep in the cab of your truck without a chaperone.", real: true, why: "Real — old Montana code, aimed at exactly what you think it was aimed at." },
    { state: "Nebraska", text: "Whale fishing is illegal — there are no whales in Nebraska.", real: true, why: "Real-ish — Nebraska actually passed this. Nebraska is 1,000+ miles from any ocean. Zero whales have been caught, ever." },
    { state: "Nebraska", text: "In Nebraska it's illegal to grow corn without a permit from the corn.", real: false, why: "Fake — corn itself grants no permits. Ask any Nebraskan." },
    { state: "Nevada", text: "It's illegal to ride a camel on the highway.", real: true, why: "Real — Nevada code, from the same Camel Corps era as Arizona's." },
    { state: "New Hampshire", text: "It's illegal to sell the clothes off your back to pay a gambling debt.", real: true, why: "Real — old Granite State law. Live free or... keep 'em on." },
    { state: "New Jersey", text: "It's illegal to frown at a police officer.", real: true, why: "Real-ish — old Newark, NJ ordinance. Save the grumpy face for later." },
    { state: "New Jersey", text: "In New Jersey it's illegal for a dog to board a school bus during prom season.", real: false, why: "Fake — the dog would pass the dress code anyway." },
    { state: "Oklahoma", text: "It's illegal to have a sleeping donkey in your bathtub after 7 PM.", real: true, why: "Real-ish — old Oklahoma town code (the bathtub-donkey classic). Tubs are for rubber ducks." },
    { state: "Oklahoma", text: "In Oklahoma it's illegal to wrassle a tornado without a permit.", real: false, why: "Fake — though Oklahoma-based, the state wisely leaves tornadoes unregulated." },
    { state: "Texas", text: "It's illegal to shoot a buffalo from the second story of a hotel.", real: true, why: "Real — actual old Texas statute. You'll have to use the lobby, as before." },
    { state: "Texas", text: "In Texas it's illegal to wear cowboy boots in a swimming pool.", real: false, why: "Fake — the boots sink, but it's nobody's law." },
    { state: "Vermont", text: "It's illegal to whistle underwater.", real: true, why: "Real-ish — old Vermont code, allegedly to keep ponds at a reasonable volume." },
    { state: "Vermont", text: "In Vermont it's illegal to serve maple syrup in anything but a pitcher.", real: false, why: "Fake — Vermont will sell you syrup in a tractor tire if you want." },
    { state: "West Virginia", text: "It's illegal to snooze on a train, because it disturbs other passengers.", real: true, why: "Real — old WV statute. Nap responsibly." },
    { state: "Wyoming", text: "It's illegal to take a photograph of a rabbit from January to April without a permit.", real: false, why: "Fake — Wyoming rabbits remain documentable year-round." },
    { state: "Wyoming", text: "It's illegal to use firearms to take a picture of a wedding.", real: true, why: "Real-ish — an old Wyoming 'game fish' code wrinkle. In any case, photograph the bouquet instead." }
  ];

  var KEY = "wl_score", BEST = "wl_best", STREAK = "wl_beststreak";
  var box = document.getElementById("wl-widget");
  if (!box) return;
  box.className = "wl-container";

  var score = parseInt(localStorage.getItem(KEY) || "0", 10),
      best = parseInt(localStorage.getItem(BEST) || "0", 10),
      bestStreak = parseInt(localStorage.getItem(STREAK) || "0", 10),
      streak = 0, current = null, asked = [];

  function esc(s) {
    var d = document.createElement("div");
    d.textContent = s;
    return d.innerHTML;
  }

  function render(html) {
    box.innerHTML = html;
  }

  function statsLine() {
    return '<div class="wl-stats">Score <b>' + score + '</b> · Streak <b>' + streak +
      '</b> · Best <b>' + best + '</b></div>';
  }

  function newRound() {
    if (asked.length >= LAWS.length) asked = [];
    var pick;
    do { pick = LAWS[Math.floor(Math.random() * LAWS.length)]; } while (asked.indexOf(pick) !== -1);
    asked.push(pick);
    current = pick;
    render(
      '<div class="wl-head"><span class="wl-title">⚖️ Weird Law or Fake?</span>' + statsLine() + '</div>' +
      '<div class="wl-card"><span class="wl-state">' + esc(pick.state) + ':</span> ' + esc(pick.text) + '</div>' +
      '<div class="wl-btns">' +
        '<button class="wl-btn" id="wl-true">REAL</button>' +
        '<button class="wl-btn ghost" id="wl-false">FAKE</button>' +
      '</div>' +
      '<div class="wl-foot">by <a href="https://redeemedwarrior333.github.io/Stream-warrior-/" target="_blank" rel="noopener">Redeemed Warrior</a> — more games on the site</div>'
    );
    document.getElementById("wl-true").onclick = function () { check(true); };
    document.getElementById("wl-false").onclick = function () { check(false); };
  }

  function save() {
    localStorage.setItem(KEY, score);
    if (score > best) { best = score; localStorage.setItem(BEST, best); }
    if (streak > bestStreak) { bestStreak = streak; localStorage.setItem(STREAK, bestStreak); }
  }

  function check(guessReal) {
    if (!current) return;
    var right = guessReal === current.real;
    if (right) { score++; streak++; } else { score = Math.max(0, score - 1); streak = 0; }
    save();
    render(
      '<div class="wl-head"><span class="wl-title">⚖️ Weird Law or Fake?</span>' + statsLine() + '</div>' +
      '<div class="wl-card ' + (right ? "gold" : "danger") + '">' +
        '<div class="wl-verdict ' + (right ? "win" : "lose") + '">' +
          (right ? "✅ Spot on!" : "❌ Not this time.") + ' It was <b>' + (current.real ? "REAL" : "FAKE") + '</b>' +
        '</div>' +
        '<div class="wl-why">' + esc(current.why) + '</div>' +
      '</div>' +
      '<div class="wl-btns"><button class="wl-btn" id="wl-next">Next law →</button></div>'
    );
    current = null;
    document.getElementById("wl-next").onclick = newRound;
  }

  newRound();
})();
