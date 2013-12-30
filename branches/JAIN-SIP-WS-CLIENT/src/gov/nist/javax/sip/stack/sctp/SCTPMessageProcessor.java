package gov.nist.javax.sip.stack.sctp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

import gov.nist.core.HostPort;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.MessageProcessor;
import gov.nist.javax.sip.stack.SIPTransactionStack;

/**
 * SCTP Message Processor
 * 
 * @author Jeroen van Bemmel
 */
public final class SCTPMessageProcessor extends MessageProcessor implements Runnable {

	private SctpServerChannel sctpServerChannel;
	private Selector selector;
	private SelectionKey key;
	private boolean isRunning, doClose;
	
	private final Set<SCTPMessageChannel> channels 
		= new ConcurrentSkipListSet<SCTPMessageChannel>();
	
	/**
	 * Constructor, called via Class.newInstance() by SIPTransactionStack
	 */
	public SCTPMessageProcessor() {
		super( "sctp" );
	}

	Selector getSelector() { return selector; }
	
	SelectionKey registerChannel( SCTPMessageChannel c, SctpChannel channel ) 
		throws ClosedChannelException {
		synchronized (this) {
			selector.wakeup();
			return channel.register( selector, SelectionKey.OP_READ, c );
		}
	}
	
	@Override
	public MessageChannel createMessageChannel(HostPort targetHostPort)
			throws IOException {		
		return this.createMessageChannel( targetHostPort.getInetAddress(), targetHostPort.getPort() );
	}

	@Override
	public MessageChannel createMessageChannel(InetAddress targetHost, int port)
			throws IOException {
		
		SCTPMessageChannel c = new SCTPMessageChannel( this, 
				new InetSocketAddress(targetHost,port) );
		channels.add( c );
		return c;
	}

	@Override
	public int getDefaultTargetPort() {		
		return 5060;	// same as UDP and TCP
	}

	@Override
	public int getMaximumMessageSize() {
		return Integer.MAX_VALUE;
	}

	@Override
	public SIPTransactionStack getSIPStack() {
		return sipStack;
	}

	@Override
	public boolean inUse() {
		return isRunning;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	public void run() {
		try {
			do {
				int n = selector.select();
				if (n>0) {
					Iterator<SelectionKey> i = selector.selectedKeys().iterator();
					while ( i.hasNext() ) {
						SelectionKey key = i.next();
						i.remove();
						if ( key.isReadable() ) {
							SCTPMessageChannel channel = (SCTPMessageChannel) key.attachment();
							channel.readMessages();
						} else if (key.isAcceptable()) {
							SctpChannel ch = sctpServerChannel.accept();
							SCTPMessageChannel c = new SCTPMessageChannel( this, ch );
							channels.add( c );
						}
					}
				}
				
				synchronized (this) {
					if (doClose) {
						selector.close();
						return;
					}
				}				
			} while ( selector.isOpen() );
		} catch (IOException ioe) {
			ioe.printStackTrace();
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				this.stop();
			}
		}
	}

	@Override
	public void start() throws IOException {

		this.sctpServerChannel = SctpServerChannel.open();		
		sctpServerChannel.bind( new InetSocketAddress(this.getIpAddress(),this.getPort()) );
		sctpServerChannel.configureBlocking( false );
		
		this.selector = Selector.open();
		this.key = sctpServerChannel.register( selector, SelectionKey.OP_ACCEPT );
				
		// Start a daemon thread to handle reception
		this.isRunning = true;
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("SCTPMessageProcessorThread");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
	}

	@Override
	public void stop() {
		this.isRunning = false;
		this.doClose = true;
		
		for ( SCTPMessageChannel c : channels ) {
			c.closeNoRemove();	// avoids call to removeChannel -> ConcurrentModification
		}
		channels.clear();
		try {
			key.cancel();
			sctpServerChannel.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			synchronized (this) {
				selector.wakeup();
			}
		}
	}

	void removeChannel(SCTPMessageChannel messageChannel) {
		channels.remove( messageChannel );
	}

}
