#!/usr/bin/env python

import os
import select
import socket
import subprocess
import sys
import time

# The test automation scripts to run via casperjs/slimerjs.
automation_scripts = [
    '/tests/automation.js',
    '/tests/fs/automation.js',
]

# The exit code to return.  We set this to 1 if an automation script outputs
# "FAIL" (case-sensitive) at any point in the test run.  Ideally, we'd determine
# the success/failure based on the exit code of the test run, but casperjs
# with slimerjs always returns 0, so instead we must look for the string "FAIL",
# which only occurs on test failure.
#
# See https://github.com/laurentj/slimerjs/issues/50 for more information.
#
exit_code = 0

# Open the server processes that handle HTTP/S requests and socket connections.
# We pipe their standard output/error back to the parent process to print it
# to the parent's standard output interspersed with the output produced by
# the automation scripts (which we have to pipe, instead of letting them print
# their output directly, so we can look for "FAIL", as described above).
#
# We don't actually pipe anything into standard input, but we have to specify
# that a new pipe should be created for it, otherwise for some reason it causes
# print/sys.stdout.write in the parent process to throw the exception "IOError:
# [Errno 35] Resource temporarily unavailable" on large amounts of output.
#
server_processes = [
    subprocess.Popen('tests/httpServer.py', stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                     bufsize=1),
    subprocess.Popen('tests/echoServer.py', stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                     bufsize=1),

    # The SSL-based servers need to have their current working directory set
    # to the tests/ subdirectory, since they load cert/key files relative to it.
    subprocess.Popen('./httpsServer.py', stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                     bufsize=1, cwd='tests'),
    subprocess.Popen('./sslEchoServer.py', stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                     bufsize=1, cwd='tests'),
]

# The output streams for the servers.
server_output_streams = [p.stdout for p in server_processes]

def wait_server(port):
    end = time.time() + 30 # Timeout of 30 seconds

    while True:
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(('', port))
            s.close()
            return
        except:
            if end < time.time():
                raise Exception("Can't connect to " + str(port))
            else:
                time.sleep(1)

def run_test(script_path):
  script_process = subprocess.Popen(['casperjs', '--engine=slimerjs', 'test', script_path],
                                    stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, bufsize=1)
  output_streams = list(server_output_streams)
  output_streams.append(script_process.stdout)

  while True:
      readable_streams, _, _ = select.select(output_streams, [], [])

      for stream in readable_streams:
          line = stream.readline()

          if stream is script_process.stdout and "FAIL" in line:
            exit_code = 1

          sys.stdout.write(line)

      if script_process.poll() is not None:
          # Print any famous last words the process wrote to its output stream
          # between the last time we polled it and its termination.
          sys.stdout.write(script_process.stdout.read())

          break

# Wait for the servers to become ready for connections.
wait_server(8000)
wait_server(50003)
wait_server(4443)
wait_server(54443)

# Run each test automation script in turn.
for script in automation_scripts:
    run_test(os.getcwd() + script);

# Terminate all the server processes.
for process in server_processes:
    process.terminate()

# Print any famous last words the processes wrote to their output streams
# between the last time we polled them and their termination.
for stream in server_output_streams:
    sys.stdout.write(stream.read())

sys.exit(exit_code)
