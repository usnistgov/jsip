/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.core.*;
import gov.nist.javax.sip.message.*;
import java.io.*;
import java.net.*;
import gov.nist.javax.sip.header.*;
import java.util.Hashtable;
import java.util.Enumeration;

//ifdef SIMULATION
/*
import sim.java.net.*;
import sim.java.*;
//endif
*/

/** 
 * Low level Input output to a socket. Caches TCP connections and takes care
 * of re-connecting to the remote party if the other end drops the connection
 *
 * @version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */


class IOHandler {

	private SIPStack sipStack;

	private static String UDP = "udp";
	private static String TCP = "tcp";

	// A cache of client sockets that can be re-used for 
	// sending tcp messages.
	private Hashtable socketTable;

	protected static String makeKey(InetAddress addr, int port) {
		return addr.getHostAddress() + ":" + port;

	}

	protected IOHandler(SIPStack sipStack) {
		this.sipStack = sipStack;
		this.socketTable = new Hashtable();
	}

//ifdef SIMULATION
/*
		private synchronized SimSocket getSocket(String key) {
			return (SimSocket) socketTable.get(key);
		}
	        protected synchronized void putSocket(String key, 
	                SimSocket sock) {
			//System.out.println("putSocket " + key + ":" + sock);
			socketTable.put(key,sock);
		}
//else
*/
	protected synchronized void putSocket(String key, Socket sock) {
		socketTable.put(key, sock);
	}
	private synchronized Socket getSocket(String key) {
		return (Socket) socketTable.get(key);
	}
//endif
//

	private void removeSocket(String key) {
		socketTable.remove(key);
	}

	/**
	 * Forward a given request to the address given. This caches
	 * connections for tcp sends.
	 * @param inaddr is the address to which to send the request.
	 * @param port is the port to send to.
	 * @param transport is the transport string udp or tcp.
	 * @param nrequest is the request that is being forwarded	    
	 * For udp we do a connect and a send as specified in tbe RFC 
	 * so that an error is returned immediately if the other end is 
	 * not listening
	 * @throws IOException If the message could not be sent for any reason
	 */
//ifdef SIMULATION
/*
	
		public 
	        SimSocket 
	        sendBytes( InetAddress inaddr, 
			int contactPort, 
	                String transport, 
	                byte[] bytes, boolean retry ) 
	        throws IOException {
			 int retry_count = 0;
			 int max_retry = retry? 2: 1;
			// Server uses TCP transport. TCP client sockets are cached
			int length = bytes.length;
			if (transport.compareToIgnoreCase(TCP) == 0 ) {
			   String key = makeKey(inaddr,contactPort);
	                   SimSocket    clientSock = getSocket(key);
			    retry:
				while(retry_count < max_retry) {
				    if (clientSock == null) {
					if (sipStack.logWriter.needsLogging) {
					   sipStack.logWriter.logMessage
					    ("inaddr = " + inaddr);
					   sipStack.logWriter.logMessage
					    ("port = " + contactPort);
					}
				        clientSock = new SimSocket
						(inaddr,contactPort);
				        OutputStream outputStream  = 
						clientSock.getOutputStream();
					synchronized(outputStream) {
				          outputStream.write(bytes,0,length);
				          outputStream.flush();
					}
					putSocket (key,clientSock);
				        break;
				    } else {
				       try {
				          OutputStream outputStream  = 
						clientSock.getOutputStream();
					 synchronized(outputStream) {
				            outputStream.write(bytes,0,length);
				            outputStream.flush();
					 }
					 break;
				       } catch (IOException ex) {
					  System.out.println("key = " + key);
					  ex.printStackTrace();
					  // old connection is bad.
					  // remove from our table.
					  removeSocket(key);
					  clientSock.close();
				          clientSock = null;
					  retry_count ++;
				          break retry;
				       }
				   }
				}
	                        return clientSock;
	
			} else {
				// This is a UDP transport...
				DatagramSocket datagramSock = new DatagramSocket();
				datagramSock.connect(inaddr, contactPort);
				DatagramPacket dgPacket = 
					new DatagramPacket
					  (bytes, 0, length, inaddr, contactPort);
				datagramSock.send(dgPacket);
				datagramSock.close();
	                        return null;
			}
	
		}
//else
*/

