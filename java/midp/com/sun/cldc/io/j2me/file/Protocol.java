package com.sun.cldc.io.j2me.file;

import com.sun.cldc.io.ConnectionBaseInterface;
import java.io.IOException;

import com.ibm.oti.connection.file.Connection;

public class Protocol implements ConnectionBaseInterface {
  public javax.microedition.io.Connection openPrim(String name, int mode, boolean timeouts) throws IOException {
  	Connection conn = new Connection();
  	return conn.setParameters2(name, mode, timeouts);
  }
}
