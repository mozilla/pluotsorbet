// install a J2ME.js packaged app on phone and run Benchmark test

// prerequisites:

// on your phone, set the pref security.turn_off_all_security_so_that_viruses_can_take_over_this_computer
// see https://wiki.mozilla.org/B2G/QA/Tips_And_Tricks#For_changing_the_preference:

// make clean && BENCHMARK=1 make package

var startSimulator = require('node-firefox-start-simulator');
var findDevices = require('node-firefox-find-devices');
var forwardPorts = require('node-firefox-forward-ports');
var findPorts = require('node-firefox-find-ports');
var launchApp = require('node-firefox-launch-app');
var installApp = require('node-firefox-install-app');
var findApp = require('node-firefox-find-app');
var uninstallApp = require('node-firefox-uninstall-app');
var connect = require('node-firefox-connect');

var args = require('system').args;
var fs = require('fs');

var gBenchRunner = {};

gBenchRunner.deviceClient = null;
gBenchRunner.emulatorWebApp = null;
gBenchRunner.pathToPackagedApp = args[2];
gBenchRunner.install = false;

if (gBenchRunner.pathToPackagedApp == null) {
  console.log('usage: node benchrunner.js /path/to/packaged/app [install]');
  process.exit(1);
}

if (args[3] && args[3] == 'install') {
  gBenchRunner.install = true;
}

console.log('app package:', gBenchRunner.pathToPackagedApp);

// parse manifest file

var manifestString = fs.readFileSync(gBenchRunner.pathToPackagedApp + '/manifest.webapp', 'utf8');
gBenchRunner.manifest = JSON.parse(manifestString);


// find the device, uninstall the old packaged app, install the new packaged app

function reinstallEmulatorApp(apps) {
  console.log('Found', apps.length, 'existing apps');

  return Promise.all(apps.map(function(app) {
    console.log('Uninstalling', app.manifestURL);
    return uninstallApp({ manifestURL: app.manifestURL, client: gBenchRunner.deviceClient })
  }))

  .then(function installEmulatorApp() {
    console.log('Installing');
    return installApp({
      // 3. install the new version
      appPath: gBenchRunner.pathToPackagedApp,
      client: gBenchRunner.deviceClient
    })
  })

  .then(function finishInstallEmulatorApp(appId) {
    // 4. find the new version
    console.log('App installed', appId);

    // TODO: this is rude from a Promises point of view.
    process.exit(0);
  });
}

// find the device, find, launch, and connect to the j2me app, connect to the console, run the benchmark
 
function benchmarkEmulatorApp(apps) {

  gBenchRunner.emulatorWebApp = apps[0];

  console.log('Found', gBenchRunner.emulatorWebApp.name, gBenchRunner.emulatorWebApp.manifestURL);

  return launchApp({
    client: gBenchRunner.deviceClient,
    manifestURL: gBenchRunner.emulatorWebApp.manifestURL
  })

  .then(function connectToAppConsoleAndRunTest(result) {
    console.log('Launched app', result);

    gBenchRunner.deviceClient.getWebapps(function(err, webapps) {
      console.log('Getting webapp', gBenchRunner.emulatorWebApp.manifestURL);
      if (err) {
        console.log(err);
      }

      webapps.getApp(gBenchRunner.emulatorWebApp.manifestURL, function (err, app) {
        if (err) {
          console.log(err);
        }

        app.Console.addListener('console-api-call', function(e) {
          var consoleLine = e.arguments[0];

          if (consoleLine.indexOf('bench: ') >= 0) {
            console.log(consoleLine);

            if (consoleLine.indexOf('bench: done') >= 0) {
              // TODO: this is rude from a Promises point of view.
              process.exit(0);
            }
          }
        });

        app.Console.startListening();
        console.log('Listening to console');

        setTimeout(function () {
          app.Console.evaluateJS("cd(frames[0])", function(err, resp) {
            console.log('cd(frames[0])', err, resp);

            app.Console.evaluateJS("Benchmark.start()", function(err, resp) {
              console.log('Started Benchmark', err, resp);
            });

          });
        }, 30000);
      });
    });
  });
}

// Start process
Promise.resolve().then(function() {
  findPorts()

  .then(function connectToDevice(results) {
    console.log('findPorts', results);
    return connect(results[0].port);
  })

  .then(function connected(client) {
    console.log('Connected');

    gBenchRunner.deviceClient = client;
    return client;
  })

  .then(function findEmulatorApp(client) {
    // find the  old version of this app
    return findApp({
      manifest: gBenchRunner.manifest,
      client: gBenchRunner.deviceClient
    })
  })

  .then(function installOrRunBenchmark(apps) {

    if (gBenchRunner.install) {
      return reinstallEmulatorApp(apps);
    } else {
      return benchmarkEmulatorApp(apps);
    }

  })
})


