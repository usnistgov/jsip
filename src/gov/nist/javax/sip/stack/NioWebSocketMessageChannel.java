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
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;

public class NioWebSocketMessageChannel extends NioTcpMessageChannel{

	private static StackLogger logger = CommonLogger
			.getLogger(NioWebSocketMessageChannel.class);
	
	public static NioWebSocketMessageChannel create(
			NioWebSocketMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		NioWebSocketMessageChannel retval = (NioWebSocketMessageChannel) channelMap.get(socketChannel);
		if (retval == null) {
			retval = new NioWebSocketMessageChannel(nioTcpMessageProcessor,
					socketChannel);
			channelMap.put(socketChannel, retval);
		}
		return retval;
	}
	
	protected NioWebSocketMessageChannel(NioTcpMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		super(nioTcpMessageProcessor, socketChannel);

		messageProcessor = nioTcpMessageProcessor;
		myClientInputStream = socketChannel.socket().getInputStream();
	}
	
	@Override
	protected void sendMessage(final byte[] msg, final boolean isClient) throws IOException {
		super.sendMessage(msg, isClient);
	}
	
	protected void sendNonWebSocketMessage(byte[] msg, boolean isClient) throws IOException {

		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("sendMessage isClient  = " + isClient + " this = " + this);
		}
		lastActivityTimeStamp = System.currentTimeMillis();
		
		NIOHandler nioHandler = ((NioTcpMessageProcessor) messageProcessor).nioHandler;
		if(this.socketChannel != null && this.socketChannel.isConnected() && this.socketChannel.isOpen()) {
			nioHandler.putSocket(NIOHandler.makeKey(this.peerAddress, this.peerPort), this.socketChannel);
		}
		super.sendTCPMessage(msg, this.peerAddress, this.peerPort, isClient);
	}

	private byte[] wrapBufferIntoWebSocketFrame(byte[] buffer) {
		try {
			return WebSocketCodec.encode(buffer, 0, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void sendTCPMessage(byte message[], InetAddress receiverAddress,
			int receiverPort, boolean retry) throws IOException {
		message = wrapBufferIntoWebSocketFrame(message);
		super.sendTCPMessage(message, receiverAddress, receiverPort, retry);
	}
	
	@Override
	public void sendMessage(final byte message[], final InetAddress receiverAddress,
			final int receiverPort, final boolean retry) throws IOException {
		sendTCPMessage(message, receiverAddress, receiverPort, retry);
	}

	public NioWebSocketMessageChannel(InetAddress inetAddress, int port,
			SIPTransactionStack sipStack,
			NioTcpMessageProcessor nioTcpMessageProcessor) throws IOException {
		super(inetAddress, port, sipStack, nioTcpMessageProcessor);
	}
	
	@Override
	protected void addBytes(byte[] bytes) throws Exception {
		String s = new String(bytes);
		
		if(s.startsWith("GET")) {
			byte[] response = new WebSocketHttpHandshake().createHttpResponse(s);
			sendNonWebSocketMessage(response, false);
		} else {
			ByteArrayInputStream bios = new ByteArrayInputStream(bytes);
			WebSocketCodec codec = new WebSocketCodec(true, true);
			byte[] decodedMsg = codec.decode(bios);
			System.out.println(new String(decodedMsg));
			nioParser.addBytes(decodedMsg);
		}
	}
	
	@Override
	public String getTransport() {
		return "WS";
	}

	@Override
	public void onNewSocket() {
		super.onNewSocket();
		
	}

}
