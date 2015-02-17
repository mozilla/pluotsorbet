/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- /
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var emoji = (function() {
  // http://www.unicode.org/Public/UNIDATA/EmojiSources.txt
  // http://developer.nokia.com/resources/library/Java/developers-guides/data-handling/emoji.html
  var regexString = [
    '\ud83c[\udf00-\udfff]', // U+1F300 to U+1F3FF
    '\ud83d[\udc00-\ude4f]', // U+1F400 to U+1F64F
    '\ud83d[\ude80-\udeff]', // U+1F680 to U+1F6FF
    '[\u0023|\u0030-\u0039]\u20e3', // U+2320E3 to U+3920E3
    '\ud83c\uddef\ud83c\uddf5', // U+1F1EF U+1F1F5 (JP)
    '\ud83c\uddf0\ud83c\uddf7', // U+1F1F0 U+1F1F7 (KR)
    '\ud83c\udde9\ud83c\uddea', // U+1F1E9 U+1F1EA (DE)
    '\ud83c\udde8\ud83c\uddf3', // U+1F1E8 U+1F1F3 (CN)
    '\ud83c\uddfa\ud83c\uddf8', // U+1F1FA U+1F1F8 (US)
    '\ud83c\uddeb\ud83c\uddf7', // U+1F1EB U+1F1F7 (FR)
    '\ud83c\uddea\ud83c\uddf8', // U+1F1EA U+1F1F8 (ES)
    '\ud83c\uddee\ud83c\uddf9', // U+1F1EE U+1F1F9 (IT)
    '\ud83c\uddf7\ud83c\uddfa', // U+1F1F7 U+1F1FA (RU)
    '\ud83c\uddec\ud83c\udde7', // U+1F1EC U+1F1E7 (GB)
    '\ud83c\uddee\ud83c\uddf3', // U+1F1EE U+1F1F3 (IN)
    '\ud83c\uddf2\ud83c\uddfd', // U+1F1F2 U+1F1FD (MX)
    '\ud83c\udde7\ud83c\uddf7', // U+1F1E7 U+1F1F7 (BR)
    '\ud83c\uddf8\ud83c\udde6', // U+1F1F8 U+1F1E6 (SA)
    '\ud83c\uddff\ud83c\udde6', // U+1F1FF U+1F1E6 (ZA)
    '\ud83c\udde6\ud83c\uddf7', // U+1F1E6 U+1F1F7 (AR)
    '\ud83c\uddf3\ud83c\uddf1', // U+1F1F3 U+1F1F1 (NL)
    '\ud83c\uddf9\ud83c\uddf7', // U+1F1F9 U+1F1F7 (TR)
    '\ud83c\uddf2\ud83c\uddfe', // U+1F1F2 U+1F1FE (MY)
    '\ud83c\uddfb\ud83c\uddea', // U+1F1FB U+1F1EA (VE)
    '\ud83c\udde8\ud83c\uddf4', // U+1F1E8 U+1F1F4 (CO)
    '\ud83c\udde8\ud83c\uddf1', // U+1F1E8 U+1F1F1 (CL)
    '\ud83c\udded\ud83c\uddf0', // U+1F1ED U+1F1F0 (HK)
    '\ud83c\uddf3\ud83c\uddec', // U+1F1F3 U+1F1EC (NG)
    '\ud83c\udde8\ud83c\udded', // U+1F1E8 U+1F1ED (CH)
    '\ud83c\uddee\ud83c\uddf1', // U+1F1EE U+1F1F1 (IL)
    '\ud83c\uddf9\ud83c\udded', // U+1F1F9 U+1F1ED (TH)
    '\ud83c\uddf8\ud83c\uddec', // U+1F1F8 U+1F1EC (SG)
    '\ud83c\udde6\ud83c\uddea', // U+1F1E6 U+1F1EA (AE)
    '\ud83c\uddf9\ud83c\uddfc', // U+1F1F9 U+1F1FC (TW)
    '\ud83c\uddea\ud83c\uddec', // U+1F1EA U+1F1EC (EG)
    '\ud83c\udde8\ud83c\udde6', // U+1F1E8 U+1F1E6 (CA)
    '\ud83c\uddf2\ud83c\udde8', // U+1F1F2 U+1F1E8 (MC)
    '\ud83c\udde6\ud83c\uddf9', // U+1F1E6 U+1F1F9 (AT)
    '\ud83c\udde6\ud83c\uddfa', // U+1F1E6 U+1F1FA (AU)
    '\ud83c\udde7\ud83c\udde6', // U+1F1E7 U+1F1E6 (BA)
    '\ud83c\udde7\ud83c\uddea', // U+1F1E7 U+1F1EA (BE)
    '\ud83c\udde8\ud83c\uddee', // U+1F1E8 U+1F1EE (CI)
    '\ud83c\udde8\ud83c\uddf2', // U+1F1E8 U+1F1F2 (CM)
    '\ud83c\udde8\ud83c\uddf7', // U+1F1E8 U+1F1F7 (CR)
    '\ud83c\udde9\ud83c\uddff', // U+1F1E9 U+1F1FF (DZ)
    '\ud83c\uddea\ud83c\udde8', // U+1F1EA U+1F1E8 (EC)
    '\ud83c\uddec\ud83c\udded', // U+1F1EC U+1F1ED (GH)
    '\ud83c\uddec\ud83c\uddf7', // U+1F1EC U+1F1F7 (GR)
    '\ud83c\udded\ud83c\uddf3', // U+1F1ED U+1F1F3 (HN)
    '\ud83c\udded\ud83c\uddf7', // U+1F1ED U+1F1F7 (HR)
    '\ud83c\uddee\ud83c\uddf7', // U+1F1EE U+1F1F7 (IR)
    '\ud83c\uddef\ud83c\uddf4', // U+1F1EF U+1F1F4 (JO)
    '\ud83c\uddf0\ud83c\uddff', // U+1F1F0 U+1F1FF (KZ)
    '\ud83c\uddf1\ud83c\udde7', // U+1F1F1 U+1F1E7 (LB)
    '\ud83c\uddf5\ud83c\uddea', // U+1F1F5 U+1F1EA (PE)
    '\ud83c\uddf5\ud83c\uddf9', // U+1F1F5 U+1F1F9 (PT)
    '\ud83c\uddf8\ud83c\uddfe', // U+1F1F8 U+1F1FE (SY)
    '\ud83c\uddfa\ud83c\udde6', // U+1F1FA U+1F1E6 (UA)
    '\ud83c\uddfa\ud83c\uddfe', // U+1F1FA U+1F1FE (UY)
    '\ud83c\uddfd\ud83c\uddea', // U+1F1FD U+1F1EA (XE)
    '\ud83c\uddef',
    '\ud83c\uddf0',
    '\ud83c\udde9',
    '\ud83c\udde8',
    '\ud83c\uddfa',
    '\ud83c\uddeb',
    '\ud83c\uddea',
    '\ud83c\uddee',
    '\ud83c\uddf7',
    '\ud83c\uddec',
    '\ud83c\uddf5',
    '\ud83c\uddf7',
    '\ud83c\uddea',
    '\ud83c\uddf3',
    '\ud83c\uddf8',
    '\ud83c\uddf7',
    '\ud83c\uddf8',
    '\ud83c\uddf9',
    '\ud83c\uddfa',
    '\ud83c\udde7',
    '\u00a9',
    '\u00ae',
    '\u2122',
    '\u2139',
    '\u2194',
    '\u2195',
    '\u2196',
    '\u2197',
    '\u2198',
    '\u2199',
    '\u21A9',
    '\u21AA',
    '\u231A',
    '\u231B',
    '\u23E9',
    '\u23EA',
    '\u23EB',
    '\u23EC',
    '\u23F0',
    '\u23F3',
    '\u24C2',
    '\u25AA',
    '\u25AB',
    '\u25B6',
    '\u25C0',
    '\u25FB',
    '\u25FC',
    '\u25FD',
    '\u25FE',
    '\u2600',
    '\u2601',
    '\u260E',
    '\u2611',
    '\u2614',
    '\u2615',
    '\u261D',
    '\u2648',
    '\u2649',
    '\u264A',
    '\u264B',
    '\u264C',
    '\u264D',
    '\u264E',
    '\u264F',
    '\u2650',
    '\u2651',
    '\u2652',
    '\u2653',
    '\u2660',
    '\u2663',
    '\u2665',
    '\u2666',
    '\u2668',
    '\u267B',
    '\u267F',
    '\u2693',
    '\u26A0',
    '\u26A1',
    '\u26AA',
    '\u26AB',
    '\u26BD',
    '\u26BE',
    '\u26C4',
    '\u26C5',
    '\u26CE',
    '\u26D4',
    '\u26EA',
    '\u26FA',
    '\u26F2',
    '\u26F3',
    '\u26F5',
    '\u26FD',
    '\u2702',
    '\u2705',
    '\u2708',
    '\u2709',
    '\u270A',
    '\u270B',
    '\u270C',
    '\u270F',
    '\u2712',
    '\u2714',
    '\u2716',
    '\u2728',
    '\u2733',
    '\u2734',
    '\u2744',
    '\u2747',
    '\u274C',
    '\u274E',
    '\u2753',
    '\u2754',
    '\u2755',
    '\u2757',
    '\u2764',
    '\u2795',
    '\u2796',
    '\u2797',
    '\u27A1',
    '\u27B0',
    '\u27BF',
    '\u2934',
    '\u2935',
    '\u2B05',
    '\u2B06',
    '\u2B07',
    '\u2B1B',
    '\u2B1C',
    '\u2B50',
    '\u2B55',
    '\u3030',
    '\u303D',
    '\u3297',
    '\u3299',
    '\ud83c\udd70', // U+1F170
    '\ud83c\udd71', // U+1F171
    '\ud83c\udd7E', // U+1F17E
    '\ud83c\udd7F', // U+1F17F
    '\ud83c\udd8E', // U+1F18E
    '\ud83c\udd91', // U+1F191
    '\ud83c\udd92', // U+1F192
    '\ud83c\udd93', // U+1F193
    '\ud83c\udd94', // U+1F194
    '\ud83c\udd95', // U+1F195
    '\ud83c\udd96', // U+1F196
    '\ud83c\udd97', // U+1F197
    '\ud83c\udd98', // U+1F198
    '\ud83c\udd99', // U+1F199
    '\ud83c\udd9A', // U+1F19A
    '\ud83c\udE01', // U+1F201
    '\ud83c\udE02', // U+1F202
    '\ud83c\udE1A', // U+1F21A
    '\ud83c\udE2F', // U+1F22F
    '\ud83c\udE32', // U+1F232
    '\ud83c\udE33', // U+1F233
    '\ud83c\udE34', // U+1F234
    '\ud83c\udE35', // U+1F235
    '\ud83c\udE36', // U+1F236
    '\ud83c\udE37', // U+1F237
    '\ud83c\udE38', // U+1F238
    '\ud83c\udE39', // U+1F239
    '\ud83c\udE3A', // U+1F23A
    '\ud83c\udE50', // U+1F250
    '\ud83c\udE51', // U+1F251
  ].join("|");

  var data;
  var images = [];
  var imagesNum = 13;

  var squareSize = 16;

  return {
    regEx: new RegExp(regexString, 'g'),

    squareSize: squareSize,

    loadData: function() {
      return new Promise(function(resolve, reject) {
        if (data) {
            resolve();
            return;
        }

        var promises = [];

        promises.push(load("style/emoji.json", "json").then(function(obj) {
          data = obj;
        }));

        for (var i = 0; i < imagesNum; i++) {
          images[i] = new Image();
          images[i].src = "style/emoji/emoji" + i + ".png";

          promises.push(new Promise(function(resolve, reject) {
            images[i].onload = resolve;
          }));
        }

        Promise.all(promises).then(resolve);
      });
    },

    getData: function(str, size) {
      var firstCodePoint = str.codePointAt(0);

      var unified = firstCodePoint.toString(16);
      if (unified.length == 2) {
        unified = "00" + unified;
      }

      var len = String.fromCodePoint(firstCodePoint).length;
      if (str.length > len) {
        unified += "-" + str.substr(len).codePointAt(0).toString(16);
      }

      var emoji = data[unified];

      return {
        img: images[emoji.sheet],
        x: emoji.x * squareSize,
      };
    },
  };
})();

