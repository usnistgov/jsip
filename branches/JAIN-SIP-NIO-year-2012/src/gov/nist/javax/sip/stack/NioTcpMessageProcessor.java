package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.HostPort;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * NIO implementation for TCP.
 * 
 * @author mranga
 *
 */
public class NioTcpMessageProcessor extends ConnectionOrientedMessageProcessor {
    
    private Selector selector ;
    private static StackLogger logger = CommonLogger.getLogger(NioTcpMessageProcessor.class);
    private Thread selectorThread;
    protected NIOHandler nioHandler;

    // Cache the change request here, the selector thread will read it when it wakes up and execute the request
    private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest> ();

    // Data send over a socket is cached here before hand, the selector thread will take it later for physical send
    private Map<SocketChannel, List<ByteBuffer>> pendingData = 
    		new WeakHashMap<SocketChannel, List<ByteBuffer>>();

    
    public static class ChangeRequest {
    	public static final int REGISTER = 1;
    	public static final int CHANGEOPS = 2;

    	public SocketChannel socket;
    	public int type;
    	public int ops;

    	public ChangeRequest(SocketChannel socket, int type, int ops) {
    		this.socket = socket;
    		this.type = type;
    		this.ops = ops;
    	}
    	
    	public String toString() {
    		return socket + " type = " + type + " ops = " + ops;
    	}
    }
    
    private SocketChannel initiateConnection(InetSocketAddress address) throws IOException {
    	
    	// We use blocking outbound connect just because it's pure pain to deal with http://stackoverflow.com/questions/204186/java-nio-select-returns-without-selected-keys-why
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);
      
        if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        	logger.logDebug("Init connect " + address);
        socketChannel.socket().connect(address, 10000);
        socketChannel.configureBlocking(false);
        if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        	logger.logDebug("Blocking set to false now " + address);
      