	private void writeChunks(OutputStream outputStream, byte[] bytes, int length) 
		throws IOException {
		int chunksize = 4096;
		for (int p = 0; p < length; p += chunksize )  {
			int chunk = p + chunksize < length? chunksize: length - p;
			outputStream.write(bytes, p, chunk);
			outputStream.flush();
		}
	}

	public Socket sendBytes(
		InetAddress inaddr,
		int contactPort,
		String transport,
		byte[] bytes,
		boolean retry)
		throws IOException {
		int retry_count = 0;
		int max_retry = retry ? 2 : 1;
		// Server uses TCP transport. TCP client sockets are cached
		int length = bytes.length;
		if (sipStack.logWriter.needsLogging)  {
			sipStack.logWriter.logMessage
			("sendBytes " + transport + " inAddr " + inaddr.getHostAddress() +
				" port = " + contactPort  +  " length = " + length );
		}
		if (transport.compareToIgnoreCase(TCP) == 0) {
			String key = makeKey(inaddr, contactPort);
			Socket clientSock = getSocket(key);
			retry : while (retry_count < max_retry) {
				if (clientSock == null) {
					if (LogWriter.needsLogging) {
						sipStack.logWriter.logMessage("inaddr = " + inaddr);
						sipStack.logWriter.logMessage("port = " + contactPort);
					}
					clientSock = new Socket(inaddr, contactPort);
					OutputStream outputStream = clientSock.getOutputStream();
					writeChunks(outputStream, bytes,length);
					putSocket(key, clientSock);
					break;
				} else {
					try {
						OutputStream outputStream =
							clientSock.getOutputStream();
						writeChunks(outputStream,bytes,length);
						break;
					} catch (IOException ex) {
						if (LogWriter.needsLogging)
							sipStack.logWriter.logException(ex);
						// old connection is bad.
						// remove from our table.
						removeSocket(key);
						clientSock.close();
						clientSock = null;
						retry_count++;
						break retry;
					}
				}
			}
			if (clientSock == null) {
				throw new IOException(
					"Could not connect to " + inaddr + ":" + contactPort);
			} else
				return clientSock;

		} else {
			// This is a UDP transport...
			DatagramSocket datagramSock = new DatagramSocket();
			datagramSock.connect(inaddr, contactPort);
			DatagramPacket dgPacket =
				new DatagramPacket(bytes, 0, length, inaddr, contactPort);
			datagramSock.send(dgPacket);
			datagramSock.close();
			return null;
		}

	}

//endif
//

	/**
	 * Send a request when you have a host and port string 
	 * @param host is the host name/address
	 * @param port is the port
	 * @param stack is the sipStack from where this message is originating (for logging purposes).
	 * @param message is the SIP message that we are forwardiong.
	 */
	public void sendRequest(
		String host,
		int port,
		String transport,
		SIPMessage message)
		throws IOException {

		String firstLine = null;
		if (message instanceof SIPRequest) {
			SIPRequest request = (SIPRequest) message;
			firstLine = request.getRequestLine().encode();
		} else {
			SIPResponse response = (SIPResponse) message;
			firstLine = response.getStatusLine().encode();
		}
		InetAddress inetAddr = InetAddress.getByName(host);
		sendBytes(
			inetAddr,
			port,
			transport,
			message.encodeAsBytes(),
			message instanceof SIPRequest);

		if (sipStack.serverLog.needsLogging(ServerLog.TRACE_MESSAGES)) {
			String status = null;
			sipStack.serverLog.logMessage(
				message.encode(),
				sipStack.getHostAddress() + ":" + sipStack.getPort(transport),
				host + ":" + transport + port,
				true,
				((CallID) message.getCallId()).encodeBody(),
				firstLine,
				status,
				message.getTransactionId());
		}

	}

	/**
	 * Close all the cached connections.
	 */
	public void closeAll() {
		for (Enumeration values = socketTable.elements();
			values.hasMoreElements();
			) {
			Socket s = (Socket) values.nextElement();
			try {
				s.close();
			} catch (IOException ex) {
			}
		}

	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.12  2004/03/18 22:01:20  mranga
 * Reviewed by:   mranga
 * Get rid of the PipedInputStream from pipelined parser to avoid a copy.
 *
 * Revision 1.11  2004/03/07 22:25:24  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
 * Revision 1.10  2004/01/22 18:39:41  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.9  2004/01/22 13:26:33  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
