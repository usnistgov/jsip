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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * This is a helper state machine that negotiates the SSL connection automatically
 * without ever blocking any threads. It is important not to block here because
 * the TLS may occur in the selector thread which is one per all sockets.
 * 
 * Other than that the state machine is able to handle partial chunks of SIP messages
 * and only supply them when they are ready to the original TCP channel once they are
 * decrypted.
 * 
 * @author vladimirralev
 *
 */
public class SSLStateMachine {

	private static StackLogger logger = CommonLogger.getLogger(SSLStateMachine.class);
	public final static ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[] {});

	protected SSLEngine sslEngine;
	protected Queue<MessageSendItem> pendingOutboundBuffers = 
			new LinkedList<MessageSendItem>();
	protected NioTlsChannelInterface channel;
	protected ByteBuffer tlsRecordBuffer;
	private Object unwrapLock = new Object();
	private Object wrapLock = new Object();

	public SSLStateMachine(SSLEngine sslEngine, NioTlsChannelInterface channel) {
		this.sslEngine = sslEngine;
		this.channel = channel;
	}

	public void wrapRemaining() throws IOException {
		wrap(null, channel.prepareEncryptedDataBuffer(), null);
	}
	public void wrap(ByteBuffer src, ByteBuffer dst, 
			MessageSendCallback callback) throws IOException {
		synchronized (wrapLock) {


			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("Wrapping " + src + ", buffers size " + pendingOutboundBuffers.size());
			}

			// Null src means we just have no ne data but still want to push any previously queued data
			if(src != null) {
				pendingOutboundBuffers.offer(new MessageSendItem(src, callback));
			}
			int iter = 0;
			loop:while(true) {
				iter ++;

				MessageSendItem currentBuffer = pendingOutboundBuffers.peek();

				// If there is no queued operations break out of the loop
				if(currentBuffer == null) break;

				SSLEngineResult result;
				try {
					result = sslEngine.wrap(currentBuffer.message, dst);
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("Wrap result " + result + " buffers size " + pendingOutboundBuffers.size());
					}
				} finally {
					if(!currentBuffer.message.hasRemaining()) {
						pendingOutboundBuffers.remove();
						if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
							logger.logDebug("REMOVED item from encryption queue because it has no more data, all is done, buffers size now is "
								+ pendingOutboundBuffers.size() + " current buffer is " + currentBuffer);
						}
					}
				}
				int remaining = currentBuffer.message.remaining();

				if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
					logger.logDebug("Remaining " + remaining +  " queue size is " + pendingOutboundBuffers.size());
				}

				if(result.bytesProduced() > 0) {
					// produced > 0 means encryption was successful and we have something to send over the wire
					dst.flip();
					byte[] bytes = new byte[dst.remaining()];
					dst.get(bytes);
					if(currentBuffer.getCallBack() != null) {
						// Send using message channel (it discriminates between client/server and new/old connecitons)
						currentBuffer.getCallBack().doSend(bytes);
					} else {
						// Send using the existing connection without attempting to guess client or server etc
						sendSSLMetadata(bytes);
					}
					dst.clear();
				} else {
					switch (result.getHandshakeStatus()) {
					case NEED_WRAP:
						if (currentBuffer.message.hasRemaining()) {
							break;
						} else {
							break loop;
						}
					case NEED_UNWRAP:
						break loop;
					case NEED_TASK:
						runDelegatedTasks(result);
						break;
					case FINISHED:
						// Added for https://java.net/jira/browse/JSIP-483 
						if(channel instanceof NioTlsMessageChannel) {
							((NioTlsMessageChannel)channel).setHandshakeCompleted(true);
							if(sslEngine.getSession() != null) {
								if(!ClientAuthType.Disabled.equals(channel.getSIPStack().getClientAuth()) && !ClientAuthType.DisabledAll.equals(channel.getSIPStack().getClientAuth())) {
									// https://java.net/jira/browse/JSIP-483 Don't try to get the PeerCertificates if the client auth is Disabled or DisabledAll as they won't be available
									try {
										((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setPeerCertificates(sslEngine.getSession().getPeerCertificates());
									} catch (SSLPeerUnverifiedException e) {
										// no op if -Dgov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Disabled is used, no peer certificates will be available
										if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
											logger.logDebug("sslEngine.getSession().getPeerCertificates() are not available, which is normal if running with gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Disabled");
										}
									}
								}
								((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setLocalCertificates(sslEngine.getSession().getLocalCertificates());
								((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setCipherSuite(sslEngine.getSession().getCipherSuite());
							}
						}
						break;
					case NOT_HANDSHAKING:
						break loop;
					default:
						break;


					}
				}
			}
		}
	}

	private void wrapNonAppData() throws Exception {
		ByteBuffer encryptedDataBuffer = channel.prepareEncryptedDataBuffer();

		SSLEngineResult result;
		try {
			loop:while(true) {
				result = sslEngine.wrap(EMPTY_BUFFER, encryptedDataBuffer);
				if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
					logger.logDebug("NonAppWrap result " + result + " buffers size "
							+ pendingOutboundBuffers.size());
				}
				if (result.bytesProduced() > 0) {
					// any output here is internal TLS metadata such as handshakes
					encryptedDataBuffer.flip();
					byte[] msg = new byte[encryptedDataBuffer.remaining()];
					encryptedDataBuffer.get(msg);
					// send it directly over the wire without further processing or parsing
					sendSSLMetadata(msg);
					encryptedDataBuffer.clear();
				}

				switch (result.getHandshakeStatus()) {
				case FINISHED:
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("Handshake complete!");
					}
					// Added for https://java.net/jira/browse/JSIP-483 
					if(channel instanceof NioTlsMessageChannel) {
						((NioTlsMessageChannel)channel).setHandshakeCompleted(true);
						if(sslEngine.getSession() != null) {
							if(!ClientAuthType.Disabled.equals(channel.getSIPStack().getClientAuth()) && !ClientAuthType.DisabledAll.equals(channel.getSIPStack().getClientAuth())) {
								// https://java.net/jira/browse/JSIP-483 Don't try to get the PeerCertificates if the client auth is Disabled or DisabledAll as they won't be available
								try {
									((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setPeerCertificates(sslEngine.getSession().getPeerCertificates());
								} catch (SSLPeerUnverifiedException e) {
									// no op if -Dgov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Disabled is used, no peer certificates will be available
									if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
										logger.logDebug("sslEngine.getSession().getPeerCertificates() are not available, which is normal if running with gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Disabled");
									}
								}
							}
							((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setLocalCertificates(sslEngine.getSession().getLocalCertificates());
							((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setCipherSuite(sslEngine.getSession().getCipherSuite());
						}
					}
					break;
				case NEED_TASK:
					runDelegatedTasks(result);
					break;
				}

				if (result.bytesProduced() == 0) {
					break;
				}
			}
		} catch (SSLException e) {

			throw e;
		} finally {

		}
	}

	public void unwrap(ByteBuffer src) throws Exception {
		ByteBuffer outputBuffer = channel.prepareAppDataBuffer();
		unwrap(src, outputBuffer);
	}

	private void startBuffer(ByteBuffer src) {
		if(tlsRecordBuffer == null) {

			// Begin buffering, if there is already a buffer the normalization will take of adding the bytes
			tlsRecordBuffer = ByteBufferFactory.getInstance().allocateDirect(33270); // max record size in other implementations

			// Append the current buffer
			tlsRecordBuffer.put(src);

			// Prepare the buffer for reading
			tlsRecordBuffer.flip();

			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("Allocated record buffer for reading " + tlsRecordBuffer + " for src = " + src);
			}
		}
	}
	private void clearBuffer() {
		tlsRecordBuffer = null;
		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("Buffer cleared");
		}
	}
	private ByteBuffer normalizeTlsRecordBuffer(ByteBuffer src) {
		if(tlsRecordBuffer == null) {
			return src;
		} else {
			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("Normalize buffer " + src + " into record buffer " 
						+ tlsRecordBuffer);
			}

			// Reverse flip() to prepare the buffer to writing in append mode
			tlsRecordBuffer.position(tlsRecordBuffer.limit());
			tlsRecordBuffer.limit(tlsRecordBuffer.capacity());

			// Append data
			tlsRecordBuffer.put(src);

			// And prepare it for reading again as if it came from the network
			tlsRecordBuffer.flip();
			return tlsRecordBuffer;
		}
	}
	private void unwrap(ByteBuffer src, ByteBuffer dst) throws Exception {
		synchronized (unwrapLock) {


			loop:while(true) {
				src = normalizeTlsRecordBuffer(src);
				if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
					logger.logDebug("Unwrap src " + src + " dst " 
							+ dst);
				}
				SSLEngineResult result = null;
				try {
					result = sslEngine.unwrap(src, dst);
				} catch (Exception e) {
					// https://java.net/jira/browse/JSIP-464 
					// Make sure to throw the exception so the result variable is not null below which makes the stack hang
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("An Exception occured while trying to unwrap the message " + e);
					}
					throw e;
				}
				if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
					logger.logDebug("Unwrap result " + result + " buffers size " 
							+ pendingOutboundBuffers.size() + " src=" + src + " dst=" + dst);
				}

				if(result.getStatus().equals(Status.BUFFER_UNDERFLOW)) {
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("Buffer underflow, wait for the next inbound chunk of data to feed the SSL engine");
					}
					startBuffer(src);
					break;
				} else {
					clearBuffer();
				}
				if(result.getStatus().equals(Status.BUFFER_OVERFLOW)) {
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("Buffer overflow , must prepare the buffer again."
								+ " outNetBuffer remaining: " +  dst.remaining()
								+ " outNetBuffer postion: " +  dst.position()
								+ " Packet buffer size: " + sslEngine.getSession().getPacketBufferSize()
								+ " new buffer size: " + sslEngine.getSession().getPacketBufferSize() + dst.position());
					}
					ByteBuffer newBuf = channel.prepareAppDataBuffer(sslEngine.getSession().getPacketBufferSize() + dst.position());
					dst.flip();
					newBuf.put(dst);
					dst = newBuf;
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug(" new outNetBuffer remaining: " +  dst.remaining()
								+ " new outNetBuffer postion: " +  dst.position());
					}
					continue;
				}
				if(result.bytesProduced()>0) {
					// There is actual application data in this chunk
					dst.flip();
					byte[] a = new byte[dst.remaining()];
					dst.get(a);
					// take it and feed the plain text to out chunk-by-chunk parser
					channel.addPlaintextBytes(a);
				}
				switch(result.getHandshakeStatus()) {
				case NEED_UNWRAP:
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("Unwrap has remaining: " + src.hasRemaining() + " buffer " + src);
					}
					if(src.hasRemaining()) {
						break;
					} else {
						break loop;
					}
				case NEED_WRAP:
					wrapNonAppData();
					break;
				case NEED_TASK:
					runDelegatedTasks(result);
					break;
				case FINISHED:
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("Handshaking just finnished, but has remaining. Will try to wrap the queues app items.");
					}
					wrapRemaining();
					if(src.hasRemaining()) {
						break;
					} else {
						if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
							this.logger.logDebug(
									"Handshake passed");
						}
						// Added for https://java.net/jira/browse/JSIP-483 
						// allow application to enforce policy by validating the
						// certificate
						if(channel instanceof NioTlsMessageChannel) {
							((NioTlsMessageChannel)channel).setHandshakeCompleted(true);
							if(sslEngine.getSession() != null) {
								if(!ClientAuthType.Disabled.equals(channel.getSIPStack().getClientAuth()) && !ClientAuthType.DisabledAll.equals(channel.getSIPStack().getClientAuth())) {
									// https://java.net/jira/browse/JSIP-483 Don't try to get the PeerCertificates if the client auth is Disabled or DisabledAll as they won't be available
									try {
										((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setPeerCertificates(sslEngine.getSession().getPeerCertificates());
									} catch (SSLPeerUnverifiedException e) {
										// no op if -Dgov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Disabled is used, no peer certificates will be available
										if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
											logger.logDebug("sslEngine.getSession().getPeerCertificates() are not available, which is normal if running with gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Disabled");
										}
									}
								}
								((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setLocalCertificates(sslEngine.getSession().getLocalCertificates());
								((NioTlsMessageChannel)channel).getHandshakeCompletedListener().setCipherSuite(sslEngine.getSession().getCipherSuite());
							}
							try {
								channel.getSIPStack()
								.getTlsSecurityPolicy()
								.enforceTlsPolicy(
										channel
										.getEncapsulatedClientTransaction());
							} catch (SecurityException ex) {
								throw new IOException(ex.getMessage());
							}

							if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
								this.logger.logDebug(
										"TLS Security policy passed");
							}
						}
						break loop;
					}
				case NOT_HANDSHAKING:
					wrapRemaining();
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("Not handshaking, but has remaining: " + src.hasRemaining() + " buffer " + src);
					}
					if(src.hasRemaining()) {
						break;
					} else {
						break loop;
					}
				default:
					break;
				}
			}
		}
	}

	private void runDelegatedTasks(SSLEngineResult result) throws IOException {
		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("Running delegated task for " + result);
		}

		/*
		 *  Delegated tasks are just invisible steps inside the sslEngine state machine.
		 *  Call them every time they have NEED_TASK otherwise the sslEngine won't make progress
		 */
		if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
			Runnable runnable;
			while ((runnable = sslEngine.getDelegatedTask()) != null) {
				runnable.run();
			}
			HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("Handshake status after delegated tasks " + hsStatus);
			}
			if (hsStatus == HandshakeStatus.NEED_TASK) {
				throw new IOException(
						"handshake shouldn't need additional tasks");
			}
		}
	}

	public void sendSSLMetadata(byte[] msg) throws IOException {
		channel.sendEncryptedData(msg);
	}

	public static interface MessageSendCallback {
		public void doSend(byte[] bytes) throws IOException;
	}

	/**
	 * Each time we send a SIP message it will be associated with the originating channel.send() method
	 * which keep additional information about the transport in its context. That method will be called
	 * using the callback provided here.
	 * 
	 * @author vladimirralev
	 *
	 */
	public static class MessageSendItem {

		private ByteBuffer message;
		private MessageSendCallback callback;

		public MessageSendItem(ByteBuffer buffer, MessageSendCallback callback) {
			this.message = buffer;
			this.callback = callback;
		}

		public MessageSendCallback getCallBack() {
			return callback;
		}

		public String toString() {
			return MessageSendItem.class.getSimpleName() + " [" 
					+ message + ", " + callback + "]";
		}

	}
}