        synchronized(this.changeRequests) {
        	changeRequests.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_READ));
        }
        selector.wakeup();
        return socketChannel;
    }

    public SocketChannel blockingConnect(InetSocketAddress address, int timeout) throws IOException {
    	SocketChannel channel = initiateConnection(address);
    	return channel;
    }
        
    public void send(SocketChannel socket, byte[] data) {
    	if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    		logger.logDebug("Sending data " + data.length + " bytes on socket " + socket);
    	synchronized (this.changeRequests) {
    		this.changeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

    		synchronized (this.pendingData) {
    			List<ByteBuffer> queue = this.pendingData.get(socket);
    			if (queue == null) {
    				queue = new ArrayList<ByteBuffer>();
    				this.pendingData.put(socket, queue);
    			}
    			queue.add(ByteBuffer.wrap(data));
    		}
    	}
    	if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    		logger.logDebug("Waking up selector thread");
    	this.selector.wakeup();
    }
    
    // This will be our selector thread, only one thread for all sockets. If you want to understand the overall design decisions read this first http://rox-xmlrpc.sourceforge.net/niotut/
    class ProcessorTask implements Runnable {

        public ProcessorTask() {
        }
        
        public void read(SelectionKey selectionKey) {
        	 // read it.
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            final NioTcpMessageChannel nioTcpMessageChannel = NioTcpMessageChannel.getMessageChannel(socketChannel);
            if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            	logger.logDebug("Got something on nioTcpMessageChannel " + nioTcpMessageChannel + " socket " + socketChannel);
            if(nioTcpMessageChannel == null) {
            	if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            		logger.logDebug("Dead socketChannel" + socketChannel + " socket " + socketChannel.socket().getInetAddress() + ":"+socketChannel.socket().getPort());
            	selectionKey.cancel();
            	return;
            }
            
			nioTcpMessageChannel.readChannel();

        }
        
        public void write(SelectionKey selectionKey) throws IOException {
          	SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        	synchronized (pendingData) {
        		List<ByteBuffer> queue = pendingData.get(socketChannel);
        		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        			logger.logDebug("Queued items for writing " + queue.size());
        		while (!queue.isEmpty()) {
        			ByteBuffer buf = queue.get(0);

        			socketChannel.write(buf);

        			int remain = buf.remaining();
        			
        			if (remain > 0) {
        				// ... or the socket's buffer fills up
        				if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        					logger.logDebug("Socket buffer filled and more is remaining" + queue.size() + " remain = " + remain);
        				break;
        			}
        			queue.remove(0);
        		}

        		if (queue.isEmpty()) {
        			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        				logger.logDebug("We wrote away all data. Setting READ interest. Queue is emtpy now size =" + queue.size());
        			selectionKey.interestOps(SelectionKey.OP_READ);
        		}
        	}
        	if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        		logger.logDebug("Done writing");
        }
        
        public void connect(SelectionKey selectionKey) throws IOException {
        	// Ignoring the advice from http://rox-xmlrpc.sourceforge.net/niotut/ because it leads to spinning on my machine
        	throw new IOException("We should use blocking connect, we must never reach here");
        	/*SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        	  
        	try {
        		socketChannel.finishConnect();
        	} catch (IOException e) {
        		selectionKey.cancel();
        		logger.logError("Cant connect", e);
        		return;
        	}
            synchronized (socketChannel) {
            	logger.logDebug("Notifying to wake up the blocking connect");
            	socketChannel.notify();
            }

            // Register an interest in writing on this channel
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            */
        }
        
        public void accept(SelectionKey selectionKey) throws IOException{
        	 ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        	 SocketChannel client;
        	 client = serverSocketChannel.accept();
        	 client.configureBlocking(false);
        	 if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        		 logger.logDebug("got a new connection! " + client);

        	 // No need for MAX SOCKET CHANNELS check here because this can be configured at OS level
        	 NioTcpMessageChannel.create(NioTcpMessageProcessor.this, client);
        	 
        	 if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        		 logger.logDebug("Adding to selector " + client);
        	 client.register(selector, SelectionKey.OP_READ);
        	 
        }
        @Override
        public void run() {
        	while (true) {
        		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        			logger.logDebug("Selector thread cycle begin...");
        		}
        		synchronized(changeRequests) {
        			Iterator<ChangeRequest> changes = changeRequests.iterator();
        			while (changes.hasNext()) {
        				ChangeRequest change = (ChangeRequest) changes.next();
        				if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        					logger.logDebug("ChangeRequest " + change + " selector = " + selector);
        				try {
        					switch(change.type) {
        					case ChangeRequest.CHANGEOPS:
        						SelectionKey key = change.socket.keyFor(selector);
        						if(key == null) continue;
        						key.interestOps(change.ops);
        						if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        							logger.logDebug("Change opts " + change + " selector = " + selector + " key = " + key + " blocking=" + change.socket.isBlocking());
        						}
        						break;
        					case ChangeRequest.REGISTER:
        						try {
        							
        							if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        								logger.logDebug("NIO register " + change + " selector = " + selector + " blocking=" + change.socket.isBlocking());
        							}
            						
        							change.socket.register(selector, change.ops);
        						} catch (ClosedChannelException e) {
        							logger.logWarning("Socket closed before register ops " + change.socket);
        						}
        						break;
        					}
        				} catch (Exception e) {
        					logger.logError("Problem setting changes", e);
        				}
        			}
    				changeRequests.clear(); 
        		}
        		try {
        			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        				logger.logDebug("Before select");
        			}
        			selector.select();
        			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        				logger.logDebug("After select");
        			}
        		} catch (IOException e) {
        			logger.logError("problem in select", e);
        			break;
        		} catch (CancelledKeyException cke) {
        			if(logger.isLoggingEnabled(LogWriter.TRACE_INFO)) {
        				logger.logInfo("Looks like remote side closed a connection");
        			}
        		}
        		if(!selector.isOpen()) {
        			if(logger.isLoggingEnabled(LogWriter.TRACE_INFO)) {
        				logger.logInfo("Selector is closed ");
        			}
        			return;
        		}
        		if (selector.selectedKeys() == null ) {
        			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        				logger.logDebug("null selectedKeys " );
        			}
        			continue;
        		}
        		Iterator<SelectionKey> it = selector.selectedKeys().iterator();
        		try {
        			while (it.hasNext()) {
        				SelectionKey selectionKey = it.next();
        				try {
        					it.remove();
        					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        						logger.logDebug("We got selkey " + selectionKey);
        					}
        					if (selectionKey.isAcceptable()) {
        						if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        							logger.logDebug("Accept " + selectionKey);
        						}
        						accept(selectionKey);
        						continue;
        					} else if (selectionKey.isReadable()) {
        						if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        							logger.logDebug("Read " + selectionKey);
        						}
        						read(selectionKey);
        						continue;
        					} else if (selectionKey.isWritable()) {
        						if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        							logger.logDebug("Write " + selectionKey);
        						}
        						write(selectionKey);
        						continue;
        					} else if(selectionKey.isConnectable()) {
        						if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        							logger.logDebug("Connect " + selectionKey);
        						}
        						connect(selectionKey);
        					}
        				} catch (Exception e) {
        					logger.logError("Problem processing selection key event", e);
        					//NioTcpMessageChannel.get(selectionKey.channel());
        				}
        			}

        		} catch (Exception ex) {
        			logger.logError("Error", ex);
        		}
        	}

        }
    }
    
    public NioTcpMessageProcessor(InetAddress ipAddress,  SIPTransactionStack sipStack, int port) {
    	super(ipAddress, port, "TCP", sipStack);
    	nioHandler = new NIOHandler(sipStack, this);
    }

    @Override
    public MessageChannel createMessageChannel(HostPort targetHostPort) throws IOException {
    	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    		logger.logDebug("NioTcpMessageProcessor::createMessageChannel: " + targetHostPort);
    	}
    	try {
    		String key = MessageChannel.getKey(targetHostPort, "TCP");
    		if (messageChannels.get(key) != null) {
    			return (NioTcpMessageChannel) this.messageChannels.get(key);
    		} else {
    			NioTcpMessageChannel retval = new NioTcpMessageChannel(targetHostPort.getInetAddress(),
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
        String key = MessageChannel.getKey(targetHost, port, "TCP");
        if (messageChannels.get(key) != null) {
            return (NioTcpMessageChannel) this.messageChannels.get(key);
        } else {
            NioTcpMessageChannel retval = new NioTcpMessageChannel(targetHost, port, sipStack, this);
            
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

    @Override
    public int getDefaultTargetPort() {
        return 5060;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

   

    @Override
    public void start() throws IOException {
        selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        InetSocketAddress isa  = new InetSocketAddress(super.getIpAddress(), super.getPort());
        ssc.socket().bind(isa);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        selectorThread = new Thread( new ProcessorTask());
        selectorThread.start();
        selectorThread.setName("NioSelector-" + selectorThread.getName());
    }

    @Override
    public void stop() {
        try {
        	nioHandler.stop();
            for (SelectionKey selectionKey : selector.keys() ) {
                selectionKey.channel().close();
            }
            selector.close();
        } catch (Exception ex) {
            logger.logError("Probelm closing channel " , ex);
        }
    }

}
