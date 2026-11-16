/* ============ Weird Laws by State ============ */
"use strict";

/* Allegedly real laws — fun for stream content, not legal advice 😄 */
const WEIRD_LAWS = [
  { s: "Alabama", laws: ["Wearing a fake mustache in church that causes laughter is illegal.", "It's illegal to drive while blindfolded."] },
  { s: "Alaska", laws: ["It's illegal to push a live moose out of a moving airplane.", "It's illegal to wake a sleeping bear just to take a photo."] },
  { s: "Arizona", laws: ["Hunting camels is illegal.", "Donkeys are not allowed to sleep in bathtubs."] },
  { s: "Arkansas", laws: ["It's technically illegal to mispronounce 'Arkansas'.", "The Arkansas River can't be pronounced wrong either — same law."] },
  { s: "California", laws: ["Frogs that die during frog-jumping contests must be destroyed — eating them is illegal.", "It's illegal to whistle for a lost canary before 7 AM."] },
  { s: "Colorado", laws: ["Riding a horse while under the influence is illegal.", "It's illegal to drive a black car on Sundays in some towns."] },
  { s: "Connecticut", laws: ["A pickle is only officially a pickle if it bounces.", "It's illegal to walk across a street on your hands."] },
  { s: "Delaware", laws: ["It's illegal to get married on a dare.", "An old code made it illegal to wear pants that are 'form fitting' around the waist."] },
  { s: "Florida", laws: ["It's illegal to sing in a public place while wearing a swimsuit.", "Unmarried women can't parachute on Sundays."] },
  { s: "Georgia", laws: ["It's illegal to eat fried chicken with a fork.", "It's illegal to tie a giraffe to a telephone pole."] },
  { s: "Hawaii", laws: ["It's illegal to place coins in your ears.", "It used to be illegal to annoy birds in city parks."] },
  { s: "Idaho", laws: ["It's illegal to give someone a box of candy heavier than 50 pounds.", "Fishing from the back of a camel is illegal."] },
  { s: "Illinois", laws: ["It's illegal to give a lit cigar to a dog, cat or other pet.", "In Chicago, it's illegal to fish while sitting on a giraffe's neck."] },
  { s: "Indiana", laws: ["It's illegal to sell cars on Sundays.", "It's illegal to take a bath between October and March in old state codes."] },
  { s: "Iowa", laws: ["One-armed piano players must perform for free.", "Kisses may last no more than five minutes."] },
  { s: "Kansas", laws: ["Catching fish with your bare hands is illegal.", "It's illegal to serve wine in teacups."] },
  { s: "Kentucky", laws: ["It's illegal to carry an ice cream cone in your back pocket.", "There was a law requiring citizens to bathe at least once a year."] },
  { s: "Louisiana", laws: ["It's illegal to gargle in public.", "Snoring is prohibited unless all bedroom windows are closed."] },
  { s: "Maine", laws: ["It's illegal to keep Christmas decorations up after January 14.", "It's illegal to step out of an airplane while it's flying."] },
  { s: "Maryland", laws: ["It's illegal to take a lion to the movies.", "It's illegal to swear from inside a moving vehicle in some cities."] },
  { s: "Massachusetts", laws: ["It's illegal to eat peanuts in church.", "It's illegal to deface a milk carton."] },
  { s: "Michigan", laws: ["It's illegal to tie an alligator to a fire hydrant.", "An old law stated a woman's hair legally belonged to her husband."] },
  { s: "Minnesota", laws: ["It's illegal to cross state lines with a duck on your head.", "It's illegal to sleep naked in some towns."] },
  { s: "Mississippi", laws: ["It's illegal to teach others what polygamy is.", "It's illegal to use skunks as pets in some old codes."] },
  { s: "Missouri", laws: ["Bear wrestling matches are prohibited.", "It's illegal to frighten a baby for fun."] },
  { s: "Montana", laws: ["It's illegal to have a sheep in the cab of your truck without a chaperone.", "It's illegal to drive with a sheep in your view for the same reason."] },
  { s: "Nebraska", laws: ["Whale fishing is illegal — there are no whales in Nebraska.", "Bar owners can't sell beer unless they're also brewing soup at the same time."] },
  { s: "Nevada", laws: ["It's illegal to ride a camel on the highway.", "An old town code banned men with mustaches from kissing women."] },
  { s: "New Hampshire", laws: ["It's illegal to sell the clothes off your back to pay a gambling debt.", "It's illegal to collect seaweed at night."] },
  { s: "New Jersey", laws: ["It's illegal to frown at a police officer.", "It's illegal to wear a bulletproof vest while committing a crime."] },
  { s: "New Mexico", laws: ["It's illegal to dance while wearing a sombrero in some towns.", "It's illegal to carry a lunchbox down Main Street in some old codes."] },
  { s: "New York", laws: ["It's illegal to greet someone by putting your thumb on your nose and wiggling your fingers.", "It's illegal to honk your horn for any reason other than an emergency."] },
  { s: "North Carolina", laws: ["It's technically illegal to sing off-key.", "It's illegal to use elephants to plow cotton fields."] },
  { s: "North Dakota", laws: ["It's illegal to fall asleep with your shoes on.", "It's illegal to serve beer and pretzels at the same time."] },
  { s: "Ohio", laws: ["It's illegal to get a fish drunk.", "In Youngstown, it's illegal to run out of gas."] },
  { s: "Oklahoma", laws: ["It's illegal to make ugly faces at a dog.", "Whale hunting is illegal — also no whales in Oklahoma."] },
  { s: "Oregon", laws: ["It used to be illegal to pump your own gas.", "It's illegal to whisper in someone's ear while they're hunting? — that's the legend, anyway."] },
  { s: "Pennsylvania", laws: ["It's illegal to sing in the bathtub.", "It's illegal to tie a dollar bill to a string and yank it away when someone tries to grab it."] },
  { s: "Rhode Island", laws: ["It's illegal to throw pickle juice on a trolley.", "It was illegal to sell a toothbrush and toothpaste to the same customer on Sundays."] },
  { s: "South Carolina", laws: ["It's illegal to keep a horse in a bathtub.", "It used to be illegal to play pinball if you were under 18."] },
  { s: "South Dakota", laws: ["It's illegal to fall asleep in a cheese factory.", "It's illegal to show movies in which police officers are shot at."] },
  { s: "Tennessee", laws: ["It's illegal to share your Netflix password — an actual 2011 law.", "It's illegal to shoot any game other than whales from a moving car."] },
  { s: "Texas", laws: ["It's illegal to milk another person's cow.", "It's illegal to sell your own eye."] },
  { s: "Utah", laws: ["It's illegal to walk down the street carrying a paper bag with a violin in it.", "It's illegal to fish from horseback."] },
  { s: "Vermont", laws: ["It's illegal to whistle underwater.", "Women once needed written permission to wear false teeth."] },
  { s: "Virginia", laws: ["It's illegal to tickle a woman's chin with a feather duster.", "In Richmond, it's illegal to flip a coin to decide who pays for coffee."] },
  { s: "Washington", laws: ["It's illegal to pretend your parents are rich.", "It's illegal to buy a mattress on Sundays in some old codes."] },
  { s: "West Virginia", laws: ["It's illegal to snooze on a train.", "It's illegal for children to attend school with breath smelling of wild onions."] },
  { s: "Wisconsin", laws: ["Public restaurants must serve apple pie with cheese.", "It's illegal to serve margarine instead of butter in some old codes."] },
  { s: "Wyoming", laws: ["It's illegal to take a photograph of a rabbit from January to April.", "It's illegal to use a firearm to catch fish."] }
];

