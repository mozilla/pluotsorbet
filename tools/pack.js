if (process.argv.length < 3) {
  console.log('Usage: node ' + process.argv[1] + ' fileName');
  process.exit(1);
}


var fs = require('fs')
  , filename = process.argv[2];

function parseHeader(s) {
  if (s.substring(0, 5) === "//// ") {
    return {
      name: s.substring(5).trim()
    };
  }
  return null;
}

/**
 * This is a very simple packaging scheme.
 *
 * Name Length, Name, Data Length, Data
 *         I32  UTF8,         I32, UTF8
 */
fs.readFile(filename, 'utf8', function(err, data) {
  var stream = process.stdout;
  var lines = data.split("\n");
  var length = lines.length;
  var buffer;
  for (var i = 0; i < length; i++) {
    var header = parseHeader(lines[i]);
    if (header || i === length - 1) {
      if (buffer) {
        var dataBuffer = new Buffer(buffer.join("\n"));
        var t = new Buffer(4)
        t.writeInt32LE(dataBuffer.length, 0);
        stream.write(t);
        stream.write(dataBuffer);
        // console.log(dataBuffer.length)
        // console.log(dataBuffer);
        // console.log(dataBuffer.toString('utf8', 0, dataBuffer.length));

      }
    }
    if (header) {
      var nameBuffer = new Buffer(header.name);
      // console.log(header.name);
      // console.log(nameBuffer.length)
      // console.log(nameBuffer);
      var t = new Buffer(4)
      t.writeInt32LE(nameBuffer.length, 0);
      stream.write(t);
      stream.write(nameBuffer);
      buffer = [];
    } else {
      buffer.push(lines[i]);
    }
  }
});