const fs    = require("fs");
const https = require("https");

var pem = fs.readFileSync('cert.pem');

var options = {
  key: pem,
  cert: pem,
  secureProtocol: 'SSLv3_method',
};

https.createServer(options, function(req, res) {
  var data = fs.readFileSync("." + req.url);

  res.writeHead(200, {
    'Content-Length': data.length,
    'Content-Type': 'text/html',
  });

  res.end(data);
}).listen(4443);
