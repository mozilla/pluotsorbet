/*
Copyright (c) 2013 Andreas Madsen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

Built using https://github.com/AndreasMadsen/ttest with browserify.
*/

(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){

var OneDataSet = require('./hypothesis/one-data-set.js');
var TwoDataSet = require('./hypothesis/two-data-set.js');

var Summary = require('summary');

var ALTERNATIVE_MAP = {
  "not equal": 0,
  "less": -1,
  "greater": 1
};

function isList(list) {
  return (Array.isArray(list) || list instanceof Summary);
}

function hypothesis(left, right, options) {
  // Vertify required arguments
  if (!isList(left)) {
    throw new TypeError('left value in hypothesis test must be an array');
  }

  if (!isList(right)) {
    options = right;
    right = undefined;
  }

  // Set the default options
  if (!options) options = {};

  options = {
    mu: options.hasOwnProperty('mu') ? options.mu : 0,
    alpha: options.hasOwnProperty('alpha') ? options.alpha : 0.05,
    alternative: options.hasOwnProperty('alternative') ? ALTERNATIVE_MAP[options.alternative] : 0
  };

  // Vertify mu option
  if (typeof options.mu !== 'number') {
    throw new TypeError('alpha option must be a number');
  }

  // Vertify alpha option
  if (typeof options.alpha !== 'number') {
    throw new TypeError('alpha option must be a number');
  }
  if (options.alpha >= 1) {
    throw new RangeError('alpha must be bellow 1.0');
  }

  // Vertify alternative option
  if (typeof options.alternative === undefined) {
    throw new Error('alternative must be either not equal, less or greater');
  }

  // Perform the student's t test
  if (isList(right)) {
    return new TwoDataSet(left, right, options);
  } else {
    return new OneDataSet(left, options);
  }
}
module.exports = hypothesis;

},{"./hypothesis/one-data-set.js":3,"./hypothesis/two-data-set.js":4,"summary":14}],2:[function(require,module,exports){

function AbstactStudentT(options) {
  this._options = options;
}
module.exports = AbstactStudentT;

AbstactStudentT.prototype.testValue = function () {
  var dif = (this._mean - this._options.mu);
  return dif / this._fac;
};

AbstactStudentT.prototype.pValue = function () {
  var t = this.testValue();

  switch (this._options.alternative) {
    case 1: // mu > mu[0]
      return 1 - this._dist.cdf(t);
    case -1: // mu < mu[0]
      return this._dist.cdf(t);
    case 0: // mu != mu[0]
      return 2 * (1 - this._dist.cdf(Math.abs(t)));
  }
};

AbstactStudentT.prototype.confidence = function () {
  var pm;
  switch (this._options.alternative) {
    case 1: // mu > mu[0]
      pm = Math.abs(this._dist.inv(this._options.alpha)) * this._fac;
      return [this._mean - pm, Infinity];
    case -1: // mu < mu[0]
      pm = Math.abs(this._dist.inv(this._options.alpha)) * this._fac;
      return [-Infinity, this._mean + pm];
    case 0: // mu != mu[0]
      pm = Math.abs(this._dist.inv(this._options.alpha / 2)) * this._fac;
      return [this._mean - pm, this._mean + pm];
  }
};

AbstactStudentT.prototype.valid = function () {
  return this.pValue() >= this._options.alpha;
};

AbstactStudentT.prototype.freedom = function () {
  return this._freedom;
}
},{}],3:[function(require,module,exports){

var Distribution = require('distributions').Studentt;
var Summary = require('summary');

var util = require('util');
var AbstactStudentT = require('./abstact.js');

function StudentT(data, options) {
  AbstactStudentT.call(this, options);

  var summary = (data instanceof Summary) ? data : new Summary(data);

  this._freedom = summary.size() - 1;
  var variance = summary.variance();

  this._fac = Math.sqrt(variance* (1 / summary.size()));
  this._mean = summary.mean();

  this._dist = new Distribution(this._freedom);
}
util.inherits(StudentT, AbstactStudentT);
module.exports = StudentT;

},{"./abstact.js":2,"distributions":5,"summary":14,"util":19}],4:[function(require,module,exports){

var Distribution = require('distributions').Studentt;
var Summary = require('summary');

var util = require('util');
var AbstactStudentT = require('./abstact.js');

function StudentT(left, right, options) {
  AbstactStudentT.call(this, options);

  var leftsummary = (left instanceof Summary) ? left : new Summary(left);
  var rightsummary = (right instanceof Summary) ? right : new Summary(right);

  this._freedom = leftsummary.size() + rightsummary.size() - 2;
  var commonVariance = ((leftsummary.size() - 1) * leftsummary.variance() +
                        (rightsummary.size() - 1) * rightsummary.variance()) / this._freedom;

  this._fac = Math.sqrt(commonVariance * (1 / leftsummary.size() + 1 / rightsummary.size()));
  this._mean = leftsummary.mean() - rightsummary.mean();

  this._dist = new Distribution(this._freedom);
}
util.inherits(StudentT, AbstactStudentT);
module.exports = StudentT;

},{"./abstact.js":2,"distributions":5,"summary":14,"util":19}],5:[function(require,module,exports){

var a = require('./distributions/normal.js');
var a = require('./distributions/uniform.js');
var a = require('./distributions/studentt.js');
var files = ['normal', 'uniform', 'studentt'];

for (var i = 0, l = files.length; i < l; i++) {
  var fns = require('./distributions/' + files[i] + '.js');
  var keys = Object.keys(fns);

  for (var n = 0, r = keys.length; n < r; n++) {
    exports[ keys[n] ] = fns[keys[n]];
  }
}

},{"./distributions/normal.js":6,"./distributions/studentt.js":7,"./distributions/uniform.js":8}],6:[function(require,module,exports){

var mathfn = require('mathfn');

function NormalDistribution(mean, sd) {
  if (!(this instanceof NormalDistribution)) {
    return new NormalDistribution(mean, sd);
  }

  if (typeof mean !== 'number' && mean !== undefined) {
    throw TypeError('mean must be a number');
  }
  if (typeof sd !== 'number' && sd !== undefined) {
    throw TypeError('sd must be a number');
  }

  if (sd !== undefined && sd <= 0.0) {
    throw TypeError('sd must be positive');
  }

  this._mean = mean || 0;
  this._sd = sd || 1;
  this._var = this._sd * this._sd;
}
exports.Normal = NormalDistribution;

// -0.5 * log(2 Pi)
var HALF_TWO_PI_LOG = -0.91893853320467274180;

NormalDistribution.prototype.pdf = function (x) {
  return Math.exp(HALF_TWO_PI_LOG - Math.log(this._sd) - Math.pow(x - this._mean, 2) / (2 * this._var));
};

NormalDistribution.prototype.cdf = function (x) {
  return 0.5 * (1 + mathfn.erf((x - this._mean) / Math.sqrt(2 * this._var)));
};

NormalDistribution.prototype.inv = function (p) {
  return -Math.SQRT2 * this._sd * mathfn.invErfc(2 * p) + this._mean;
};

NormalDistribution.prototype.median = function () {
  return this._mean;
};

NormalDistribution.prototype.mean = function () {
  return this._mean;
};

NormalDistribution.prototype.variance = function () {
  return this._var;
};

},{"mathfn":13}],7:[function(require,module,exports){

var mathfn = require('mathfn');

function StudenttDistribution(df) {
  if (!(this instanceof StudenttDistribution)) {
    return new StudenttDistribution(df);
  }

  if (typeof df !== 'number') {
    throw TypeError('mean must be a number');
  }
  if (df <= 0) {
    throw RangeError('df must be a positive number');
  }

  this._df = df;

  this._pdf_const = (mathfn.gamma((df + 1) / 2) / (Math.sqrt(df * Math.PI) * mathfn.gamma(df / 2)));
  this._pdf_exp = -((df + 1) / 2);

  this._df_half = df / 2;
}
exports.Studentt = StudenttDistribution;

StudenttDistribution.prototype.pdf = function (x) {
  return this._pdf_const * Math.pow(1 + ((x*x) / this._df), this._pdf_exp);
};

StudenttDistribution.prototype.cdf = function (x) {
  var fac = Math.sqrt(x * x + this._df);

  return mathfn.incBeta((x + fac) / (2 * fac), this._df_half, this._df_half);
};

StudenttDistribution.prototype.inv = function (p) {
  var fac = mathfn.invIncBeta(2 * Math.min(p, 1 - p), this._df_half, 0.5);
  var y = Math.sqrt(this._df * (1 - fac) / fac);
  return (p > 0.5) ? y : -y;
};

StudenttDistribution.prototype.median = function () {
  return 0;
};

StudenttDistribution.prototype.mean = function () {
  return (this._df > 1) ? 0 : undefined;
};

StudenttDistribution.prototype.variance = function () {
  if (this._df > 2) return this._df / (this._df - 2);
  else if (this._df > 1) return Infinity;
  else return undefined;
};

},{"mathfn":13}],8:[function(require,module,exports){

function UniformDistribution(a, b) {
  if (!(this instanceof UniformDistribution)) {
    return new UniformDistribution(a, b);
  }

  if (typeof a !== 'number' && a !== undefined) {
    throw TypeError('mean must be a number');
  }
  if (typeof b !== 'number' && b !== undefined) {
    throw TypeError('sd must be a number');
  }

  this._a = typeof a === 'number' ? a : 0;
  this._b = typeof b === 'number' ? b : 1;

  if (this._b <= this._a) {
    throw new RangeError('a must be greater than b');
  }

  this._k = 1 / (this._b - this._a);
  this._mean = (this._a + this._b) / 2;
  this._var = (this._a - this._b) * (this._a - this._b) / 12;
}
exports.Uniform = UniformDistribution;

UniformDistribution.prototype.pdf = function (x) {
  return (x < this._a || x > this._b) ? 0 : this._k;
};

UniformDistribution.prototype.cdf = function (x) {
  if (x < this._a) return 0;
  else if (x > this._b) return 1;
  else return (x - this._a) * this._k;
};

UniformDistribution.prototype.inv = function (p) {
  if (p < 0 || p > 1) return NaN;
  else return p * (this._b - this._a) + this._a;
};

UniformDistribution.prototype.median = function () {
  return this._mean;
};

UniformDistribution.prototype.mean = function () {
  return this._mean;
};

UniformDistribution.prototype.variance = function () {
  return this._var;
};

},{}],9:[function(require,module,exports){

var gammaCollection = require('./gamma.js');
var log1p = require('./log.js').log1p;

//
// The beta functions are taken from the jStat library, and modified to fit
// the API and style pattern used in this module.
// See: https://github.com/jstat/jstat/
// License: MIT
// 

//Copyright (c) 2013 jStat
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

function beta(x, y) {
	if (x < 0 || y < 0) {
   throw RangeError('Arguments must be positive.'); 
	}

  // Some special cases
  else if (x === 0 && y === 0) return NaN;
  else if (x === 0 || y === 0) return Infinity;

	// make sure x + y doesn't exceed the upper limit of usable values
  else if (x + y > 170) {
    return Math.exp(gammaCollection.betaln(x, y));
  }

  else {
    return gammaCollection.gamma(x) * gammaCollection.gamma(y) / gammaCollection.gamma(x + y);
  }
}
exports.beta = beta;

function logBeta(x, y) {
  if (x < 0 || y < 0) {
   throw RangeError('Arguments must be positive.'); 
	}

  // Some special cases
  else if (x === 0 && y === 0) return NaN;
  else if (x === 0 || y === 0) return Infinity;
  
  else {
    return gammaCollection.logGamma(x) + gammaCollection.logGamma(y) - gammaCollection.logGamma(x + y);
  }
}
exports.logBeta = logBeta;

// evaluates the continued fraction for incomplete beta function by modified Lentz's method.
function betacf(x, a, b) {
	var fpmin = 1e-30,
		m = 1,
		m2, aa, c, d, del, h, qab, qam, qap;
	// These q's will be used in factors that occur in the coefficients
	qab = a + b;
	qap = a + 1;
	qam = a - 1;
	c = 1;
	d = 1 - qab * x / qap;
	if (Math.abs(d) < fpmin) d = fpmin;
	d = 1 / d;
	h = d;
	for (; m <= 100; m++) {
		m2 = 2 * m;
		aa = m * (b - m) * x / ((qam + m2) * (a + m2));
		// One step (the even one) of the recurrence
		d = 1 + aa * d;
		if (Math.abs(d) < fpmin) d = fpmin;
		c = 1 + aa / c;
		if (Math.abs(c) < fpmin) c = fpmin;
		d = 1 / d;
		h *= d * c;
		aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2));
		// Next step of the recurrence (the odd one)
		d = 1 + aa * d;
		if (Math.abs(d) < fpmin) d = fpmin;
		c = 1 + aa / c;
		if (Math.abs(c) < fpmin) c = fpmin;
		d = 1 / d;
		del = d * c;
		h *= del;
		if (Math.abs(del - 1.0) < 3e-7) break;
	}
	return h;
}

