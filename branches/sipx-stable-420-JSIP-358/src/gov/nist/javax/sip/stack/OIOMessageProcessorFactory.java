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

import java.io.IOException;
import java.net.InetAddress;

import javax.sip.ListeningPoint;

/**
 * Default stack implementation of the MessageProcessorFactory.
 * This Factory creates MessageProcessor instances using the Old IO (as opposed to NIO)
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class OIOMessageProcessorFactory implements MessageProcessorFactory {

	public MessageProcessor createMessageProcessor(
			SIPTransactionStack sipStack, InetAddress ipAddress, int port,
			String transport) throws IOException {
		if (transport.equalsIgnoreCase(ListeningPoint.UDP)) {
			UDPMessageProcessor udpMessageProcessor = new UDPMessageProcessor(
					ipAddress, sipStack, port);			
			sipStack.udpFlag = true;
			return udpMessageProcessor;
		} else if (transport.equalsIgnoreCase(ListeningPoint.TCP)) {
			TCPMessageProcessor tcpMessageProcessor = new TCPMessageProcessor(
					ipAddress, sipStack, port);			
			// this.tcpFlag = true;
			return tcpMessageProcessor;
		} else if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
			TLSMessageProcessor tlsMessageProcessor = new TLSMessageProcessor(
					ipAddress, sipStack, port);			
			// this.tlsFlag = true;
			return tlsMessageProcessor;
		} else if (transport.equalsIgnoreCase(ListeningPoint.SCTP)) {

			// Need Java 7 for this, so these classes are packaged in a separate
			// jar
			// Try to load it indirectly, if fails report an error
			try {
				Class<?> mpc = ClassLoader.getSystemClassLoader().loadClass(
						"gov.nist.javax.sip.stack.sctp.SCTPMessageProcessor");
				MessageProcessor mp = (MessageProcessor) mpc.newInstance();
				mp.initialize(ipAddress, port, sipStack);				
				return mp;
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(
						"SCTP not supported (needs Java 7 and SCTP jar in classpath)");
			} catch (InstantiationException ie) {
				throw new IllegalArgumentException("Error initializing SCTP",
						ie);
			} catch (IllegalAccessException ie) {
				throw new IllegalArgumentException("Error initializing SCTP",
						ie);
			}
		} else {
			throw new IllegalArgumentException("bad transport");
		}
	}

}
