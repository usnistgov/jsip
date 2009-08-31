package gov.nist.javax.sip.stack.sctp;

import java.io.IOException;
import java.net.InetAddress;

import gov.nist.core.HostPort;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.MessageProcessor;
import gov.nist.javax.sip.stack.SIPTransactionStack;

public final class SCTPMessageProcessor extends MessageProcessor {

	/**
	 * Constructor, called via Class.newInstance() by SIPTransactionStack
	 */
	public SCTPMessageProcessor() {
		super( "sctp" );
	}
		
	@Override
	public MessageChannel createMessageChannel(HostPort targetHostPort)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageChannel createMessageChannel(InetAddress targetHost, int port)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDefaultTargetPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaximumMessageSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SIPTransactionStack getSIPStack() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inUse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
