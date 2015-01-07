/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

(function() {

  var windowConsole = window.console;

  var LOG_LEVELS = {
    trace: 0,
    log: 1,
    info: 2,
    warn: 3,
    error: 4,
    silent: 5,
  };

  /**
   * The console(s) to which messages should be output.  A comma-separated list
   * of one or more of these consoles:
   *    web: the browser's Web Console (default)
   *    native: the native console (via the *dump* function)
   *    terminal: a faster canvas based console if Shumway.js is included.
   */
  var ENABLED_CONSOLE_TYPES = (urlParams.logConsole || "terminal").split(",");
  var minLogLevel = LOG_LEVELS[urlParams.logLevel || "log"];


  //================================================================


  var startTime = performance.now();

  /**
   * Every log entry serializes itself into a LogItem, so that it can
   * subsequently be piped to various consoles.
   */
  function LogItem(levelName, args) {
    if (levelName === "trace") {
      // If logging a trace, save the stack (minus uninteresting parts):
      this.stack = new Error().stack.split('\n').filter(function(line) {
        return line.indexOf("console.js") !== -1;
      }).join('\n');
    }

    this.levelName = levelName;
    this.ctx = $ ? $.ctx : null;
    this.logLevel = LOG_LEVELS[levelName];
    this.args = args;
    this.time = performance.now() - startTime;
  }

  LogItem.prototype = {
    get messagePrefix() {
      var s = J2ME.Context.currentContextPrefix();
      if (false) {
        s = this.time.toFixed(2) + " " + s;
      }
      return s.toString().padRight(" ", 4) + " | ";
    },

    get message() {
      if (this._message === undefined) {
        this._message = this.messagePrefix + this.args.join(" ") + " ";
      }
      return this._message;
    },

    get searchPredicate() {
      if (this._searchPredicate === undefined) {
        this._searchPredicate = this.message.toLowerCase();
      }
      return this._searchPredicate;
    },

    /**
     * Return this log item as an HTML node suitable for insertion
     * into the page console, caching the node for performance when
     * doing live filtering.
     */
    toHtmlElement: function() {
      if (this._cachedElement === undefined) {
        var div = document.createElement("div");
        div.classList.add("log-item");
        div.classList.add("log-item-" + this.levelName);
        div.textContent = this.message + "\n";
        this._cachedElement = div;
      }
      return this._cachedElement;
    },

    matchesCurrentFilters: function() {
      return this.logLevel >= minLogLevel;
    }
  };


  //================================================================
  // Console Implementations


  /**
   * WebConsole: The standard console.log() and friends.
   */
  function WebConsole() {
    this.buffer = "";
  }

  WebConsole.prototype = {
    flush: function() {
      if (this.buffer.length) {
        var temp = this.buffer;
        this.buffer = "";
        console.info(temp);
      }
    },

    push: function(item) {
      if (item.matchesCurrentFilters()) {
        this.flush(); // Preserve order w/r/t console.print().
        windowConsole[item.levelName].apply(windowConsole, item.args);
      }
    },

    /** Print one character to the output (buffered). */
    print: function(ch) {
      if (ch === 10) {
        this.flush();
      } else {
        this.buffer += String.fromCharCode(ch);
      }
    }
  };

  /**
   * NativeConsole: Throws logs at Gecko's dump().
   */
  function NativeConsole() {
  }

  NativeConsole.prototype = {
    push: function(item) {
      if (item.matchesCurrentFilters()) {
        dumpLine(item.message);
      }
    }
  };

  /**
   * RawConsoleForTests: Spits text directly into a textarea, for
   * simpler CasperJS-style output testing.
   */
  function RawConsoleForTests(selector) {
    this.el = document.querySelector(selector);
  }

  RawConsoleForTests.prototype = {
    push: function(item) {
      if (item.matchesCurrentFilters()) {
        this.el.textContent += item.levelName[0].toUpperCase() + ' ' + item.args.join(" ") + '\n';
      }
    }
  };

  function TerminalConsole(selector) {
    this.buffer = new Terminal.Buffer();
    this.view = new Terminal.View(new Terminal.Screen(document.querySelector(selector), 10), this.buffer);
    this.count = 0;
    window.addEventListener(
      'console-clear', this.onClear.bind(this));
  }

  var contextColors = ["#111111", "#222222", "#333333", "#444444", "#555555", "#666666"];
  var colors = [0xAAFFFF, 0xFF0000, 0x00FFFF, 0xFFFF00, 0xFF0000, 0x0000FF];

  TerminalConsole.prototype = {
    push: function(item) {
      if (item.matchesCurrentFilters()) {
        //var color;
        //if (item.logLevel < 3) {
        //  var id = item.ctx.runtime.id + item.ctx.id;
        //  color = contextColors[id % contextColors.length];
        //} else {
        //  color = colors[item.logLevel]
        //}
        //this.appendToTraceTerminal(item.message, color);
        var color = colors[item.logLevel];
        // this.screen.setColor(color >> 16, color >> 8, color >> 0);
        // this.screen.writeText((this.count ++) + " " + item.message);
        // this.screen.nextLine();
        this.buffer.writeString((this.count ++) + " " + item.message);
        this.buffer.writeLine();
        this.view.scrollToBottom();
      }
    },
    onClear: function() {
      this.buffer.clear();
      this.view.scrollToBottom();
    }
  }

  var CONSOLES = {
    web: new WebConsole(),
    native: new NativeConsole(),
    raw: new RawConsoleForTests("#raw-console"),
    terminal: new TerminalConsole("#consoleContainer")
  };

  var print = CONSOLES.web.print.bind(CONSOLES.web);

  // If we're only printing to the web console, then use the original console
  // object, so that file/line number references show up correctly in it.
  if (ENABLED_CONSOLE_TYPES.length === 1 && ENABLED_CONSOLE_TYPES[0] === "web") {
    windowConsole.print = print;
    return;
  }


  //================================================================
  // Filtering & Runtime Page Console Options

  document.querySelector('#console-clear').addEventListener('click', function() {
    window.dispatchEvent(new CustomEvent('console-clear'));
  });

  var logLevelSelect = document.querySelector('#loglevel');
  function updateFilters() {
    minLogLevel = logLevelSelect.value;
    window.dispatchEvent(new CustomEvent('console-filters-changed'));
  }

  logLevelSelect.value = minLogLevel;
  logLevelSelect.addEventListener('change', updateFilters);


  //----------------------------------------------------------------


  var logAtLevel = function(levelName) {
    var item = new LogItem(levelName, Array.prototype.slice.call(arguments, 1));
    ENABLED_CONSOLE_TYPES.forEach(function(consoleType) {
      CONSOLES[consoleType].push(item);
    });
  };

  window.console = {
    trace: logAtLevel.bind(null, "trace"),
    log: logAtLevel.bind(null, "log"),
    info: logAtLevel.bind(null, "info"),
    warn: logAtLevel.bind(null, "warn"),
    error: logAtLevel.bind(null, "error"),
    print: print
  };

})();
