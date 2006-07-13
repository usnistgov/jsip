/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import java.util.StringTokenizer;
/*
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/>
 * Network Research Team (http://www-r2.u-strasbg.fr))<br/>
 * Louis Pasteur University - Strasbourg - France<br/>
 * Bug fix for correct handling of IPV6 Address added by
 * Daniel J. Martinez Manzano <dani@dif.um.es>
 */
/** 
 * Routing algorithms return a list of hops to which the request is
 * routed.
 *
 * @version 1.2 $Revision: 1.7 $ $Date: 2006-07-13 09:00:52 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 *
 
 *
 */
public class HopImpl extends Object implements javax.sip.address.Hop {
	protected String host;
	protected int port;
	protected String transport;
	
	protected boolean defaultRoute; // This is generated from the proxy addr
	protected boolean uriRoute; // This is extracted from the requestURI.

	/**
	 * Debugging println.
	 */
	public String toString() {
		return host + ":" + port + "/" + transport;
	}

	/**
	 * Create new hop given host, port and transport.
	 * @param hostName hostname
	 * @param portNumber port
	 * @param trans transport
	 */
	public HopImpl(String hostName, int portNumber, String trans) {
		host = hostName;

		// Added by Daniel J. Martinez Manzano <dani@dif.um.es>
		// for correct management of IPv6 addresses.
		if(host.indexOf(":") >= 0)
			if(host.indexOf("[") < 0)
				host = "[" + host + "]";

		port = portNumber;
		transport = trans;
	}
	
	
	/**
	 * Creates new Hop
	 * @param hop is a hop string in the form of host:port/Transport
	 * @throws IllegalArgument exception if string is not properly formatted or null.
	 */
	HopImpl(String hop) throws IllegalArgumentException {
		if (hop == null)
			throw new IllegalArgumentException("Null arg!");
		// System.out.println("hop = " + hop);
		StringTokenizer stringTokenizer = new StringTokenizer(hop + "/");
		String hostPort = stringTokenizer.nextToken("/").trim();
		transport = stringTokenizer.nextToken().trim();
		// System.out.println("Hop: transport = " + transport);
		if (transport == null)
			transport = "UDP";
		else if (transport == "")
			transport = "UDP";
		if (transport.compareToIgnoreCase("UDP") != 0
			&& transport.compareToIgnoreCase("TLS") != 0 // Added by Daniel J. Martinez Manzano <dani@dif.um.es>
			&& transport.compareToIgnoreCase("TCP") != 0) {
			System.out.println("Bad transport string " + transport);
			throw new IllegalArgumentException(hop);
		}

		String portString = null;
		//IPv6 hostport
		if (hostPort.charAt(0) == '[') {
			int rightSqBrackIndex = hostPort.indexOf(']');
			if (rightSqBrackIndex == -1)
				throw new IllegalArgumentException("Bad IPv6 reference spec");

			host = hostPort.substring(0, rightSqBrackIndex + 1);

			int portColon = hostPort.indexOf(':', rightSqBrackIndex);
			if (portColon != -1)
				try {
					portString = hostPort.substring(portColon + 1).trim();
				} catch (IndexOutOfBoundsException exc) {
					//Do nothing - handled later
				}
		}
		//IPv6 address and no port
		else if (hostPort.indexOf(':') != hostPort.lastIndexOf(":")) {
			host = '[' + hostPort + ']';
		} else //no square brackets and a single or zero colons => IPv4 hostPort
			{
			int portColon = hostPort.indexOf(':');
			if (portColon == -1)
				host = hostPort;
			else {
				host = hostPort.substring(0, portColon).trim();
				try {
					portString = hostPort.substring(portColon + 1).trim();
				} catch (IndexOutOfBoundsException exc) {
					//Do nothing - handled later
				}
			}
		}

		if (host == null || host.equals(""))
			throw new IllegalArgumentException("no host!");
		if (portString == null || portString.equals("")) {
			port = 5060;
		} else {
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Bad port spec");
			}
		}
	}

	/**
	 * Retruns the host string.
	 * @return host String
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port.
	 * @return port integer.
	 */
	public int getPort() {
		return port;
	}

	/** returns the transport string.
	 */
	public String getTransport() {
		return transport;
	}

	

	/** Return true if this is uriRoute
	 */
	public boolean isURIRoute() {
		return uriRoute;
	}

	/** Set the URIRoute flag.
	 */
	public void setURIRouteFlag() {
		uriRoute = true;
	}


	
}
