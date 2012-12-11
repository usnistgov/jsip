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
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.LogLevels;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public abstract class ConnectionOrientedMessageProcessor extends MessageProcessor {

	private static StackLogger logger = CommonLogger.getLogger(ConnectionOrientedMessageProcessor.class);
	
	protected int nConnections;

    protected boolean isRunning;

    protected final Map<String, ConnectionOrientedMessageChannel> messageChannels;

    protected final Map<String, ConnectionOrientedMessageChannel> incomingMessageChannels;

    protected ServerSocket sock;

    protected int useCount;
	
	public ConnectionOrientedMessageProcessor(InetAddress ipAddress, int port,
			String transport, SIPTransactionStack sipStack) {
		super(ipAddress, port, transport, sipStack);
		
	    this.sipStack = sipStack;
	    
		this.messageChannels = new ConcurrentHashMap <String, ConnectionOrientedMessageChannel>();
        this.incomingMessageChannels = new ConcurrentHashMap <String, ConnectionOrientedMessageChannel>();
	}
 	
	 /**
     * Returns the stack.
     * 
     * @return my sip stack.
     */
    public SIPTransactionStack getSIPStack() {
        return sipStack;
    }
    
    protected synchronized void remove(ConnectionOrientedMessageChannel messageChannel) {

        String key = messageChannel.getKey();
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug(Thread.currentThread() + " removing " + key + " for processor " + getIpAddress()+ ":" + getPort() + "/" + getTransport());
        }

        /** May have been removed already */
        if (messageChannels.get(key) == messageChannel)
            this.messageChannels.remove(key);
        
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug(Thread.currentThread() + " Removing incoming channel " + key + " for processor " + getIpAddress()+ ":" + getPort() + "/" + getTransport());
        incomingMessageChannels.remove(key);
    }
	
    protected synchronized void cacheMessageChannel(ConnectionOrientedMessageChannel messageChannel) {
        String key = messageChannel.getKey();
        ConnectionOrientedMessageChannel currentChannel = messageChannels.get(key);
        if (currentChannel != null) {
            if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
                logger.logDebug("Closing " + key);
            currentChannel.close();
        }
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("Caching " + key);
        this.messageChannels.put(key, messageChannel);
    }
    
    /**
     * TCP can handle an unlimited number of bytes.
     */
    public int getMaximumMessageSize() {
        return Integer.MAX_VALUE;
    }

    public boolean inUse() {
        return this.useCount != 0;
    }
    
    public boolean closeReliableConnection(String peerAddress, int peerPort) throws IllegalArgumentException {

        validatePortInRange(peerPort);

        HostPort hostPort = new HostPort();
        hostPort.setHost(new Host(peerAddress));
        hostPort.setPort(peerPort);

        String messageChannelKey = MessageChannel.getKey(hostPort, "TCP");

        synchronized (this) {
            ConnectionOrientedMessageChannel foundMessageChannel = messageChannels.get(messageChannelKey);

            if (foundMessageChannel != null) {
                foundMessageChannel.close();
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger.logDebug(Thread.currentThread() + " Removing channel " + messageChannelKey + " for processor " + getIpAddress()+ ":" + getPort() + "/" + getTransport());
                incomingMessageChannels.remove(messageChannelKey);
                messageChannels.remove(messageChannelKey);
                return true;
            }
            
            foundMessageChannel = incomingMessageChannels.get(messageChannelKey);

            if (foundMessageChannel != null) {
                foundMessageChannel.close();
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger.logDebug(Thread.currentThread() + " Removing incoming channel " + messageChannelKey + " for processor " + getIpAddress()+ ":" + getPort() + "/" + getTransport());
                incomingMessageChannels.remove(messageChannelKey);
                messageChannels.remove(messageChannelKey);
                return true;
            }
        }
        return false;
    }
    
    public boolean setKeepAliveTimeout(String peerAddress, int peerPort, long keepAliveTimeout) {

        validatePortInRange(peerPort);

        HostPort hostPort  = new HostPort();
        hostPort.setHost(new Host(peerAddress));
        hostPort.setPort(peerPort);

        String messageChannelKey = MessageChannel.getKey(hostPort, "TCP");
                
        ConnectionOrientedMessageChannel foundMessageChannel = messageChannels.get(messageChannelKey);
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug(Thread.currentThread() + " checking channel with key " + messageChannelKey + " : " + foundMessageChannel + " for processor " + getIpAddress()+ ":" + getPort() + "/" + getTransport());
        
        if (foundMessageChannel != null) {
            foundMessageChannel.setKeepAliveTimeout(keepAliveTimeout);
            return true;
        }
        
        foundMessageChannel = incomingMessageChannels.get(messageChannelKey);
        
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug(Thread.currentThread() + " checking incoming channel with key " + messageChannelKey + " : " + foundMessageChannel + " for processor " + getIpAddress()+ ":" + getPort() + "/" + getTransport());
        
        if (foundMessageChannel != null) {
            foundMessageChannel.setKeepAliveTimeout(keepAliveTimeout);
            return true;
        }

        return false;
    }       

    protected void validatePortInRange(int port) throws IllegalArgumentException {
        if (port < 1 || port > 65535){
            throw new IllegalArgumentException("Peer port should be greater than 0 and less 65535, port = " + port);
        }
    }
}
