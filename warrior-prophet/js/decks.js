/* Warrior Prophet — spiritual message decks */
"use strict";
var DECKS = (function () {
  var DECK_LIST = [
    {
      id: "promises", name: "God's Promises", sym: "🕯️", free: true,
      voice: "The Father's voice of promise and protection",
      intro: "Draw with a quiet heart. These are promises — receive the one meant for you today."
    },
    {
      id: "jesus", name: "Words of Jesus", sym: "✝️", free: true,
      voice: "The gentle, direct words of Jesus",
      intro: "Red-letter words. Draw and listen — He still speaks to the weary."
    },
    {
      id: "angels", name: "Angel Watch", sym: "👼", free: false,
      voice: "Messages from the angels who guard you",
      intro: "Angels are near. Draw a card and remember Who sent them."
    }
  ];

  var PROMISES = [
    ["I know the plans I have for you — plans for hope, not harm. The future is not lost; it is held.", "Jeremiah 29:11"],
    ["Fear not, for I am with you. My right hand holds you up when your strength runs out.", "Isaiah 41:10"],
    ["Trust Me with all your heart. Stop trying to map every turn — I will make the path straight.", "Proverbs 3:5-6"],
    ["My power shows up best in your weakness. You do not have to be strong today.", "2 Corinthians 12:9"],
    ["I will never leave you. Never. Read that again, slower.", "Deuteronomy 31:6"],
    ["Be still and know that I am God. The battle is bigger than you — and I am in it.", "Psalm 46:10"],
    ["I am doing a new thing in your life. Do not miss it by staring at the old thing.", "Isaiah 43:19"],
    ["Call to Me and I will answer you, and tell you things you could never imagine.", "Jeremiah 33:3"],
    ["I am able to do immeasurably more than all you ask or imagine. Pray bigger.", "Ephesians 3:20"],
    ["When you pass through the waters, you will not drown. I am in the river with you.", "Isaiah 43:2"],
    ["I am your refuge and strength, a very present help — not a distant one.", "Psalm 46:1"],
    ["Delight yourself in Me, and I will shape the desires of your heart — then grant them.", "Psalm 37:4"],
    ["I heal the brokenhearted. The wound is not the ending you think it is.", "Psalm 147:3"],
    ["Nothing — nothing — can separate you from My love. Not today's failure either.", "Romans 8:38-39"],
    ["I am the God of all comfort, so that comfort can flow through you to someone else.", "2 Corinthians 1:3-4"],
    ["Wait on Me and your strength will be renewed. You will rise with wings, not drag with worry.", "Isaiah 40:31"],
    ["Ask and it will be given; seek and you will find; knock and the door will open.", "Matthew 7:7"],
    ["I am with you always — in the waiting room, the night shift, and the quiet car ride.", "Matthew 28:20"],
    ["My mercies are new every morning. Yesterday's failure does not have today's word.", "Lamentations 3:22-23"],
    ["Be still. I will fight for you. Put the sword down and pick up your peace.", "Exodus 14:14"],
    ["I will supply every need you have, according to My riches — not your rationing.", "Philippians 4:19"],
    ["You are fearfully and wonderfully made. You were not an accident or an afterthought.", "Psalm 139:14"],
    ["I have called you by name. You are Mine. You belong.", "Isaiah 43:1"],
    ["The Lord bless you and keep you; make His face shine upon you and be gracious to you.", "Numbers 6:24-26"]
  ];

  var JESUS = [
    ["Come to Me, all who are weary and burdened, and I will give you rest.", "Matthew 11:28"],
    ["Do not let your heart be troubled. You believe in God; believe also in Me.", "John 14:1"],
    ["I am the light of the world. Follow Me and the darkness cannot stay.", "John 8:12"],
    ["Your faith has made you whole. Go in peace and be free from this thing.", "Mark 5:34"],
    ["I am the resurrection and the life. The story is not over.", "John 11:25"],
    ["Do not worry about tomorrow. Today has enough of its own — and I am in it.", "Matthew 6:34"],
    ["I am the good shepherd. I know My sheep and My sheep know My voice.", "John 10:11"],
    ["Ask in My name and it will be done. Pray like it matters — because it does.", "John 14:13-14"],
    ["Peace I leave with you; My peace I give you. Not as the world gives — real peace.", "John 14:27"],
    ["I go to prepare a place for you. Home is being built while you wait.", "John 14:2-3"],
    ["Love one another as I have loved you. That is the whole signature.", "John 13:34"],
    ["I am the vine; you are the branches. Stay connected and fruit comes naturally.", "John 15:5"],
    ["Let the little ones come to Me. Heaven belongs to hearts like theirs.", "Mark 10:14"],
    ["Blessed are the pure in heart, for they will see God. Keep your heart clean.", "Matthew 5:8"],
    ["Do not be afraid; only believe. Fear lies; faith listens.", "Mark 5:36"],
    ["I have told you these things so My joy may be in you, and your joy may be full.", "John 15:11"],
    ["Take My yoke upon you and learn from Me, for I am gentle and humble in heart.", "Matthew 11:29"],
    ["You are the light of the world. Stop hiding the lamp under the bowl.", "Matthew 5:14-16"],
    ["Whatever you did for the least of these, you did for Me. I noticed.", "Matthew 25:40"],
    ["I stand at the door and knock. The handle is on your side.", "Revelation 3:20"],
    ["In this world you will have trouble. But take heart — I have overcome the world.", "John 16:33"],
    ["Heaven and earth will pass away, but My words will never pass away.", "Matthew 24:35"],
    ["Seek first the kingdom, and everything else finds its place.", "Matthew 6:33"],
    ["Forgive, and you will be forgiven. Loose them, and you go free.", "Luke 6:37"]
  ];

  var ANGELS = [
    ["The angel of the Lord encamps around those who honor Him. You are guarded tonight.", "Psalm 34:7"],
    ["He commands His angels concerning you — to guard you in all your ways.", "Psalm 91:11"],
    ["Angels are ministering spirits sent to serve you. Help has been dispatched.", "Hebrews 1:14"],
    ["My God sent His angel and shut the lions' mouths. Your den has an angel in it too.", "Daniel 6:22"],
    ["An angel touched Elijah and said, 'Get up and eat — the journey is too much for you.' Rest and eat. That is the strategy.", "1 Kings 19:7"],
    ["Peter's chains fell off when the angel came. Your chains are next in line.", "Acts 12:7"],
    ["There is rejoicing in the presence of the angels over one who turns back. Come home.", "Luke 15:10"],
    ["Jacob saw a ladder, earth to heaven. The distance between you and God is smaller than it looks.", "Genesis 28:12"],
    ["'Do not be afraid' — the angel's first words are always peace. Receive the peace first.", "Luke 2:10"],
    ["Angels will bear you up in their hands, so you do not strike your foot against the stone.", "Psalm 91:12"],
    ["The mountain is full of horses and chariots of fire. You are not outnumbered; you are outgunned — for the enemy.", "2 Kings 6:17"],
    ["An angel opened the prison doors for the apostles. Watch your doors this week.", "Acts 5:19"],
    ["Angels long to look into these things. Your story is worth watching.", "1 Peter 1:12"],
    ["An angel strengthened Jesus in Gethsemane. Strength comes in the darkest hour, not before it.", "Luke 22:43"],
    ["Do not forget to show hospitality — some have entertained angels without knowing it.", "Hebrews 13:2"],
    ["Michael stands guard over your people. The war behind the curtain is already turning.", "Daniel 12:1"],
    ["Gabriel stood in the presence of God and was sent with good news. Good news is being sent.", "Luke 1:19"],
    ["Angels carried Lazarus to Abraham's side. Home is real, and you will know it.", "Luke 16:22"],
    ["The angel found Hagar by a spring in the desert. Provision finds the abandoned.", "Genesis 21:17"],
    ["An angel of God stood beside me this night. Do not be afraid; keep the faith.", "Acts 27:23"]
  ];

  function byId(id) {
    for (var i = 0; i < DECK_LIST.length; i++) if (DECK_LIST[i].id === id) return DECK_LIST[i];
    return DECK_LIST[0];
  }
  function cardsFor(id) {
    if (id === "jesus") return JESUS;
    if (id === "angels") return ANGELS;
    return PROMISES;
  }

  return { DECK_LIST: DECK_LIST, byId: byId, cardsFor: cardsFor };
})();
