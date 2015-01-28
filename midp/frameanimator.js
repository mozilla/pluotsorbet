/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var FrameAnimator = function() {};

FrameAnimator.numRegistered = 0;

FrameAnimator.prototype._isRegistered = false;

FrameAnimator.prototype.register = function(x, y, maxFps, maxPps, listener) {
  this.x = x;
  this.y = y;
  this.maxFps = maxFps;
  this.maxPps = maxPps;
  this.listener = listener;
  this._isRegistered = true;
  ++FrameAnimator.numRegistered;
};

FrameAnimator.prototype.unregister = function() {
  this.x = null;
  this.y = null;
  this.maxFps = null;
  this.maxPps = null;
  this.listener = null;
  this._isRegistered = false;
  --FrameAnimator.numRegistered;
};

FrameAnimator.prototype.isRegistered = function() {
  return this._isRegistered;
};

Native["com/nokia/mid/ui/frameanimator/FrameAnimator.init.()V"] = function() {
  this.nativeObject = new FrameAnimator();
};

Native["com/nokia/mid/ui/frameanimator/FrameAnimator.register.(IISSLcom/nokia/mid/ui/frameanimator/FrameAnimatorListener;)Z"] = function(x, y, maxFps, maxPps, listener) {
  if (this.nativeObject.isRegistered()) {
    throw $.newIllegalStateException("FrameAnimator already registered");
  }

  if (!listener) {
    throw $.newNullPointerException("listener is null");
  }

  if (x < -65535 || x > 65535 || y < -65535 || y > 65535) {
    throw $.newIllegalArgumentException("coordinate out of bounds");
  }

  // XXX return false if FrameAnimator.numRegistered >= FRAME_ANIMATOR_MAX_CONCURRENT

  this.nativeObject.register(x, y, maxFps, maxPps, listener);
  return 1;
};

Native["com/nokia/mid/ui/frameanimator/FrameAnimator.unregister.()V"] = function() {
  if (!this.nativeObject.isRegistered()) {
    throw $.newIllegalStateException("FrameAnimator not registered");
  }

  this.nativeObject.unregister();
};

addUnimplementedNative("com/nokia/mid/ui/frameanimator/FrameAnimator.drag.(II)V");
addUnimplementedNative("com/nokia/mid/ui/frameanimator/FrameAnimator.kineticScroll.(IIIF)V");
addUnimplementedNative("com/nokia/mid/ui/frameanimator/FrameAnimator.limitedKineticScroll.(IIIFII)V");
addUnimplementedNative("com/nokia/mid/ui/frameanimator/FrameAnimator.stop.()V");

Native["com/nokia/mid/ui/frameanimator/FrameAnimator.isRegistered.()Z"] = function() {
  return this.nativeObject.isRegistered() ? 1 : 0;
};

Native["com/nokia/mid/ui/frameanimator/FrameAnimator.getNumRegisteredFrameAnimators.()I"] = function() {
  return FrameAnimator.numRegistered;
};
