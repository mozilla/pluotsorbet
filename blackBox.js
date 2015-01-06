/*
 * This script can be black-boxed in the debugger to disable breaking
 * at unwanted exception points.
 */

function throwHelper(e) {
  J2ME.traceWriter && J2ME.traceWriter.writeLn("Throw " + e);
  throw e;
}

function throwPause() {
  throwHelper(VM.Pause);
}