const WeirdLaws = {
  state: null, idx: -1,

  el() { return document.getElementById("m-laws"); },

  render() {
    const st = this.state ? WEIRD_LAWS.find(x => x.s === this.state) : null;
    App.scene(`
      <div class="module" id="m-laws">
        <div class="module-head"><h2>⚖️ Weird Laws</h2><span class="q-count">real-ish 😄</span></div>
        ${st ? `
          <div class="state-tag">${st.s}</div>
          <div class="question-card big-text"><p>${st.laws[this.idx >= 0 ? this.idx : 0]}</p></div>
          <div class="row center gap">
            <button class="btn" id="law-next">Another Law ▶</button>
            <button class="btn small" id="law-rand">🎲 Random State</button>
            <button class="btn small ghost" id="law-back">Change State</button>
          </div>
        ` : `
          <p class="hint center">Pick a state — read the law, let chat guess if it's real.</p>
          <div class="state-grid">
            ${WEIRD_LAWS.map(x => `<button class="state-btn" data-state="${x.s}">${x.s}</button>`).join("")}
          </div>
          <div class="row center"><button class="btn small" id="law-rand">🎲 Random State</button></div>
        `}
      </div>
    `, () => {
      const root = this.el();
      root.querySelectorAll("[data-state]").forEach(b => b.addEventListener("click", () => {
        this.state = b.dataset.state; this.idx = 0; Sound.play("ding"); this.render();
      }));
      const nx = root.querySelector("#law-next"); if (nx) nx.addEventListener("click", () => {
        const st2 = WEIRD_LAWS.find(x => x.s === this.state);
        this.idx = (this.idx + 1) % st2.laws.length;
        Sound.play("boing"); this.render();
      });
      const rd = root.querySelector("#law-rand"); if (rd) rd.addEventListener("click", () => {
        this.state = WEIRD_LAWS[Math.floor(Math.random() * WEIRD_LAWS.length)].s;
        this.idx = Math.floor(Math.random() * 2);
        Sound.play("whoosh"); this.render();
      });
      const bk = root.querySelector("#law-back"); if (bk) bk.addEventListener("click", () => { this.state = null; this.render(); });
    });
  }
};
