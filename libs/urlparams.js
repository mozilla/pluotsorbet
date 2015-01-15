/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

/**
 * The URL query parameters.  One or more of the following:
 *
 *    args (gets split from comma-separated list to Array)
 *    gamepad
 *    icc_mcc
 *    icc_mnc
 *    icc_msisdn
 *    instrument
 *    jad
 *    jars
 *    logConsole
 *    logLevel
 *    main
 *    midletClassName
 *    network_mcc
 *    network_mnc
 *    platform
 *    profile
 *    pushConn
 *    pushMidlet
 *    autosize
 *    fontSize
 *    jsFiles
 *
 * Keep this list up-to-date!
 */

var urlParams = (function() {
  var params = {};

  location.search.substring(1).split("&").forEach(function (param) {
    param = param.split("=").map(function(v) {
      return v.replace(/\+/g, " ");
    }).map(decodeURIComponent);
    params[param[0]] = param[1];
  });

  params.args = (params.args || "").split(",");
  params.jsFiles = (params.jsFiles || "").split(",");

  for (var name in params) {
    config[name] = params[name];
  }

  return params;
})();
