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


if (args[3]) {

// find the device, uninstall the old packaged app, install the new packaged app, then launch it

findPorts().then(function(results) {
	console.log('findPorts', results);
	connect(results[0].port).then(function(client) {
		console.log('Connected');

		// 1. find the  old version of this app
		findApp({
			manifest: manifest,
			client: client
		}).then(function(apps) {
			// 2. uninstall the old version
			console.log('Found', apps.length, 'existing apps');
			Promise.all(apps.map(function(app) {
				console.log('Uninstalling', app.manifestURL);
				return uninstallApp({ manifestURL: app.manifestURL, client: client })
			}));
		}).then(installApp({
			// 3. install the new version
			appPath: pathToPackagedApp,
			client: client
		}).then(function(appId) {
			// 4. find the new version
			console.log('App installed', appId);
			process.exit(0);
		}))
	})
});


} else {

// find the device, find, launch, and connect to the j2me app, connect to the console, run the benchmark
 
findPorts().then(function(results) {
	console.log('findPorts', results);
	connect(results[0].port).then(function(client) {
		console.log('Connected');
		// find the j2me app
		findApp({
			manifest: manifest,
			client: client
		}).then(function(apps) {
		// launch the j2me app
			if (apps.length > 0) {
				var firstApp = apps[0];
				console.log('Found', firstApp.name, firstApp.manifestURL);
				launchApp({
					client: client,
					manifestURL: firstApp.manifestURL
				}).then(function(result) {
					console.log('Launched app', result);

					client.getWebapps(function(err, webapps) {
						console.log('Getting app', manifest);
						webapps.getApp(firstApp.manifestURL, function (err, app) {
							if (err) {
								console.log(err);
							}

							app.Console.addListener('console-api-call', function(e) {
								var consoleLine = e.arguments[0];

								if (consoleLine.indexOf('bench: ') >= 0) {
									console.log(consoleLine);
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
  				}, function(err) {
					console.error('Could not launch app', err);
				});
			}
		}, function(e) {
			console.error('Could not find app', e);
		});
	})
});


}
