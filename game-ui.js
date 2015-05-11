document.getElementById("up").onmousedown = function() {
  MIDP.sendKeyPress(119);
}

document.getElementById("up").onmouseup = function() {
  MIDP.sendKeyRelease(119);
}

document.getElementById("down").onmousedown = function() {
  MIDP.sendKeyPress(115);
}

document.getElementById("down").onmouseup = function() {
  MIDP.sendKeyRelease(115);
}

document.getElementById("left").onmousedown = function() {
  MIDP.sendKeyPress(97);
}

document.getElementById("left").onmouseup = function() {
  MIDP.sendKeyRelease(97);
}

document.getElementById("right").onmousedown = function() {
  MIDP.sendKeyPress(100);
}

document.getElementById("right").onmouseup = function() {
  MIDP.sendKeyRelease(100);
}

document.getElementById("fire").onmousedown = function() {
  MIDP.sendKeyPress(32);
}

document.getElementById("fire").onmouseup = function() {
  MIDP.sendKeyRelease(32);
}
