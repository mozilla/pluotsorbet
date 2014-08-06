
var xy = document.getElementById("xy");

document.getElementById("canvas").onmousemove = function(ev) {
  xy.textContent = "" + ev.layerX + "," + ev.layerY;
}

