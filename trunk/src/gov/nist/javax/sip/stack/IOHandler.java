/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

/** Low level Input output to a socket. Caches TCP connections and takes care
* of re-connecting to the remote party if the other end drops the connection
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/


package gov.nist.javax.sip.stack;
import gov.nist.javax.sip.message.*;
import java.io.*; 
import java.net.*;
import gov.nist.javax.sip.*;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import java.util.LinkedList;
import java.util.Hashtable;
import gov.nist.core.*;
//ifdef SIMULATION
/*
import sim.java.net.*;
import sim.java.*;
//endif
*/

/**
*  Class that is used for forwarding SIP requests. 
*/

class IOHandler
{

	private SIPStack sipStack;

	private static String UDP = "udp";
	private static String TCP = "tcp";

	// A cache of client sockets that can be re-used for 
	// sending tcp messages.
	private Hashtable socketTable;


	protected static String makeKey(InetAddress addr, int port) {
		return addr.getHostAddress() +":"+port;

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
		socketTable.put(key,sock);
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
         * Forward a given request to the address given. 
         * The address has information on
         * the type of transport etc. used to talk to it.
         * @param addr is the address to which to send the request to
         * @param request is the request that is being forwarded
         * If the address does not specify a transport, we 
         * try UDP first and if this  fails, then try TCP.
         * @throws IOException If the message could not be sent for any
         * reason
         */
	public void sendRequest(AddressImpl addr, 
       					String request) throws IOException 
	{
		HostPort hostPort = addr.getHostPort();
		String transport = 
                    ((SipUri)addr.getURI()).getTransportParam();
		if (transport == null) {
	       		transport = SIPConstants.UDP;
			try {
				sendRequest(hostPort,transport,request); 
			} catch (IOException ex) {
				if (sipStack.logWriter.needsLogging) {
				   sipStack.logWriter.logException(ex);
				   sipStack.logWriter.logMessage
					("UDP Send failed!");
				}
				sendRequest(hostPort, TCP, request);
			}
		} else { 
			sendRequest(hostPort,transport,request);
		}
				
	}
	
		

	/**
         * Forward a given request to the address given. 
         * The address has information on
         * the type of transport etc. used to talk to it.
         * @param addr is the address to which to send the request.
         * @param transport is the transport string udp or tcp.
         * @param nrequest is the request that is being forwarded	    
         * For udp we do a connect and a send as specified in tbe RFC 
         * so that an error is returned immediately if the other end is 
         * not listening
         * @throws IOException If the message could not be sent for any reason
         */
	
	public void sendRequest(HostPort addr, 
				String transport,
				String nrequest)  throws IOException 
	{      
		String request = nrequest;

		String hostName = addr.getHost().getHostname();
		InetAddress inaddr =  InetAddress.getByName(hostName);
		int contactPort = addr.getPort(); 		
		if (contactPort == -1) {
		    contactPort = 5060;
		}
		sendRequest(inaddr,contactPort,transport,request);
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
                byte[] bytes) 
        throws IOException {
		 int retry_count = 0;
		// Server uses TCP transport. TCP client sockets are cached
		int length = bytes.length;
		if (transport.compareToIgnoreCase(TCP) == 0 ) {
		   String key = makeKey(inaddr,contactPort);
                   SimSocket    clientSock = getSocket(key);
		    retry:
			while(retry_count < 2) {
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
	public  Socket sendBytes(InetAddress inaddr, 
			int contactPort, 
                        String transport, 
                        byte[] bytes) 
        throws IOException {
		 int retry_count = 0;
		// Server uses TCP transport. TCP client sockets are cached
		int length = bytes.length;
		if (transport.compareToIgnoreCase(TCP) == 0 ) {
		   String key = makeKey(inaddr,contactPort);
		    Socket clientSock = getSocket(key);
		    retry:
			while(retry_count < 2) {
			    if (clientSock == null) {
				if (sipStack.logWriter.needsLogging) {
				   sipStack.logWriter.logMessage
				   ("inaddr = " + inaddr);
				   sipStack.logWriter.logMessage("port = " 
					+ contactPort);
				}
			        clientSock = new Socket(inaddr,contactPort);
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

//endif
//


	/**
         * Forward a given request to the address given. 
         * The address has information on
         * the type of transport etc. used to talk to it.
         * @param inaddr is the address to which to send the request.
         * @param port is the port to send to.
         * @param transport is the transport string udp or tcp.
         * @param nrequest is the request that is being forwarded	    
         * For udp we do a connect and a send as specified in tbe RFC 
         * so that an error is returned immediately if the other end is 
         * not listening
         * @throws IOException If the message could not be sent for any reason
         */

	public void sendRequest(InetAddress inaddr, 
			int contactPort, 
                        String transport, 
                        String request) 
        throws IOException {
		int length = request.getBytes().length;
		byte bytes[] = request.getBytes();
                // Log some debugging information.
                if (sipStack.logWriter.needsLogging) {
                    sipStack.logWriter.logMessage("sendRequest: " 
			+  inaddr.getHostAddress() + ":"+  contactPort + "/" + 
			  transport + "length" + length);
                }
		sendBytes(inaddr,contactPort,transport,bytes);
		
	}


	/** Send a request when you have a host and port string 
        *@param host is the host name/address
        *@param port is the port
        *@param stack is the sipStack from where this message is
        *   originating (for logging purposes).
        *@param message is the SIP message that we are forwardiong.
	*/

	public void sendRequest(String host, int port, String 
				transport, 
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
	   sendRequest(inetAddr,port,transport,message.encode());
	   
	   if (sipStack.serverLog.needsLogging(ServerLog.TRACE_MESSAGES)) {
               String status = null;
	       sipStack.serverLog.logMessage(message.encode(), 
				    sipStack.getHostAddress() + ":" +
				     sipStack.getPort(transport), 
				     host+":" +transport +port,
				     true,
				     ((CallID)message.getCallId()).encodeBody(),
				     firstLine,status,
				     message.getTransactionId());
	   }

	}



}
