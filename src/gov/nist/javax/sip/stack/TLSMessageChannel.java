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
/* This class is entirely derived from TCPMessageChannel,
 * by making some minor changes. Daniel J. Martinez Manzano <dani@dif.um.es>
 * made these changes. Ahmet Uyar
 * <auyar@csit.fsu.edu>sent in a bug report for TCP operation of the
 * JAIN sipStack. Niklas Uhrberg suggested that a mechanism be added to
 * limit the number of simultaneous open connections. The TLS
 * Adaptations were contributed by Daniel Martinez. Hagai Sela
 * contributed a bug fix for symmetric nat. Jeroen van Bemmel
 * added compensation for buggy clients ( Microsoft RTC clients ).
 * Bug fixes by viswashanti.kadiyala@antepo.com, Joost Yervante Damand
 * Lamine Brahimi (IBM Zurich) sent in a bug fix - a thread was being uncessarily created.
 * Carolyn Beeton (Avaya) bug fix for ssl handshake exception.
 */

/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.SIPMessage;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;

/**
 * This is sipStack for TLS connections. This abstracts a stream of parsed
 * messages. The SIP sipStack starts this from the main SIPStack class for each
 * connection that it accepts. It starts a message parser in its own thread and
 * talks to the message parser via a pipe. The message parser calls back via the
 * parseError or processMessage functions that are defined as part of the
 * SIPMessageListener interface.
 *
 * @see gov.nist.javax.sip.parser.PipelinedMsgParser
 *
 *
 * @author M. Ranganathan
 *
 *
 * @version 1.2 $Revision: 1.43 $ $Date: 2010-12-02 22:44:53 $
 */
public class TLSMessageChannel extends ConnectionOrientedMessageChannel {
    private static StackLogger logger = CommonLogger.getLogger(TLSMessageChannel.class);            

    private HandshakeCompletedListener handshakeCompletedListener;
	private boolean handshakeCompleted = false;
   
    /**
     * Constructor - gets called from the SIPStack class with a socket on
     * accepting a new client. All the processing of the message is done here
     * with the sipStack being freed up to handle new connections. The sock
     * input is the socket that is returned from the accept. Global data that is
     * shared by all threads is accessible in the Server structure.
     *
     * @param sock
     *            Socket from which to read and write messages. The socket is
     *            already connected (was created as a result of an accept).
     *
     * @param sipStack
     *            Ptr to SIP Stack
     *
     * @param msgProcessor
     *            -- the message processor that created us.
     */

    protected TLSMessageChannel(Socket sock, SIPTransactionStack sipStack,
            TLSMessageProcessor msgProcessor, String threadName) throws IOException {
    	
    	super(sipStack);
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug(
                    "creating new TLSMessageChannel (incoming)");
            logger.logStackTrace();
        }

        mySock = (SSLSocket) sock;
        if (sock instanceof SSLSocket) {
            SSLSocket sslSock = (SSLSocket) sock;
            if(sipStack.getClientAuth() != ClientAuthType.Want && sipStack.getClientAuth() != ClientAuthType.Disabled && sipStack.getClientAuth() != ClientAuthType.DisabledAll) {
                sslSock.setNeedClientAuth(true);
            }
            if(logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                logger.logDebug("SSLServerSocket need client auth " + sslSock.getNeedClientAuth());
            }
        }

        peerAddress = mySock.getInetAddress();
        myAddress = msgProcessor.getIpAddress().getHostAddress();
        myClientInputStream = mySock.getInputStream();

        mythread = new Thread(this);
        mythread.setDaemon(true);
        mythread.setName(threadName);

