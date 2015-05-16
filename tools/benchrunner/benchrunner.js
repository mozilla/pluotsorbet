// install a J2ME.js packaged app on phone and run Benchmark test

// prerequisites:

// set the pref security.turn_off_all_security_so_that_viruses_can_take_over_this_computer
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

// command line looks like "node benchrunner.js /path/to/packaged/app [install]"
var pathToPackagedApp = args[2];
console.log('app package:', pathToPackagedApp);

// parse manifest file

var manifestString = fs.readFileSync(pathToPackagedApp + '/manifest.webapp', 'utf8');
var manifest = JSON.parse(manifestString);

var gDeviceClient = null;
var gEmulatorWebApp = null;

// find the device, uninstall the old packaged app, install the new packaged app

function reinstallEmulatorApp() {

	return findPorts()

	.then(function connectToDevice(results) {
		console.log('findPorts', results);
		return connect(results[0].port);
	})

	.then(function connected(client) {
		console.log('Connected');

		gDeviceClient = client;
		return client;
	})

	.then(function findEmulatorApp(client) {
		// find the  old version of this app
		return findApp({
			manifest: manifest,
			client: client
		})
	})

	.then(function uninstallEmulatorApp(apps) {
		// uninstall the old version
		console.log('Found', apps.length, 'existing apps');
		return Promise.all(apps.map(function(app) {
			console.log('Uninstalling', app.manifestURL);
			return uninstallApp({ manifestURL: app.manifestURL, client: gDeviceClient })
		}));
	})

	.then(function installEmulatorApp() {
		console.log('Installing');
		return installApp({
			// 3. install the new version
			appPath: pathToPackagedApp,
			client: gDeviceClient
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
 
function benchmarkEmulatorApp() {

	return findPorts()

	.then(function connectToDevice(results) {
		console.log('findPorts', results);
		return connect(results[0].port);
	})

	.then(function connected(client) {
		console.log('Connected');

		gDeviceClient = client;
		return client;
	})

	.then(function findEmulatorApp(client) {
		return findApp({
			manifest: manifest,
			client: client
		});
	})

	.then(function launchEmulatorApp(apps) {
		// launch the j2me app
		if (apps.length > 0) {
			gEmulatorWebApp = apps[0];
			console.log('Found', gEmulatorWebApp.name, gEmulatorWebApp.manifestURL);
			return launchApp({
				client: gDeviceClient,
				manifestURL: gEmulatorWebApp.manifestURL
			});
		} else {
			return null;
		}
	})

	.then(function connectToAppConsoleAndRunTest(result) {
		console.log('Launched app', result);

		gDeviceClient.getWebapps(function(err, webapps) {
			console.log('Getting webapp', gEmulatorWebApp.manifestURL);
			if (err) {
				console.log(err);
			}

			webapps.getApp(gEmulatorWebApp.manifestURL, function (err, app) {
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
	// reinstallEmulatorApp();
	benchmarkEmulatorApp();
})


