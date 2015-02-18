DumbPipe.registerOpener("echo", function(message, sender) {
  sender(message);
});

// Generate faked location data for tests
LocationProvider.AVAILABLE = 1;
DumbPipe.registerOpener("locationprovider", function(message, sender) {
  return function(message) {
    switch (message.type) {
      case "requestData":
        sender({
          type: "data",
          position: {
            timestamp: Date.now(),
            latitude: 45,
            longitude: -122,
            altitude: 500,
            horizontalAccuracy: 200,
            verticalAccuracy: 10,
            speed: 90,
            course: 2
          },
          state: LocationProvider.AVAILABLE
        });
        break;
    }
  };
});
