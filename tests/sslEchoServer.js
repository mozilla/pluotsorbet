const fs  = require("fs");
const tls = require("tls");

var pem = fs.readFileSync('cert.pem');

var options = {
  key: pem,
  cert: pem,
  secureProtocol: 'SSLv3_method',
};

tls.createServer(options, function(socket) {
  socket.on('data', function(data) {
    socket.write(data);
  });
}).listen(54443);
