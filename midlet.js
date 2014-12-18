// Midlet-specific customization code.

var MIDlet = {
  name: "aMIDlet",

  SMSDialogTimeout: 300000, // Five minutes
  SMSDialogTimeoutText: "left",
  SMSDialogReceiveFilter: function(message) {
    return message;
  },
};
