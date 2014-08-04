package com.ibm.oti.connection;

import java.io.IOException;

import javax.microedition.io.Connection;

/**
 * Stub for IBM J9 implementation.
 * <p>
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2000, 2002  All Rights Reserved
 * <p>
 * This class is not distributed with BlueCove binary distribution: bluecove.jar
 */
public interface CreateConnection extends Connection {
    
	/**
	 * Passes the parameters from the Connector.open() method to this
	 * object. Protocol used by MIDP 1.0
	 *
	 * @author		OTI
	 * @version		initial
	 *
	 * @param		spec String
	 *					The address passed to Connector.open()
	 * @param		access int
	 *					The type of access this Connection is
	 *					granted (READ, WRITE, READ_WRITE)
	 * @param		timeout boolean
	 *					A boolean indicating wether or not the
	 *					caller to Connector.open() wants timeout
	 *					exceptions or not
	 * @exception	IOException
	 *					If an error occured opening and configuring
	 *					serial port.
	 *
	 * @see javax.microedition.io.Connector
	 */
	public void setParameters(String spec, int access, boolean timeout) throws IOException;
    
	/**
	 * Passes the parameters from the Connector.open() method to this
	 * object. Protocol used by MIDP 2.0
	 *
	 * @author		OTI
	 * @version		initial
	 *
	 * @param		spec String
	 *					The address passed to Connector.open()
	 * @param		access int
	 *					The type of access this Connection is
	 *					granted (READ, WRITE, READ_WRITE)
	 * @param		timeout boolean
	 *					A boolean indicating wether or not the
	 *					caller to Connector.open() wants timeout
	 *					exceptions or not
	 * @exception	IOException
	 *					If an error occured opening and configuring
	 *					serial port.
	 *
	 * @see javax.microedition.io.Connector
	 */
	public Connection setParameters2(String spec, int access, boolean timeout) throws IOException;
    
}
