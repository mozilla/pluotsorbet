

function makeSendKey(keyCode) {
  return function() {
    MIDP.keyPress(keyCode);
  }
}

document.getElementById("up").ontouchstart = makeSendKey(119);
document.getElementById("down").ontouchstart = makeSendKey(115);
document.getElementById("left").ontouchstart = makeSendKey(97);
document.getElementById("right").ontouchstart = makeSendKey(100);
document.getElementById("fire").ontouchstart = makeSendKey(32);