// Returns the incomplete beta function I_x(a,b)
function incBeta(x, a, b) {
	if(x < 0 || x > 1) {
    throw new RangeError('First argument must be between 0 and 1.');
	}

  // Special cases, there can make trouble otherwise
  else if (a === 1 && b === 1) return x;
  else if (x === 0) return 0;
  else if (x === 1) return 1;
  else if (a === 0) return 1;
  else if (b === 0) return 0;

  else {
    var bt = 
      Math.exp(gammaCollection.logGamma(a + b) -
      gammaCollection.logGamma(a) -
      gammaCollection.logGamma(b) +
      a * Math.log(x) +
      b * log1p(-x));

    // Use continued fraction directly.
    if (x < (a + 1) / (a + b + 2)) return bt * betacf(x, a, b) / a;
    // else use continued fraction after making the symmetry transformation.
    else return 1 - bt * betacf(1 - x, b, a) / b;
  }
}
exports.incBeta = incBeta;

// Returns the inverse of the incomplete beta function
function invIncBeta(p, a, b) {
  if(x < 0 || x > 1) {
    throw new RangeError('First argument must be between 0 and 1.');
	}

  // Special cases, there can make trouble otherwise
  else if (a === 1 && b === 1) return p;
  else if (p === 1) return 1;
  else if (p === 0) return 0;
  else if (a === 0) return 0;
  else if (b === 0) return 1;

  else {
    var EPS = 1e-8,
        a1 = a - 1,
        b1 = b - 1,
        j = 0,
        lna, lnb, pp, t, u, err, x, al, h, w, afac;
  
	if(a >= 1 && b >= 1) {
    pp = (p < 0.5) ? p : 1 - p;
    t = Math.sqrt(-2 * Math.log(pp));

		x = (2.30753 + t * 0.27061) / (1 + t* (0.99229 + t * 0.04481)) - t;
		if(p < 0.5) x = -x;
		al = (x * x - 3) / 6;
		h = 2 / (1 / (2 * a - 1)  + 1 / (2 * b - 1));
		w = (x * Math.sqrt(al + h) / h) - (1 / (2 * b - 1) - 1 / (2 * a - 1)) * (al + 5 / 6 - 2 / (3 * h));
		x = a / (a + b * Math.exp(2 * w));
	} else {
		lna = Math.log(a / (a + b));
		lnb = Math.log(b / (a + b));
		t = Math.exp(a * lna) / a;
		u = Math.exp(b * lnb) / b;
		w = t + u;
		if (p < t / w) x = Math.pow(a * w * p, 1 / a);
		else x = 1 - Math.pow(b * w * (1 - p), 1 / b);
	}

	afac = -gammaCollection.logGamma(a) - gammaCollection.logGamma(b) + gammaCollection.logGamma(a + b);

  for(; j < 10; j++) {
		if(x === 0 || x === 1) return x;
		err = incBeta(x, a, b) - p;

    t = Math.exp(a1 * Math.log(x) + b1 * log1p(-x) + afac);
		u = err / t;
		x -= (t = u / (1 - 0.5 * Math.min(1, u * (a1 / x - b1 / (1 - x)))));

    if (x <= 0) x = 0.5 * (x + t);
		if (x >= 1) x = 0.5 * (x + t + 1);

		if (Math.abs(t) < EPS * x && j > 0) break;
	}

	return x;
  }
}
exports.invIncBeta = invIncBeta;

},{"./gamma.js":11,"./log.js":12}],10:[function(require,module,exports){

//
// Modified from:
//  C++: http://www.johndcook.com/cpp_erf.html
//
var ERF_A = [
  0.254829592,
  -0.284496736,
  1.421413741,
  -1.453152027,
  1.061405429
];
var ERF_P = 0.3275911;

function erf(x) {
  var sign = 1;
  if (x < 0) sign = -1;

  x = Math.abs(x);

  var t = 1.0/(1.0 + ERF_P*x);
  var y = 1.0 - (((((ERF_A[4]*t + ERF_A[3])*t) + ERF_A[2])*t + ERF_A[1])*t + ERF_A[0])*t*Math.exp(-x*x);

  return sign * y;
}
exports.erf = erf;

//
// Combined from two sources:
//  Python: http://pydoc.net/Python/timbre/1.0.0/timbre.stats/
//  JavaScript: https://github.com/jstat/jstat/blob/master/src/special.js
//
var M_2_SQRTPI = 1.12837916709551257;

var ERFC_COF = [
  -2.8e-17, 1.21e-16, -9.4e-17, -1.523e-15, 7.106e-15,
   3.81e-16, -1.12708e-13, 3.13092e-13, 8.94487e-13,
  -6.886027e-12, 2.394038e-12, 9.6467911e-11,
  -2.27365122e-10, -9.91364156e-10, 5.059343495e-9,
   6.529054439e-9, -8.5238095915e-8, 1.5626441722e-8,
   1.303655835580e-6, -1.624290004647e-6,
  -2.0278578112534e-5, 4.2523324806907e-5,
   3.66839497852761e-4, -9.46595344482036e-4,
  -9.561514786808631e-3, 1.9476473204185836e-2,
   6.4196979235649026e-1, -1.3026537197817094
];
var ERFC_COF_LAST = ERFC_COF[ERFC_COF.length - 1];

function erfc(x) {
  function erfccheb(y) {
    var d = 0.0, dd = 0.0, temp = 0.0,
        t = 2.0 / (2.0 + y), ty = 4.0 * t - 2.0;
  
    for (var i = 0, l = ERFC_COF.length - 1; i < l; i++) {
      temp = d;
      d = ty * d - dd + ERFC_COF[i];
      dd = temp;
    }
  
    return t * Math.exp(-y * y + 0.5 * (ERFC_COF_LAST + ty * d) - dd);
  }
  
  return x >= 0.0 ? erfccheb(x) : 2.0 - erfccheb(-x);
}
exports.erfc = erfc;

//
// Combined from three sources:
//  Python: http://pydoc.net/Python/timbre/1.0.0/timbre.stats/
//  JavaScript: https://github.com/jstat/jstat/blob/master/src/special.js
//  C: https://github.com/Peteysoft/sea_ice/blob/master/src/mcc_ice/inverf.c
//
function invErfc(p) {
  if (p < 0.0 || p > 2.0) {
    throw RangeError('Argument must be betweeen 0 and 2');
  }

  else if (p === 0.0) {
    return Infinity;
  }
  
  else if (p === 2.0) {
    return -Infinity;
  }
  
  else {
    var pp = p < 1.0 ? p : 2.0 - p;
    var t = Math.sqrt(-2.0 * Math.log(pp / 2.0));
    var x = -0.70711 * ((2.30753 + t * 0.27061) / (1.0 + t * (0.99229 + t * 0.04481)) - t);

    var err1 = erfc(x) - pp;
    x += err1 / (M_2_SQRTPI * Math.exp(-x * x) - x * err1);
    var err2 = erfc(x) - pp;
    x += err2 / (M_2_SQRTPI * Math.exp(-x * x) - x * err2);

    return p < 1.0 ? x : -x;
  }
}
exports.invErfc = invErfc;

//
// Used math: inverf(x) = -inverfc(1 + x);
//  NOTE: you are welcome to add a specific approximation
//
function invErf(p) {
  if (p < -1.0 || p > 1.0) {
    throw RangeError('Argument must be betweeen -1 and 1');
  }

  return -invErfc(p + 1);
}
exports.invErf = invErf;

},{}],11:[function(require,module,exports){

//
// Modified form:
//  C++: http://www.johndcook.com/cpp_gamma.html
//

// Euler's gamma constant
var GAMMA_CONST = 0.577215664901532860606512090;

// numerator coefficients for approximation over the interval (1,2)
var P_COFF = [
  -1.71618513886549492533811E+0,
   2.47656508055759199108314E+1,
  -3.79804256470945635097577E+2,
   6.29331155312818442661052E+2,
   8.66966202790413211295064E+2,
  -3.14512729688483675254357E+4,
  -3.61444134186911729807069E+4,
   6.64561438202405440627855E+4
];

// denominator coefficients for approximation over the interval (1,2)
var Q_COFF = [
  -3.08402300119738975254353E+1,
   3.15350626979604161529144E+2,
  -1.01515636749021914166146E+3,
  -3.10777167157231109440444E+3,
   2.25381184209801510330112E+4,
   4.75584627752788110767815E+3,
  -1.34659959864969306392456E+5,
  -1.15132259675553483497211E+5
];

function gamma(x) {
  if (x <= 0.0) {
    throw new RangeError('Argument must be positive.');
	}

	// For small x, 1/Gamma(x) has power series x + gamma x^2  - ...
	// So in this range, 1/Gamma(x) = x + gamma x^2 with error on the order of x^3.
	// The relative error over this interval is less than 6e-7.
  else if (x < 0.001) {
    return 1.0/(x*(1.0 + GAMMA_CONST*x));
  }
  
  // The algorithm directly approximates gamma over (1,2) and uses
  // reduction identities to reduce other arguments to this interval.
  else if (x < 12.0) {
    var y = x, n = 0, lessOne = (y < 1.0);

    // Add or subtract integers as necessary to bring y into (1,2)
    if (lessOne) {
      y += 1.0;
    } else {
      n = Math.floor(y) - 1;
      y -= n;
    }
    
    var num = 0.0, den = 1.0, z = y - 1;
    for (var i = 0; i < 8; i++) {
      num = (num + P_COFF[i])*z;
      den = den*z + Q_COFF[i];
    }
    var result = num/den + 1.0;

    // Apply correction if argument was not initially in (1,2)
    if (lessOne) {
      result /= (y-1.0);
    } else {
      // Use the identity gamma(z+n) = z*(z+1)* ... *(z+n-1)*gamma(z)
      for (i = 0; i < n; i++)
        result *= y++;
    }

    return result;
  }

  // Correct answer too large to display. Force +infinity.
  else if (x > 171.624) {
		return Infinity;
  }
  
  else {
    return Math.exp(logGamma(x));
  }
}

// gamma functions goes under two names
exports.gamma = gamma;

//
// Modified form:
//  C++: http://www.johndcook.com/cpp_gamma.html
//

var C_COFF = [
   1.0/12.0,
  -1.0/360.0,
   1.0/1260.0,
  -1.0/1680.0,
   1.0/1188.0,
  -691.0/360360.0,
   1.0/156.0,
  -3617.0/122400.0
];

var HALF_LOG_TWO_PI = 0.91893853320467274178032973640562;

function logGamma(x) {
  if (x <= 0.0) {
    throw new RangeError('Argument must be positive.');
	}

  else if (x < 12.0) {
    return Math.log(Math.abs(gamma(x)));
  }

  // Abramowitz and Stegun 6.1.41
  // Asymptotic series should be good to at least 11 or 12 figures
  // For error analysis, see Whittiker and Watson
  // A Course in Modern Analysis (1927), page 252
  
  else {
    var  z = 1.0/(x*x);
    var sum = C_COFF[7];
    for (var i = 6; i >= 0; i--) {
      sum *= z;
      sum += C_COFF[i];
    }
    var series = sum/x;
    return (x - 0.5)*Math.log(x) - x + HALF_LOG_TWO_PI + series;
  }
}
exports.logGamma = logGamma;

},{}],12:[function(require,module,exports){

//
// Modified from:
//  C++: http://www.johndcook.com/cpp_erf.html
//

function log1p(x) {
  if (x <= -1.0) {
    throw new RangeError('Argument mustbe greater than -1.0');
  }

  // x is large enough that the obvious evaluation is OK
  else if (Math.abs(x) > 1e-4) {
      return Math.log(1.0 + x);
  }

  // Use Taylor approx. log(1 + x) = x - x^2/2 with error roughly x^3/3
  // Since |x| < 10^-4, |x|^3 < 10^-12, relative error less than 10^-8
  else {
    return (-0.5*x + 1.0)*x;
  }
}
exports.log1p = log1p;

//
// Modified from:
//  C++: http://www.johndcook.com/cpp_erf.html
//
var TABLE_LOOKUP = [
  0.000000000000000,
  0.000000000000000,
  0.693147180559945,
  1.791759469228055,
  3.178053830347946,
  4.787491742782046,
  6.579251212010101,
  8.525161361065415,
  10.604602902745251,
  12.801827480081469,
  15.104412573075516,
  17.502307845873887,
  19.987214495661885,
  22.552163853123421,
  25.191221182738683,
  27.899271383840894,
  30.671860106080675,
  33.505073450136891,
  36.395445208033053,
  39.339884187199495,
  42.335616460753485,
  45.380138898476908,
  48.471181351835227,
  51.606675567764377,
  54.784729398112319,
  58.003605222980518,
  61.261701761002001,
  64.557538627006323,
  67.889743137181526,
  71.257038967168000,
  74.658236348830158,
  78.092223553315307,
  81.557959456115029,
  85.054467017581516,
  88.580827542197682,
  92.136175603687079,
  95.719694542143202,
  99.330612454787428,
  102.968198614513810,
  106.631760260643450,
  110.320639714757390,
  114.034211781461690,
  117.771881399745060,
  121.533081515438640,
  125.317271149356880,
  129.123933639127240,
  132.952575035616290,
  136.802722637326350,
  140.673923648234250,
  144.565743946344900,
  148.477766951773020,
  152.409592584497350,
  156.360836303078800,
  160.331128216630930,
  164.320112263195170,
  168.327445448427650,
  172.352797139162820,
  176.395848406997370,
  180.456291417543780,
  184.533828861449510,
  188.628173423671600,
  192.739047287844900,
  196.866181672889980,
  201.009316399281570,
  205.168199482641200,
  209.342586752536820,
  213.532241494563270,
  217.736934113954250,
  221.956441819130360,
  226.190548323727570,
  230.439043565776930,
  234.701723442818260,
  238.978389561834350,
  243.268849002982730,
  247.572914096186910,
  251.890402209723190,
  256.221135550009480,
  260.564940971863220,
  264.921649798552780,
  269.291097651019810,
  273.673124285693690,
  278.067573440366120,
  282.474292687630400,
  286.893133295426990,
  291.323950094270290,
  295.766601350760600,
  300.220948647014100,
  304.686856765668720,
  309.164193580146900,
  313.652829949878990,
  318.152639620209300,
  322.663499126726210,
  327.185287703775200,
  331.717887196928470,
  336.261181979198450,
  340.815058870798960,
  345.379407062266860,
  349.954118040770250,
  354.539085519440790,
  359.134205369575340,
  363.739375555563470,
  368.354496072404690,
  372.979468885689020,
  377.614197873918670,
  382.258588773060010,
  386.912549123217560,
  391.575988217329610,
  396.248817051791490,
  400.930948278915760,
  405.622296161144900,
  410.322776526937280,
  415.032306728249580,
  419.750805599544780,
  424.478193418257090,
  429.214391866651570,
  433.959323995014870,
  438.712914186121170,
  443.475088120918940,
  448.245772745384610,
  453.024896238496130,
  457.812387981278110,
  462.608178526874890,
  467.412199571608080,
  472.224383926980520,
  477.044665492585580,
  481.872979229887900,
  486.709261136839360,
  491.553448223298010,
  496.405478487217580,
  501.265290891579240,
  506.132825342034830,
  511.008022665236070,
  515.890824587822520,
  520.781173716044240,
  525.679013515995050,
  530.584288294433580,
  535.496943180169520,
  540.416924105997740,
  545.344177791154950,
  550.278651724285620,
  555.220294146894960,
  560.169054037273100,
  565.124881094874350,
  570.087725725134190,
  575.057539024710200,
  580.034272767130800,
  585.017879388839220,
  590.008311975617860,
  595.005524249382010,
  600.009470555327430,
  605.020105849423770,
  610.037385686238740,
  615.061266207084940,
  620.091704128477430,
  625.128656730891070,
  630.172081847810200,
  635.221937855059760,
  640.278183660408100,
  645.340778693435030,
  650.409682895655240,
  655.484856710889060,
  660.566261075873510,
  665.653857411105950,
  670.747607611912710,
  675.847474039736880,
  680.953419513637530,
  686.065407301994010,
  691.183401114410800,
  696.307365093814040,
  701.437263808737160,
  706.573062245787470,
  711.714725802289990,
  716.862220279103440,
  722.015511873601330,
  727.174567172815840,
  732.339353146739310,
  737.509837141777440,
  742.685986874351220,
  747.867770424643370,
  753.055156230484160,
  758.248113081374300,
  763.446610112640200,
  768.650616799717000,
  773.860102952558460,
  779.075038710167410,
  784.295394535245690,
  789.521141208958970,
  794.752249825813460,
  799.988691788643450,
  805.230438803703120,
  810.477462875863580,
  815.729736303910160,
  820.987231675937890,
  826.249921864842800,
  831.517780023906310,
  836.790779582469900,
  842.068894241700490,
  847.352097970438420,
  852.640365001133090,
  857.933669825857460,
  863.231987192405430,
  868.535292100464630,
  873.843559797865740,
  879.156765776907600,
  884.474885770751830,
  889.797895749890240,
  895.125771918679900,
  900.458490711945270,
  905.796028791646340,
  911.138363043611210,
  916.485470574328820,
  921.837328707804890,
  927.193914982476710,
  932.555207148186240,
  937.921183163208070,
  943.291821191335660,
  948.667099599019820,
  954.046996952560450,
  959.431492015349480,
  964.820563745165940,
  970.214191291518320,
  975.612353993036210,
  981.015031374908400,
  986.422203146368590,
  991.833849198223450,
  997.249949600427840,
  1002.670484599700300,
  1008.095434617181700,
  1013.524780246136200,
  1018.958502249690200,
  1024.396581558613400,
  1029.838999269135500,
  1035.285736640801600,
  1040.736775094367400,
  1046.192096209724900,
  1051.651681723869200,
  1057.115513528895000,
  1062.583573670030100,
  1068.055844343701400,
  1073.532307895632800,
  1079.012946818975000,
  1084.497743752465600,
  1089.986681478622400,
  1095.479742921962700,
  1100.976911147256000,
  1106.478169357800900,
  1111.983500893733000,
  1117.492889230361000,
  1123.006317976526100,
  1128.523770872990800,
  1134.045231790853000,
  1139.570684729984800,
  1145.100113817496100,
  1150.633503306223700,
  1156.170837573242400
];

function logFactorial(n) {
  if (n < 0) {
    throw new Error('Argument may not be negative.');
  }

  // For big values use a function
  else if (n > 254) {
    var x = n + 1;
    return (x - 0.5)*Math.log(x) - x + 0.5*Math.log(2*Math.PI) + 1.0/(12.0*x);
  }
  
  // For small values use a table lookup
  else {
    return TABLE_LOOKUP[n];
  }
}
exports.logFactorial = logFactorial;

},{}],13:[function(require,module,exports){

var files = ['erf', 'gamma', 'beta', 'log'];

require('./functions/erf.js');
require('./functions/gamma.js');
require('./functions/beta.js');
require('./functions/log.js');

for (var i = 0, l = files.length; i < l; i++) {
  var fns = require('./functions/' + files[i] + '.js');
  var keys = Object.keys(fns);

  for (var n = 0, r = keys.length; n < r; n++) {
    exports[ keys[n] ] = fns[keys[n]];
  }
}

},{"./functions/beta.js":9,"./functions/erf.js":10,"./functions/gamma.js":11,"./functions/log.js":12}],14:[function(require,module,exports){

var array_types = [
    Array, Int8Array, Uint8Array, Int16Array, Uint16Array,
    Int32Array, Uint32Array, Float32Array, Float64Array
];

function Summary(data, sorted) {
  if (!(this instanceof Summary)) return new Summary(data, sorted);

  if (array_types.indexOf(data.constructor) === -1) {
    throw TypeError('data must be an array');
  }

  this._data = data;
  this._sorted = !!sorted;
  this._length = data.length;

  this._cache_sum = null;
  this._cache_mode = null;
  this._cache_mean = null;
  this._cache_quartiles = {};
  this._cache_variance = null;
  this._cache_sd = null;
  this._cache_max = null;
  this._cache_min = null;
}
module.exports = Summary;

//
// Not all values are in lazy calculated since that wouldn't do any good
//
Summary.prototype.sort = function() {
  if (this._sorted === false) {
    this._sorted = true;
    this._data = this._data.sort(function (a, b) { return a - b; });
  }

  return this._data;
};

Summary.prototype.size = function () {
  return this._length;
};

//
// Always lazy calculated functions
//
Summary.prototype.sum = function () {
  if (this._cache_sum === null) {
    var sum = 0;
    for (var i = 0; i < this._length; i++) sum += this._data[i];
    this._cache_sum = sum;
  }

  return this._cache_sum;
};

Summary.prototype.mode = function () {
  if (this._cache_mode === null) {
    var data = this.sort();

    var modeValue = NaN;
    var modeCount = 0;
    var currValue = data[0];
    var currCount = 1;

    // Count the amount of repeat and update mode variables
    for (var i = 1; i < this._length; i++) {
      if (data[i] === currValue) {
        currCount += 1;
      } else {
        if (currCount >= modeCount) {
          modeCount = currCount;
          modeValue = currValue;
        }

        currValue = data[i];
        currCount = 1;
      }
    }

    // Check the last count
    if (currCount >= modeCount) {
      modeCount = currCount;
      modeValue = currValue;
    }

    this._cache_mode = modeValue;
  }

  return this._cache_mode;
};

Summary.prototype.mean = function () {
  if (this._cache_mean === null) {
    this._cache_mean = this.sum() / this._length;
  }

  return this._cache_mean;
};

Summary.prototype.quartile = function (prob) {
  if (!this._cache_quartiles.hasOwnProperty(prob)) {
    var data = this.sort();
    var product = prob * this.size();
    var ceil = Math.ceil(product);

    if (ceil === product) {
      if (ceil === 0) {
        this._cache_quartiles[prob] = data[0];
      } else if (ceil === data.length) {
        this._cache_quartiles[prob] = data[data.length - 1];
      } else {
        this._cache_quartiles[prob] = (data[ceil - 1] + data[ceil]) / 2;
      }
    } else {
      this._cache_quartiles[prob] = data[ceil - 1];
    }
  }

  return this._cache_quartiles[prob];
};

Summary.prototype.median = function () {
  return this.quartile(0.5);
};

Summary.prototype.variance = function () {
  if (this._cache_variance === null) {
    var mean = this.mean();
    var sqsum = 0;
    for (var i = 0; i < this._length; i++) {
      sqsum += (this._data[i] - mean) * (this._data[i] - mean);
    }

    this._cache_variance = sqsum / (this._length - 1);
  }

  return this._cache_variance;
};

Summary.prototype.sd = function () {
  if (this._cache_sd === null) {
    this._cache_sd = Math.sqrt(this.variance());
  }

  return this._cache_sd;
};

Summary.prototype.max = function () {
  if (this._cache_max === null) {
    this._cache_max = this.sort()[this._length - 1];
  }

  return this._cache_max;
};

Summary.prototype.min = function () {
  if (this._cache_min === null) {
    this._cache_min = this.sort()[0];
  }

  return this._cache_min;
};

},{}],15:[function(require,module,exports){
window.ttest = require('ttest');

},{"ttest":1}],16:[function(require,module,exports){
if (typeof Object.create === 'function') {
  // implementation from standard node.js 'util' module
  module.exports = function inherits(ctor, superCtor) {
    ctor.super_ = superCtor
    ctor.prototype = Object.create(superCtor.prototype, {
      constructor: {
        value: ctor,
        enumerable: false,
        writable: true,
        configurable: true
      }
    });
  };
} else {
  // old school shim for old browsers
  module.exports = function inherits(ctor, superCtor) {
    ctor.super_ = superCtor
    var TempCtor = function () {}
    TempCtor.prototype = superCtor.prototype
    ctor.prototype = new TempCtor()
    ctor.prototype.constructor = ctor
  }
}

},{}],17:[function(require,module,exports){
// shim for using process in browser

var process = module.exports = {};
var queue = [];
var draining = false;

function drainQueue() {
    if (draining) {
        return;
    }
    draining = true;
    var currentQueue;
    var len = queue.length;
    while(len) {
        currentQueue = queue;
        queue = [];
        var i = -1;
        while (++i < len) {
            currentQueue[i]();
        }
        len = queue.length;
    }
    draining = false;
}
process.nextTick = function (fun) {
    queue.push(fun);
    if (!draining) {
        setTimeout(drainQueue, 0);
    }
};

process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];
process.version = ''; // empty string to avoid regexp issues

function noop() {}

process.on = noop;
process.addListener = noop;
process.once = noop;
process.off = noop;
process.removeListener = noop;
process.removeAllListeners = noop;
process.emit = noop;

process.binding = function (name) {
    throw new Error('process.binding is not supported');
};

// TODO(shtylman)
process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};
process.umask = function() { return 0; };

},{}],18:[function(require,module,exports){
module.exports = function isBuffer(arg) {
  return arg && typeof arg === 'object'
    && typeof arg.copy === 'function'
    && typeof arg.fill === 'function'
    && typeof arg.readUInt8 === 'function';
}
},{}],19:[function(require,module,exports){
(function (process,global){
// Copyright Joyent, Inc. and other Node contributors.
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit
// persons to whom the Software is furnished to do so, subject to the
// following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
// NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.

var formatRegExp = /%[sdj%]/g;
exports.format = function(f) {
  if (!isString(f)) {
    var objects = [];
    for (var i = 0; i < arguments.length; i++) {
      objects.push(inspect(arguments[i]));
    }
    return objects.join(' ');
  }

  var i = 1;
  var args = arguments;
  var len = args.length;
  var str = String(f).replace(formatRegExp, function(x) {
    if (x === '%%') return '%';
    if (i >= len) return x;
    switch (x) {
      case '%s': return String(args[i++]);
      case '%d': return Number(args[i++]);
      case '%j':
        try {
          return JSON.stringify(args[i++]);
        } catch (_) {
          return '[Circular]';
        }
      default:
        return x;
    }
  });
  for (var x = args[i]; i < len; x = args[++i]) {
    if (isNull(x) || !isObject(x)) {
      str += ' ' + x;
    } else {
      str += ' ' + inspect(x);
    }
  }
  return str;
};


// Mark that a method should not be used.
// Returns a modified function which warns once by default.
// If --no-deprecation is set, then it is a no-op.
exports.deprecate = function(fn, msg) {
  // Allow for deprecating things in the process of starting up.
  if (isUndefined(global.process)) {
    return function() {
      return exports.deprecate(fn, msg).apply(this, arguments);
    };
  }

  if (process.noDeprecation === true) {
    return fn;
  }

  var warned = false;
  function deprecated() {
    if (!warned) {
      if (process.throwDeprecation) {
        throw new Error(msg);
      } else if (process.traceDeprecation) {
        console.trace(msg);
      } else {
        console.error(msg);
      }
      warned = true;
    }
    return fn.apply(this, arguments);
  }

  return deprecated;
};


var debugs = {};
var debugEnviron;
exports.debuglog = function(set) {
  if (isUndefined(debugEnviron))
    debugEnviron = process.env.NODE_DEBUG || '';
  set = set.toUpperCase();
  if (!debugs[set]) {
    if (new RegExp('\\b' + set + '\\b', 'i').test(debugEnviron)) {
      var pid = process.pid;
      debugs[set] = function() {
        var msg = exports.format.apply(exports, arguments);
        console.error('%s %d: %s', set, pid, msg);
      };
    } else {
      debugs[set] = function() {};
    }
  }
  return debugs[set];
};


/**
 * Echos the value of a value. Trys to print the value out
 * in the best way possible given the different types.
 *
 * @param {Object} obj The object to print out.
 * @param {Object} opts Optional options object that alters the output.
 */
/* legacy: obj, showHidden, depth, colors*/
function inspect(obj, opts) {
  // default options
  var ctx = {
    seen: [],
    stylize: stylizeNoColor
  };
  // legacy...
  if (arguments.length >= 3) ctx.depth = arguments[2];
  if (arguments.length >= 4) ctx.colors = arguments[3];
  if (isBoolean(opts)) {
    // legacy...
    ctx.showHidden = opts;
  } else if (opts) {
    // got an "options" object
    exports._extend(ctx, opts);
  }
  // set default options
  if (isUndefined(ctx.showHidden)) ctx.showHidden = false;
  if (isUndefined(ctx.depth)) ctx.depth = 2;
  if (isUndefined(ctx.colors)) ctx.colors = false;
  if (isUndefined(ctx.customInspect)) ctx.customInspect = true;
  if (ctx.colors) ctx.stylize = stylizeWithColor;
  return formatValue(ctx, obj, ctx.depth);
}
exports.inspect = inspect;


// http://en.wikipedia.org/wiki/ANSI_escape_code#graphics
inspect.colors = {
  'bold' : [1, 22],
  'italic' : [3, 23],
  'underline' : [4, 24],
  'inverse' : [7, 27],
  'white' : [37, 39],
  'grey' : [90, 39],
  'black' : [30, 39],
  'blue' : [34, 39],
  'cyan' : [36, 39],
  'green' : [32, 39],
  'magenta' : [35, 39],
  'red' : [31, 39],
  'yellow' : [33, 39]
};

// Don't use 'blue' not visible on cmd.exe
inspect.styles = {
  'special': 'cyan',
  'number': 'yellow',
  'boolean': 'yellow',
  'undefined': 'grey',
  'null': 'bold',
  'string': 'green',
  'date': 'magenta',
  // "name": intentionally not styling
  'regexp': 'red'
};


function stylizeWithColor(str, styleType) {
  var style = inspect.styles[styleType];

  if (style) {
    return '\u001b[' + inspect.colors[style][0] + 'm' + str +
           '\u001b[' + inspect.colors[style][1] + 'm';
  } else {
    return str;
  }
}


function stylizeNoColor(str, styleType) {
  return str;
}


function arrayToHash(array) {
  var hash = {};

  array.forEach(function(val, idx) {
    hash[val] = true;
  });

  return hash;
}


function formatValue(ctx, value, recurseTimes) {
  // Provide a hook for user-specified inspect functions.
  // Check that value is an object with an inspect function on it
  if (ctx.customInspect &&
      value &&
      isFunction(value.inspect) &&
      // Filter out the util module, it's inspect function is special
      value.inspect !== exports.inspect &&
      // Also filter out any prototype objects using the circular check.
      !(value.constructor && value.constructor.prototype === value)) {
    var ret = value.inspect(recurseTimes, ctx);
    if (!isString(ret)) {
      ret = formatValue(ctx, ret, recurseTimes);
    }
    return ret;
  }

  // Primitive types cannot have properties
  var primitive = formatPrimitive(ctx, value);
  if (primitive) {
    return primitive;
  }

  // Look up the keys of the object.
  var keys = Object.keys(value);
  var visibleKeys = arrayToHash(keys);

  if (ctx.showHidden) {
    keys = Object.getOwnPropertyNames(value);
  }

  // IE doesn't make error fields non-enumerable
  // http://msdn.microsoft.com/en-us/library/ie/dww52sbt(v=vs.94).aspx
  if (isError(value)
      && (keys.indexOf('message') >= 0 || keys.indexOf('description') >= 0)) {
    return formatError(value);
  }

  // Some type of object without properties can be shortcutted.
  if (keys.length === 0) {
    if (isFunction(value)) {
      var name = value.name ? ': ' + value.name : '';
      return ctx.stylize('[Function' + name + ']', 'special');
    }
    if (isRegExp(value)) {
      return ctx.stylize(RegExp.prototype.toString.call(value), 'regexp');
    }
    if (isDate(value)) {
      return ctx.stylize(Date.prototype.toString.call(value), 'date');
    }
    if (isError(value)) {
      return formatError(value);
    }
  }

  var base = '', array = false, braces = ['{', '}'];

  // Make Array say that they are Array
  if (isArray(value)) {
    array = true;
    braces = ['[', ']'];
  }

  // Make functions say that they are functions
  if (isFunction(value)) {
    var n = value.name ? ': ' + value.name : '';
    base = ' [Function' + n + ']';
  }

  // Make RegExps say that they are RegExps
  if (isRegExp(value)) {
    base = ' ' + RegExp.prototype.toString.call(value);
  }

  // Make dates with properties first say the date
  if (isDate(value)) {
    base = ' ' + Date.prototype.toUTCString.call(value);
  }

  // Make error with message first say the error
  if (isError(value)) {
    base = ' ' + formatError(value);
  }

  if (keys.length === 0 && (!array || value.length == 0)) {
    return braces[0] + base + braces[1];
  }

  if (recurseTimes < 0) {
    if (isRegExp(value)) {
      return ctx.stylize(RegExp.prototype.toString.call(value), 'regexp');
    } else {
      return ctx.stylize('[Object]', 'special');
    }
  }

  ctx.seen.push(value);

  var output;
  if (array) {
    output = formatArray(ctx, value, recurseTimes, visibleKeys, keys);
  } else {
    output = keys.map(function(key) {
      return formatProperty(ctx, value, recurseTimes, visibleKeys, key, array);
    });
  }

  ctx.seen.pop();

  return reduceToSingleString(output, base, braces);
}


function formatPrimitive(ctx, value) {
  if (isUndefined(value))
    return ctx.stylize('undefined', 'undefined');
  if (isString(value)) {
    var simple = '\'' + JSON.stringify(value).replace(/^"|"$/g, '')
                                             .replace(/'/g, "\\'")
                                             .replace(/\\"/g, '"') + '\'';
    return ctx.stylize(simple, 'string');
  }
  if (isNumber(value))
    return ctx.stylize('' + value, 'number');
  if (isBoolean(value))
    return ctx.stylize('' + value, 'boolean');
  // For some reason typeof null is "object", so special case here.
  if (isNull(value))
    return ctx.stylize('null', 'null');
}


function formatError(value) {
  return '[' + Error.prototype.toString.call(value) + ']';
}


function formatArray(ctx, value, recurseTimes, visibleKeys, keys) {
  var output = [];
  for (var i = 0, l = value.length; i < l; ++i) {
    if (hasOwnProperty(value, String(i))) {
      output.push(formatProperty(ctx, value, recurseTimes, visibleKeys,
          String(i), true));
    } else {
      output.push('');
    }
  }
  keys.forEach(function(key) {
    if (!key.match(/^\d+$/)) {
      output.push(formatProperty(ctx, value, recurseTimes, visibleKeys,
          key, true));
    }
  });
  return output;
}


function formatProperty(ctx, value, recurseTimes, visibleKeys, key, array) {
  var name, str, desc;
  desc = Object.getOwnPropertyDescriptor(value, key) || { value: value[key] };
  if (desc.get) {
    if (desc.set) {
      str = ctx.stylize('[Getter/Setter]', 'special');
    } else {
      str = ctx.stylize('[Getter]', 'special');
    }
  } else {
    if (desc.set) {
      str = ctx.stylize('[Setter]', 'special');
    }
  }
  if (!hasOwnProperty(visibleKeys, key)) {
    name = '[' + key + ']';
  }
  if (!str) {
    if (ctx.seen.indexOf(desc.value) < 0) {
      if (isNull(recurseTimes)) {
        str = formatValue(ctx, desc.value, null);
      } else {
        str = formatValue(ctx, desc.value, recurseTimes - 1);
      }
      if (str.indexOf('\n') > -1) {
        if (array) {
          str = str.split('\n').map(function(line) {
            return '  ' + line;
          }).join('\n').substr(2);
        } else {
          str = '\n' + str.split('\n').map(function(line) {
            return '   ' + line;
          }).join('\n');
        }
      }
    } else {
      str = ctx.stylize('[Circular]', 'special');
    }
  }
  if (isUndefined(name)) {
    if (array && key.match(/^\d+$/)) {
      return str;
    }
    name = JSON.stringify('' + key);
    if (name.match(/^"([a-zA-Z_][a-zA-Z_0-9]*)"$/)) {
      name = name.substr(1, name.length - 2);
      name = ctx.stylize(name, 'name');
    } else {
      name = name.replace(/'/g, "\\'")
                 .replace(/\\"/g, '"')
                 .replace(/(^"|"$)/g, "'");
      name = ctx.stylize(name, 'string');
    }
  }

  return name + ': ' + str;
}


function reduceToSingleString(output, base, braces) {
  var numLinesEst = 0;
  var length = output.reduce(function(prev, cur) {
    numLinesEst++;
    if (cur.indexOf('\n') >= 0) numLinesEst++;
    return prev + cur.replace(/\u001b\[\d\d?m/g, '').length + 1;
  }, 0);

  if (length > 60) {
    return braces[0] +
           (base === '' ? '' : base + '\n ') +
           ' ' +
           output.join(',\n  ') +
           ' ' +
           braces[1];
  }

  return braces[0] + base + ' ' + output.join(', ') + ' ' + braces[1];
}


// NOTE: These type checking functions intentionally don't use `instanceof`
// because it is fragile and can be easily faked with `Object.create()`.
function isArray(ar) {
  return Array.isArray(ar);
}
exports.isArray = isArray;

function isBoolean(arg) {
  return typeof arg === 'boolean';
}
exports.isBoolean = isBoolean;

function isNull(arg) {
  return arg === null;
}
exports.isNull = isNull;

function isNullOrUndefined(arg) {
  return arg == null;
}
exports.isNullOrUndefined = isNullOrUndefined;

function isNumber(arg) {
  return typeof arg === 'number';
}
exports.isNumber = isNumber;

function isString(arg) {
  return typeof arg === 'string';
}
exports.isString = isString;

function isSymbol(arg) {
  return typeof arg === 'symbol';
}
exports.isSymbol = isSymbol;

function isUndefined(arg) {
  return arg === void 0;
}
exports.isUndefined = isUndefined;

function isRegExp(re) {
  return isObject(re) && objectToString(re) === '[object RegExp]';
}
exports.isRegExp = isRegExp;

function isObject(arg) {
  return typeof arg === 'object' && arg !== null;
}
exports.isObject = isObject;

function isDate(d) {
  return isObject(d) && objectToString(d) === '[object Date]';
}
exports.isDate = isDate;

function isError(e) {
  return isObject(e) &&
      (objectToString(e) === '[object Error]' || e instanceof Error);
}
exports.isError = isError;

function isFunction(arg) {
  return typeof arg === 'function';
}
exports.isFunction = isFunction;

function isPrimitive(arg) {
  return arg === null ||
         typeof arg === 'boolean' ||
         typeof arg === 'number' ||
         typeof arg === 'string' ||
         typeof arg === 'symbol' ||  // ES6 symbol
         typeof arg === 'undefined';
}
exports.isPrimitive = isPrimitive;

exports.isBuffer = require('./support/isBuffer');

function objectToString(o) {
  return Object.prototype.toString.call(o);
}


function pad(n) {
  return n < 10 ? '0' + n.toString(10) : n.toString(10);
}


var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep',
              'Oct', 'Nov', 'Dec'];

// 26 Feb 16:19:34
function timestamp() {
  var d = new Date();
  var time = [pad(d.getHours()),
              pad(d.getMinutes()),
              pad(d.getSeconds())].join(':');
  return [d.getDate(), months[d.getMonth()], time].join(' ');
}


// log is just a thin wrapper to console.log that prepends a timestamp
exports.log = function() {
  console.log('%s - %s', timestamp(), exports.format.apply(exports, arguments));
};


/**
 * Inherit the prototype methods from one constructor into another.
 *
 * The Function.prototype.inherits from lang.js rewritten as a standalone
 * function (not on Function.prototype). NOTE: If this file is to be loaded
 * during bootstrapping this function needs to be rewritten using some native
 * functions as prototype setup using normal JavaScript does not work as
 * expected during bootstrapping (see mirror.js in r114903).
 *
 * @param {function} ctor Constructor function which needs to inherit the
 *     prototype.
 * @param {function} superCtor Constructor function to inherit prototype from.
 */
exports.inherits = require('inherits');

exports._extend = function(origin, add) {
  // Don't do anything if add isn't an object
  if (!add || !isObject(add)) return origin;

  var keys = Object.keys(add);
  var i = keys.length;
  while (i--) {
    origin[keys[i]] = add[keys[i]];
  }
  return origin;
};

function hasOwnProperty(obj, prop) {
  return Object.prototype.hasOwnProperty.call(obj, prop);
}

}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"./support/isBuffer":18,"_process":17,"inherits":16}]},{},[15]);
