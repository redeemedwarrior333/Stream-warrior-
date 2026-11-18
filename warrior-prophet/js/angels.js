/* Warrior Prophet — angel numbers */
"use strict";
var ANGELS_NUM = (function () {
  var NUMBERS = [
    { n:"000", title:"New Beginning", m:"Pure potential. Before the first day there was God — and He is starting something fresh with you. Pray big; nothing here limits Him.", v:"In the beginning God created the heavens and the earth.", r:"Genesis 1:1" },
    { n:"111", title:"Alignment", m:"Your thoughts are planting fast right now. Think on what is true, noble and lovely — you are setting the direction of the season.", v:"Whatever is true, whatever is noble, think about such things.", r:"Philippians 4:8" },
    { n:"1111", title:"Gateway", m:"An open door no one can shut. Stay prayerful and awake — the opportunity is timed, not random.", v:"See, I have placed before you an open door that no one can shut.", r:"Revelation 3:8" },
    { n:"222", title:"Partnership", m:"You are not walking alone. Trust the timing, lean on your people, and let the covenant relationships carry weight today.", v:"Two are better than one... if either of them falls, one can help the other up.", r:"Ecclesiastes 4:9-10" },
    { n:"333", title:"Trinity", m:"Body, mind and spirit are being covered. The God who is three-in-one is present in all three rooms of your life right now.", v:"May the grace of the Lord Jesus Christ, and the love of God, and the fellowship of the Holy Spirit be with you all.", r:"2 Corinthians 13:14" },
    { n:"444", title:"Angels Near", m:"Protection and foundation. The angels are encamped around you — build on the rock, not the sand, and the storm loses.", v:"He will command His angels concerning you to guard you in all your ways.", r:"Psalm 91:11" },
    { n:"555", title:"Change Is Coming", m:"A shift is in motion. Do not grip the old thing — God goes before you into the new one.", v:"See, I am doing a new thing! Now it springs up; do you not perceive it?", r:"Isaiah 43:19" },
    { n:"666", title:"Reset", m:"Not doom — a warning light on the dashboard. Refocus on spirit over stuff, and let the anxiety about material things go.", v:"You cannot serve both God and money.", r:"Matthew 6:24" },
    { n:"777", title:"Divine Yes", m:"You are on the right path. This is a heaven-wink: keep going, rest well, and receive what you have prayed for.", v:"Ask and it will be given to you; seek and you will find.", r:"Matthew 7:7" },
    { n:"888", title:"Abundance", m:"A season of provision and release. What flows to you is meant to flow through you — generosity keeps the river open.", v:"And my God will meet all your needs according to the riches of His glory in Christ Jesus.", r:"Philippians 4:19" },
    { n:"999", title:"Completion", m:"A chapter is closing. Close it with gratitude, not grief — endings are how God makes room for the assignment ahead.", v:"There is a time for everything, and a season for every activity under the heavens.", r:"Ecclesiastes 3:1" },
    { n:"1010", title:"Wake-Up Call", m:"Divine timing is in motion. You were born for such a time as this — stop shrinking from the moment.", v:"And who knows but that you have come to your royal position for such a time as this?", r:"Esther 4:14" },
    { n:"1212", title:"Keep The Faith", m:"The answer is closer than it appears. Faith is the evidence of things not seen — hold on one more stretch.", v:"Now faith is confidence in what we hope for and assurance about what we do not see.", r:"Hebrews 11:1" },
    { n:"911", title:"Help Is Coming", m:"Cry out and be heard. This number is an emergency line to heaven — and the line is never busy.", v:"The righteous cry out, and the Lord hears them; He delivers them from all their troubles.", r:"Psalm 34:17" },
    { n:"1234", title:"Step By Step", m:"Progress is holy. Stop measuring the mountain; take the next step, and let God order the route.", v:"In their hearts humans plan their course, but the Lord establishes their steps.", r:"Proverbs 16:9" },
    { n:"2222", title:"Double Rest", m:"Stop striving. Be still and know — the outcome does not depend on your anxiety, and never did.", v:"Be still, and know that I am God.", r:"Psalm 46:10" }
  ];

  var DIGITS = [
    { d:1, m:"1 — Unity and beginnings. One God, one door, one next step. Start the thing.", v:"Hear, O Israel: The Lord our God, the Lord is one.", r:"Deuteronomy 6:4" },
    { d:2, m:"2 — Partnership and witness. You are not meant to carry this alone; covenant and community are the answer.", v:"It is not good for the man to be alone.", r:"Genesis 2:18" },
    { d:3, m:"3 — The Trinity and resurrection. On the third day, everything changed. Your third-day is coming.", v:"He was raised on the third day according to the Scriptures.", r:"1 Corinthians 15:4" },
    { d:4, m:"4 — Creation and foundation. Seasons, directions, corners of the altar. Build on solid ground.", v:"He set the earth on its foundations; it can never be moved.", r:"Psalm 104:5" },
    { d:5, m:"5 — Grace. Five loaves fed thousands. What you have is enough when God blesses it.", v:"My grace is sufficient for you, for My power is made perfect in weakness.", r:"2 Corinthians 12:9" },
    { d:6, m:"6 — Humanity and home. Created on the sixth day, created for family. Tend your house and your heart.", v:"As for me and my house, we will serve the Lord.", r:"Joshua 24:15" },
    { d:7, m:"7 — Completion and rest. God rested on the seventh — not from weakness, but from winning. Rest like that.", v:"On the seventh day God rested from all His work.", r:"Genesis 2:2" },
    { d:8, m:"8 — New beginnings. Eight survived the flood; eight is the first day of the new week. Resurrection energy.", v:"If anyone is in Christ, the new creation has come: the old has gone, the new is here!", r:"2 Corinthians 5:17" },
    { d:9, m:"9 — Fruit and harvest. Nine fruits of the Spirit. The harvest is ripening — stay faithful in the field.", v:"Let us not become weary in doing good, for at the proper time we will reap a harvest.", r:"Galatians 6:9" },
    { d:0, m:"0 — Eternity. No beginning, no end. The Alpha and Omega is holding the whole circle of your life.", v:"I am the Alpha and the Omega, the First and the Last.", r:"Revelation 22:13" }
  ];

  function lookup(numStr) {
    var s = (numStr || "").replace(/[^0-9]/g, "");
    if (!s.length) return null;
    for (var i = 0; i < NUMBERS.length; i++) {
      if (NUMBERS[i].n === s && NUMBERS[i].n.length >= 3) return NUMBERS[i];
    }
    if (s === "11" || s === "22" || s === "33") {
      for (var j = 0; j < NUMBERS.length; j++) if (NUMBERS[j].n === s) return NUMBERS[j];
    }
    var last = parseInt(s[s.length - 1], 10);
    for (var k = 0; k < DIGITS.length; k++) if (DIGITS[k].d === last) return { n:s, title:"Digit " + last, m:DIGITS[k].m, v:DIGITS[k].v, r:DIGITS[k].r };
    return null;
  }

  return { NUMBERS: NUMBERS, DIGITS: DIGITS, lookup: lookup };
})();