        this.myPort = msgProcessor.getPort();
        this.peerPort = mySock.getPort();
        this.key = MessageChannel.getKey(peerAddress, peerPort, "TLS");
        // Bug report by Vishwashanti Raj Kadiayl
        super.messageProcessor = msgProcessor;
        // Can drop this after response is sent potentially.
        mythread.start();
    }

    /**
     * Constructor - connects to the given inet address.
     *
     * @param inetAddr
     *            inet address to connect to.
     * @param sipStack
     *            is the sip sipStack from which we are created.
     * @param messageProcessor
     *            -- the message processor that created us.
     * @throws IOException
     *             if we cannot connect.
     */
    protected TLSMessageChannel(InetAddress inetAddr, int port,
            SIPTransactionStack sipStack, TLSMessageProcessor messageProcessor)
            throws IOException {
    	
    	super(sipStack);
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug(
                    "creating new TLSMessageChannel (outgoing)");
            logger.logStackTrace();
        }
        this.peerAddress = inetAddr;
        this.peerPort = port;
        this.myPort = messageProcessor.getPort();
        this.peerProtocol = "TLS";
        this.sipStack = sipStack;
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.key = MessageChannel.getKey(peerAddress, peerPort, "TLS");
        super.messageProcessor = messageProcessor;

    }   

    /**
     * Close the message channel.
     */
    public void close(boolean removeSocket, boolean stopKeepAliveTask) {  
		isRunning = false;
    	// we need to close everything because the socket may be closed by the other end
    	// like in LB scenarios sending OPTIONS and killing the socket after it gets the response    	
        if (mySock != null) {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("Closing socket " + key);
        	try {
	            mySock.close();
        	} catch (IOException ex) {
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger.logDebug("Error closing socket " + ex);
            }
        }        
        if(myParser != null) {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("Closing my parser " + myParser);
            myParser.close();            
        }           
        if(removeSocket) {    
	        // remove the "tls:" part of the key to cleanup the ioHandler hashmap
	        String ioHandlerKey = key.substring(4);
	        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
	            logger.logDebug("Closing TLS socket " + ioHandlerKey);
	        // Issue 358 : remove socket and semaphore on close to avoid leaking
	        sipStack.ioHandler.removeSocket(ioHandlerKey);

            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("Closing message Channel (key = " + key +")" + this);
            }
        } else {
    		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    			String ioHandlerKey = key.substring(4);
	    		logger.logDebug("not removing socket key from the cached map since it has already been updated by the iohandler.sendBytes " + ioHandlerKey);
    		}
    	}
        if(stopKeepAliveTask) {
			cancelPingKeepAliveTimeoutTaskIfStarted();
		}	    	        
    }    

    /**
     * get the transport string.
     *
     * @return "tcp" in this case.
     */
    public String getTransport() {
        return "TLS";
    }    

    /**
     * Send message to whoever is connected to us. Uses the topmost via address
     * to send to.
     *
     * @param msg
     *            is the message to send.
     * @param retry
     */
    protected synchronized void sendMessage(byte[] msg, boolean retry) throws IOException {

    	if ( logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
             logger.logDebug("sendMessage isClient  = " + retry);
        }
    	  Socket sock = null;
        IOException problem = null;
        try {
        	sock = this.sipStack.ioHandler.sendBytes(
                this.getMessageProcessor().getIpAddress(), this.peerAddress, this.peerPort,
                this.peerProtocol, msg, retry,this);
        } catch (IOException any) {
        	problem = any;
        	logger.logWarning("Failed to connect " + this.peerAddress + ":" + this.peerPort +" but trying the advertised port=" + this.peerPortAdvertisedInHeaders + " if it's different than the port we just failed on");
        	logger.logError("Error is ", any);
        }
        if(sock == null) { // http://java.net/jira/browse/JSIP-362 If we couldn't connect to the host, try the advertised host:port as failsafe
        	if(peerAddressAdvertisedInHeaders  != null && peerPortAdvertisedInHeaders > 0) { 
                logger.logWarning("Couldn't connect to peerAddress = " + peerAddress + " peerPort = " + peerPort + " key = " + key +  " retrying on peerPortAdvertisedInHeaders " + peerPortAdvertisedInHeaders);
                InetAddress address = InetAddress.getByName(peerAddressAdvertisedInHeaders);
                sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(),
                    address, this.peerPortAdvertisedInHeaders, this.peerProtocol, msg, retry, this);        		
        		this.peerPort = this.peerPortAdvertisedInHeaders;
        		this.peerAddress = address;
        		this.key = MessageChannel.getKey(peerAddress, peerPort, "TLS");
                logger.logWarning("retry suceeded to peerAddress = " + peerAddress + " peerPortAdvertisedInHeaders = " + peerPortAdvertisedInHeaders + " key = " + key);
        	} else {
        		throw problem; // throw the original excpetion we had from the first attempt
        	}
        }
        // Created a new socket so close the old one and stick the new
        // one in its place but dont do this if it is a datagram socket.
        // (could have replied via udp but received via tcp!).
        if (sock != mySock && sock != null) {
          	 if (mySock != null) {
          		 if(logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
          			 logger.logWarning(
                       		 "Old socket different than new socket on channel " + key);
   		             logger.logStackTrace();
   		             logger.logWarning(
   		            		 "Old socket local ip address " + mySock.getLocalSocketAddress());
   		             logger.logWarning(
   		            		 "Old socket remote ip address " + mySock.getRemoteSocketAddress());                         
   		             logger.logWarning(
   		            		 "New socket local ip address " + sock.getLocalSocketAddress());
   		             logger.logWarning(
   		            		 "New socket remote ip address " + sock.getRemoteSocketAddress());
          		 }
          		 close(false, false);
          	}    
          	if(problem == null) {
          		if(mySock != null) {
   	        		if(logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
   	        			logger.logWarning(
   	                		 "There was no exception for the retry mechanism so creating a new thread based on the new socket for incoming " + key);
   	        		}
          		}
   	            mySock = sock;
   	            this.myClientInputStream = mySock.getInputStream();
   	            Thread thread = new Thread(this);
   	            thread.setDaemon(true);
   	            thread.setName("TCPMessageChannelThread");
   	            thread.start();
          	} else {
          		if(logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
          			logger.logWarning(
          					"There was an exception for the retry mechanism so not creating a new thread based on the new socket for incoming " + key);
          		}
          		mySock = sock;
          	}
          }

    }

    /**
     * Send a message to a specified address.
     *
     * @param message
     *            Pre-formatted message to send.
     * @param receiverAddress
     *            Address to send it to.
     * @param receiverPort
     *            Receiver port.
     * @throws IOException
     *             If there is a problem connecting or sending.
     */
    public synchronized void sendMessage(byte message[], InetAddress receiverAddress,
            int receiverPort, boolean retry) throws IOException {
        if (message == null || receiverAddress == null)
            throw new IllegalArgumentException("Null argument");

        if(peerPortAdvertisedInHeaders <= 0) {
        	if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            	logger.logDebug("receiver port = " + receiverPort + " for this channel " + this + " key " + key);
            }        	
        	if(receiverPort <=0) {    
        		// if port is 0 we assume the default port for TCP
        		this.peerPortAdvertisedInHeaders = 5060;
        	} else {
        		this.peerPortAdvertisedInHeaders = receiverPort;
        	}
        	if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            	logger.logDebug("2.Storing peerPortAdvertisedInHeaders = " + peerPortAdvertisedInHeaders + " for this channel " + this + " key " + key);
            }	        
        }
        
        Socket sock = null;
        IOException problem = null;
        try {
        	sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(),
                receiverAddress, receiverPort, "TLS", message, retry, this);
        } catch (IOException any) {
        	problem = any;
        	logger.logWarning("Failed to connect "
        			+ this.peerAddress + ":" + receiverPort +
        			" but trying the advertised port=" + 
        			this.peerPortAdvertisedInHeaders + 
        			" if it's different than the port we just failed on, rcv addr=" +
        			receiverAddress + ", port=" + receiverPort);
        	logger.logError("Error is ", any);
        }
        if(sock == null) { // http://java.net/jira/browse/JSIP-362 If we couldn't connect to the host, try the advertised host:port as failsafe
        	if(peerAddressAdvertisedInHeaders  != null && peerPortAdvertisedInHeaders > 0) {
        		logger.logWarning("Couldn't connect to receiverAddress = " + receiverAddress + " receiverPort = " + receiverPort + " key = " + key +  " retrying on peerPortAdvertisedInHeaders " + peerPortAdvertisedInHeaders);
        		InetAddress address = InetAddress.getByName(peerAddressAdvertisedInHeaders);
            	sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(),
            			address, this.peerPortAdvertisedInHeaders, "TLS", message, retry, this);
        		this.peerPort = this.peerPortAdvertisedInHeaders;
        		this.peerAddress = address;
        		this.key = MessageChannel.getKey(peerAddress, peerPortAdvertisedInHeaders, "TLS");
                
                logger.logWarning("retry suceeded to receiverAddress = " + receiverAddress + " peerPortAdvertisedInHeaders = " + peerPortAdvertisedInHeaders + " key = " + key);
        	} else {
        		throw problem; // throw the original excpetion we had from the first attempt
        	}
        }
        //
        // Created a new socket so close the old one and s
        // Check for null (bug fix sent in by Christophe)
        if (sock != mySock && sock != null) {        	        	
            if (mySock != null) {
            	if(logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
       			 	 logger.logWarning(
                    		 "Old socket different than new socket on channel " + key);
		             logger.logStackTrace();
		             logger.logWarning(
		            		 "Old socket local ip address " + mySock.getLocalSocketAddress());
		             logger.logWarning(
		            		 "Old socket remote ip address " + mySock.getRemoteSocketAddress());                         
		             logger.logWarning(
		            		 "New socket local ip address " + sock.getLocalSocketAddress());
		             logger.logWarning(
		            		 "New socket remote ip address " + sock.getRemoteSocketAddress());
       		 	}
            	// we can't delay the close otherwise it will close the previous socket we just set
            	close(false, false);
            }
            if(problem == null) {
            	if (mySock != null) {
            		if(logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
            			logger.logWarning(
            					"There was no exception for the retry mechanism so creating a new thread based on the new socket for incoming " + key);
            		}
            	}
	            mySock = sock;
	            this.myClientInputStream = mySock.getInputStream();
	            // start a new reader on this end of the pipe.
	            Thread mythread = new Thread(this);
	            mythread.setDaemon(true);
	            mythread.setName("TCPMessageChannelThread");
	            mythread.start();
            } else {
            	if(logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
            		logger.logWarning(
            			"There was an exception for the retry mechanism so not creating a new thread based on the new socket for incoming " + key);
            	}
            	mySock = sock;
            }
        }

    }

    /**
     * Exception processor for exceptions detected from the parser. (This is
     * invoked by the parser when an error is detected).
     *
     * @param sipMessage
     *            -- the message that incurred the error.
     * @param ex
     *            -- parse exception detected by the parser.
     * @param header
     *            -- header that caused the error.
     * @throws ParseException
     *             Thrown if we want to reject the message.
     */
    public void handleException(ParseException ex, SIPMessage sipMessage,
            Class hdrClass, String header, String message)
            throws ParseException {
        if (logger.isLoggingEnabled())
            logger.logException(ex);
        // Log the bad message for later reference.
        if ((hdrClass != null)
                && (hdrClass.equals(From.class) || hdrClass.equals(To.class)
                        || hdrClass.equals(CSeq.class)
                        || hdrClass.equals(Via.class)
                        || hdrClass.equals(CallID.class)
                        || hdrClass.equals(ContentLength.class)
                        || hdrClass.equals(RequestLine.class) || hdrClass
                        .equals(StatusLine.class))) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug(
                        "Encountered bad message \n" + message);
            // JvB: send a 400 response for requests (except ACK)
            String msgString = sipMessage.toString();
            if (!msgString.startsWith("SIP/") && !msgString.startsWith("ACK ")) {

                String badReqRes = createBadReqRes(msgString, ex);
                if (badReqRes != null) {
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger.logDebug(
                                "Sending automatic 400 Bad Request:");
                        logger.logDebug(badReqRes);
                    }
                    try {
                        this.sendMessage(badReqRes.getBytes(), this
                                .getPeerInetAddress(), this.getPeerPort(),
                                false);
                    } catch (IOException e) {
                        logger.logException(e);
                    }
                } else {
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger
                                .logDebug(
                                        "Could not formulate automatic 400 Bad Request");
                    }
                }
            }
            throw ex;
        } else {
            sipMessage.addUnparsed(header);
        }
    }       

    /**
     * Equals predicate.
     *
     * @param other
     *            is the other object to compare ourselves to for equals
     */

    public boolean equals(Object other) {

        if (!this.getClass().equals(other.getClass()))
            return false;
        else {
            TLSMessageChannel that = (TLSMessageChannel) other;
            if (this.mySock != that.mySock)
                return false;
            else
                return true;
        }
    }

    /**
     * TLS Is a secure protocol.
     */
    public boolean isSecure() {
        return true;
    }

    public void setHandshakeCompletedListener(
            HandshakeCompletedListener handshakeCompletedListenerImpl) {
        this.handshakeCompletedListener = handshakeCompletedListenerImpl;
    }

    /**
     * @return the handshakeCompletedListener
     */
    public HandshakeCompletedListenerImpl getHandshakeCompletedListener() {
        return (HandshakeCompletedListenerImpl) handshakeCompletedListener;
    }  
    
    @Override
    public void run() {
    	// http://java.net/jira/browse/JSIP-415 
    	// moved from constructor to the TCPMessageChannel thread to avoid blocking the Processor if the handshake is stuck because of a bad client
    	if (mySock!= null && mySock instanceof SSLSocket && !handshakeCompleted) {
    		SSLSocket sslSock = (SSLSocket) mySock;
    		HandshakeCompletedListenerImpl listener = new HandshakeCompletedListenerImpl(this, sslSock);
            this.handshakeCompletedListener = listener;
            sslSock.addHandshakeCompletedListener(this.handshakeCompletedListener);
            listener.startHandshakeWatchdog();			
			try {
				sslSock.startHandshake();
				handshakeCompleted = true;
				if(!getSIPStack().isSslRenegotiationEnabled()) {
					/* 
					 * disable TLS re-negotiations to avoid DOS attack
					 *  http://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2011-1473 
					 *  http://www.ietf.org/mail-archive/web/tls/current/msg07553.html
					 *  http://192.9.162.55/javase/javaseforbusiness/docs/TLSReadme.html 
					 *  */ 
					sslSock.getSession().invalidate(); 
					sslSock.setEnableSessionCreation(false);
				}
			} catch (IOException e) {
				logger.logError("A problem occured while Accepting connection", e);
				// https://code.google.com/p/jain-sip/issues/detail?id=14 clean up
                sslSock.removeHandshakeCompletedListener(handshakeCompletedListener);
                handshakeCompletedListener = null;
                try {
                        myClientInputStream.close();
                } catch (IOException e1) {
                        // ignore
                } finally {
                        myClientInputStream = null;
                }

                try {
                        mySock.close();
                } catch (IOException e1) {
                        // ignore
                } finally {
                        mySock = null;
                }

                return;
			}			
        }
    	super.run();
    }

	/**
	 * @return the handshakeCompleted
	 */
	public boolean isHandshakeCompleted() {
		return handshakeCompleted;
	}

	/**
	 * @param handshakeCompleted the handshakeCompleted to set
	 */
	public void setHandshakeCompleted(boolean handshakeCompleted) {
		this.handshakeCompleted = handshakeCompleted;
	}
}
