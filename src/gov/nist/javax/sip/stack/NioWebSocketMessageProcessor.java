package gov.nist.javax.sip.stack;


import gov.nist.core.CommonLogger;
import gov.nist.core.HostPort;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class NioWebSocketMessageProcessor extends NioTcpMessageProcessor {

    private static StackLogger logger = CommonLogger.getLogger(NioWebSocketMessageProcessor.class);
    
    public static HashMap<String, NioWebSocketMessageChannel> addressMap = new HashMap<String, NioWebSocketMessageChannel>();

	public NioWebSocketMessageProcessor(InetAddress ipAddress,
			SIPTransactionStack sipStack, int port) {
		super(ipAddress, sipStack, port);
		transport = "WS";
	}
	
	@Override
	public NioTcpMessageChannel createMessageChannel(NioTcpMessageProcessor nioTcpMessageProcessor, SocketChannel client) throws IOException {
    	return NioWebSocketMessageChannel.create(this, client);
    }
	
    @Override
    public MessageChannel createMessageChannel(HostPort targetHostPort) throws IOException {
    	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    		logger.logDebug(":createMessageChannel: " + targetHostPort);
    	}
    	try {
    		String key = MessageChannel.getKey(targetHostPort, "WS");
    		if (messageChannels.get(key) != null) {
    			return this.messageChannels.get(key);
    		} else {
    			NioWebSocketMessageChannel retval = new NioWebSocketMessageChannel(targetHostPort.getInetAddress(),
    					targetHostPort.getPort(), sipStack, this);
    			
    			
    		//	retval.getSocketChannel().register(selector, SelectionKey.OP_READ);
    			synchronized(messageChannels) {
    				this.messageChannels.put(key, retval);
    			}
    			retval.isCached = true;
    			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    				logger.logDebug("key " + key);
    				logger.logDebug("Creating " + retval);
    			}
    			selector.wakeup();
    			return retval;

    		}
    	} finally {
    		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    			logger.logDebug("MessageChannel::createMessageChannel - exit");
    		}
    	}
    }

    @Override
    public MessageChannel createMessageChannel(InetAddress targetHost, int port) throws IOException {
        String key = MessageChannel.getKey(targetHost, port, "WS");
        if (messageChannels.get(key) != null) {
            return this.messageChannels.get(key);
        } else {
            NioWebSocketMessageChannel retval = new NioWebSocketMessageChannel(targetHost, port, sipStack, this);
            
            selector.wakeup();
 //           retval.getSocketChannel().register(selector, SelectionKey.OP_READ);
            this.messageChannels.put(key, retval);
            retval.isCached = true;
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("key " + key);
                logger.logDebug("Creating " + retval);
            }
            return retval;
        }

    }

}
