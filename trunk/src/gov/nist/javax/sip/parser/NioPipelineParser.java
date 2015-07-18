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
/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)       *
 ******************************************************************************/
package gov.nist.javax.sip.parser;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogLevels;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.stack.ConnectionOrientedMessageChannel;
import gov.nist.javax.sip.stack.QueuedMessageDispatchBase;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentLengthHeader;

/**
 * This is a FSM that can parse a single stream of messages with they bodies and 
 * then pass the sip message to the listeners. It accumulates bytes until end of
 * message is detected or some DoS trigger terminates it due to excessive amount
 * of bytes per message or line.
 * 
 * Once parsed it will pass the message to the SIPMessageListener
 *
 * @see SIPMessageListener
 * @author vladimirralev
 */
public class NioPipelineParser {
	
	private static StackLogger logger = CommonLogger.getLogger(NioPipelineParser.class);

	private static final String CRLF = "\r\n";

    /**
     * The message listener that is registered with this parser. (The message
     * listener has methods that can process correct and erroneous messages.)
     */
    protected SIPMessageListener sipMessageListener;
    private int maxMessageSize;
    private int sizeCounter;
    private SIPTransactionStack sipStack;
    private MessageParser smp = null;
    boolean isRunning = false;
	boolean currentStreamEnded = false;
	boolean readingMessageBodyContents = false;
	boolean readingHeaderLines = true;
	boolean partialLineRead = false; // if we didn't receive enough bytes for a full line we expect the line to end in the next batch of bytes
	String partialLine = "";
	String callId;
	
	private ConcurrentHashMap<String, CallIDOrderingStructure> messagesOrderingMap = new ConcurrentHashMap<String, CallIDOrderingStructure>();
	   
	class CallIDOrderingStructure {
        private Semaphore semaphore;
        private Queue<UnparsedMessage> messagesForCallID;
        
        public CallIDOrderingStructure() {
            semaphore = new Semaphore(1, true);
            messagesForCallID = new ConcurrentLinkedQueue<UnparsedMessage>();
        }        

        /**
         * @return the semaphore
         */
        public Semaphore getSemaphore() {
            return semaphore;
        }
       
        /**
         * @return the messagesForCallID
         */
        public Queue<UnparsedMessage> getMessagesForCallID() {
            return messagesForCallID;
        }
    }
	
	public static class UnparsedMessage {
		String lines;
		byte[] body;
		public UnparsedMessage(String messageLines, byte[] body) {
			this.lines = messageLines;
			this.body = body;
		}
		
		public String toString() {
			return super.toString() + "\n" + lines;
		}
	}
	
    public class Dispatch implements Runnable, QueuedMessageDispatchBase{
    	CallIDOrderingStructure callIDOrderingStructure;
    	String callId;
    	long time;
    	public Dispatch(CallIDOrderingStructure callIDOrderingStructure, String callId) {
    		this.callIDOrderingStructure = callIDOrderingStructure;
    		this.callId = callId;
    		time = System.currentTimeMillis();
    	}
        public void run() {   
        	
            // we acquire it in the thread to avoid blocking other messages with a different call id
            // that could be processed in parallel                                    
            Semaphore semaphore = callIDOrderingStructure.getSemaphore();
            final Queue<UnparsedMessage> messagesForCallID = callIDOrderingStructure.getMessagesForCallID();
            try {                                                                                
                boolean acquired = semaphore.tryAcquire(30, TimeUnit.SECONDS);
                if(!acquired) {
                	if (logger.isLoggingEnabled(StackLogger.TRACE_WARN)) {
                		logger.logWarning("Semaphore acquisition for callId " + callId + " wasn't successful so don't process message, returning");
                	}
                	// https://java.net/jira/browse/JSIP-499 don't process the message if the semaphore wasn't acquired
                	return;
                } else {
	                if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
	                	logger.logDebug("semaphore acquired for message " + callId + " acquired");
	                }
                }
            } catch (InterruptedException e) {
            	logger.logError("Semaphore acquisition for callId " + callId + " interrupted, couldn't process message, returning", e);
            	// https://java.net/jira/browse/JSIP-499 don't process the message if the semaphore wasn't acquired
            	return;
            }
            
            UnparsedMessage unparsedMessage = null;
            SIPMessage parsedSIPMessage = null;
            boolean messagePolled = false;
            try {
            	synchronized(smp) {
            		unparsedMessage = messagesForCallID.peek();
            		if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            			logger.logDebug( "\nUnparsed message before parser is:\n" + unparsedMessage);
            		}
        			parsedSIPMessage = smp.parseSIPMessage(unparsedMessage.lines.getBytes(), false, false, null);        		
        			if(parsedSIPMessage == null) {
        				// https://java.net/jira/browse/JSIP-503
        				if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                			logger.logDebug( "parsed message is null, probably because of end of stream, empty packets or socket closed "
                					+ "and we got CRLF to terminate cleanly, not processing message");
                		}
        			} else if(unparsedMessage.body.length > 0) {
        				parsedSIPMessage.setMessageContent(unparsedMessage.body);
        			}
            	}
            	if(sipStack.sipEventInterceptor != null
            			// https://java.net/jira/browse/JSIP-503
                		&& parsedSIPMessage != null) {
            		sipStack.sipEventInterceptor.beforeMessage(parsedSIPMessage);
            	}

            	// once acquired we get the first message to process
            	messagesForCallID.poll();
            	messagePolled = true;
            	if(parsedSIPMessage != null) { // https://java.net/jira/browse/JSIP-503
            		sipMessageListener.processMessage(parsedSIPMessage);
            	}
            } catch (ParseException e) {
            	// https://java.net/jira/browse/JSIP-499 move the ParseException here so the finally block 
            	// is called, the semaphore released and map cleaned up if need be
            	if (logger.isLoggingEnabled(StackLogger.TRACE_WARN)) {
            		logger.logWarning("Problem parsing message " + unparsedMessage);
            	}
    		}catch (Exception e) {
            	logger.logError("Error occured processing message " + message, e);
                // We do not break the TCP connection because other calls use the same socket here
            } finally {            
            	if(!messagePolled) {
            		// https://java.net/jira/browse/JSIP-503 we poll the message only if it 
            		// wasn't polled in case of some exception by example while parsing or before processing the message
            		// as in the interceptor can cause the poll not to be called
            		messagesForCallID.poll(); // move on to the next one
            	}
                if(messagesForCallID.size() <= 0) {
                    messagesOrderingMap.remove(callId);
                    if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                    	logger.logDebug("CallIDOrderingStructure removed for callId " + callId);
                    }
                }
                if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                	logger.logDebug("releasing semaphore for message " + parsedSIPMessage);
                }
                //release the semaphore so that another thread can process another message from the call id queue in the correct order
                // or a new message from another call id queue
                semaphore.release(); 
                if(messagesOrderingMap.isEmpty()) {
                    synchronized (messagesOrderingMap) {
                        messagesOrderingMap.notify();
                    }
                }
                if(sipStack.sipEventInterceptor != null
                		// https://java.net/jira/browse/JSIP-503
                		&& parsedSIPMessage != null) {
                	sipStack.sipEventInterceptor.afterMessage(parsedSIPMessage);
                }
            }
            if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            	logger.logDebug("dispatch task done on " + parsedSIPMessage);
            }
        }
		public long getReceptionTime() {
			return time;
		}
    };
	
	public void close() {
		
	}
	
	StringBuffer message = new StringBuffer();
	byte[] messageBody = null;
	int contentLength = 0;
	int contentReadSoFar = 0;
	
	/*
	 *  This is where we receive the bytes from the stream and we analyze the through message structure.
	 *  For TCP the key things to identify are message lines for the headers, parse the Content-Length header
	 *  and then read the message body (aka message content). For TCP the Content-Length must be 100% accurate.
	 */
	private void readStream(InputStream inputStream) throws IOException {
		boolean isPreviousLineCRLF = false;
		while(true) { // We read continiously from the bytes we receive and only break where there are no more bytes in the inputStream passed to us
			if(currentStreamEnded) break; // The stream ends when we have read all bytes in the chunk NIO passed to us
			else {
				if(readingHeaderLines) {// We are in state to read header lines right now
					isPreviousLineCRLF = readMessageSipHeaderLines(inputStream, isPreviousLineCRLF);
				}
				if(readingMessageBodyContents) { // We've already read the headers an now we are reading the Contents of the SIP message (which doesn't generally have lines)
					readMessageBody(inputStream);
				}
			}
		}
	}
	
	private boolean readMessageSipHeaderLines(InputStream inputStream, boolean isPreviousLineCRLF) throws IOException {
		boolean crlfReceived = false;
		String line = readLine(inputStream); // This gives us a full line or if it didn't fit in the byte check it may give us part of the line
		if(partialLineRead) {
			partialLine = partialLine + line; // If we are reading partial line again we must concatenate it with the previous partial line to reconstruct the full line
		} else {
			line = partialLine + line; // If we reach the end of the line in this chunk we concatenate it with the partial line from the previous buffer to have a full line
			partialLine = ""; // Reset the partial line so next time we will concatenate empty string instead of the obsolete partial line that we just took care of
			if(!line.equals(CRLF)) { // CRLF indicates END of message headers by RFC
				message.append(line); // Collect the line so far in the message buffer (line by line)
                String lineIgnoreCase = line.toLowerCase();
                // contribution from Alexander Saveliev compare to lower case as RFC 3261 states (7.3.1 Header Field Format) states that header fields are case-insensitive
				if(lineIgnoreCase.startsWith(ContentLengthHeader.NAME.toLowerCase())) { // naive Content-Length header parsing to figure out how much bytes of message body must be read after the SIP headers
					contentLength = Integer.parseInt(line.substring(
							ContentLengthHeader.NAME.length()+1).trim());
				} else if(lineIgnoreCase.startsWith(CallIdHeader.NAME.toLowerCase())) { // naive Content-Length header parsing to figure out how much bytes of message body must be read after the SIP headers
					callId = line.substring(
							CallIdHeader.NAME.length()+1).trim();
				}
			} else {				
				if(isPreviousLineCRLF) {
            		// Handling keepalive ping (double CRLF) as defined per RFC 5626 Section 4.4.1
                	// sending pong (single CRLF)
                	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger.logDebug("KeepAlive Double CRLF received, sending single CRLF as defined per RFC 5626 Section 4.4.1");
                        logger.logDebug("~~~ setting isPreviousLineCRLF=false");
                    }

                	crlfReceived = false;

                	try {
						sipMessageListener.sendSingleCLRF();
					} catch (Exception e) {						
						logger.logError("A problem occured while trying to send a single CLRF in response to a double CLRF", e);
					}                	                	
            	} else {
            		crlfReceived = true;
                	if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                    	logger.logDebug("Received CRLF");
                    }
                	if(sipMessageListener != null && 
                			sipMessageListener instanceof ConnectionOrientedMessageChannel) {
                		((ConnectionOrientedMessageChannel)sipMessageListener).cancelPingKeepAliveTimeoutTaskIfStarted();
                	}
            	}
				if(message.length() > 0) { // if we havent read any headers yet we are between messages and ignore CRLFs
					readingMessageBodyContents = true;
					readingHeaderLines = false;
					partialLineRead = false;
					message.append(CRLF); // the parser needs CRLF at the end, otherwise fails TODO: Is that a bug?
					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
						logger.logDebug("Content Length parsed is " + contentLength);
					}

					contentReadSoFar = 0;
					messageBody = new byte[contentLength];
				}
			}			
		}
		return crlfReceived;
	}

	// This method must be called repeatedly until the inputStream returns -1 or some error conditions is triggered
	private void readMessageBody(InputStream inputStream) throws IOException {
		int bytesRead = 0;
		if(contentLength>0) {
			bytesRead = readChunk(inputStream, messageBody, contentReadSoFar, contentLength-contentReadSoFar);
			if(bytesRead == -1) {
				currentStreamEnded = true;
				bytesRead = 0; // avoid passing by a -1 for a one-off bug when contentReadSoFar gets wrong
			}
		}
		contentReadSoFar += bytesRead;
		if(contentReadSoFar == contentLength) { // We have read the full message headers + body
			sizeCounter = maxMessageSize;
			readingHeaderLines = true;
			readingMessageBodyContents = false;
			final String msgLines = message.toString();
			message = new StringBuffer();
			final byte[] msgBodyBytes = messageBody;
			final int finalContentLength = contentLength;
			
			
			if(PostParseExecutorServices.getPostParseExecutor() != null) {
				final String callId = this.callId;
				if(callId == null || callId.trim().length() < 1) {
					// http://code.google.com/p/jain-sip/issues/detail?id=18
					// NIO Message with no Call-ID throws NPE
					throw new IOException("received message with no Call-ID");
				}
                // http://dmy999.com/article/34/correct-use-of-concurrenthashmap
                CallIDOrderingStructure orderingStructure = messagesOrderingMap.get(callId);
                if(orderingStructure == null) {
                    CallIDOrderingStructure newCallIDOrderingStructure = new CallIDOrderingStructure();
                    orderingStructure = messagesOrderingMap.putIfAbsent(callId, newCallIDOrderingStructure);
                    if(orderingStructure == null) {
                        orderingStructure = newCallIDOrderingStructure;       
                        if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                            logger.logDebug("new CallIDOrderingStructure added for message " + message);
                        }
                    }
                }
                final CallIDOrderingStructure callIDOrderingStructure = orderingStructure;                                 
                // we add the message to the pending queue of messages to be processed for that call id here 
                // to avoid blocking other messages with a different call id
                // that could be processed in parallel
                callIDOrderingStructure.getMessagesForCallID().offer(new UnparsedMessage(msgLines, msgBodyBytes));                                                                                   
                
                PostParseExecutorServices.getPostParseExecutor().execute(new Dispatch(callIDOrderingStructure, callId)); // run in executor thread
			} else {
				SIPMessage sipMessage = null;
				synchronized(smp) {
					try {
						sipMessage = smp.parseSIPMessage(msgLines.getBytes(), false, false, null);
						sipMessage.setMessageContent(msgBodyBytes);
					} catch (ParseException e) {
						logger.logError("Parsing problem", e);
					}
				}
				this.contentLength = 0;
				processSIPMessage(sipMessage);
			}
		}

	}

	public void processSIPMessage(SIPMessage message) {
		try {
			sipMessageListener.processMessage(message);
		} catch (Exception e) {
			logger.logError("Can't process message", e);
		}
	}
	
	public synchronized void addBytes(byte[] bytes)  throws Exception{
		currentStreamEnded = false;
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		readStream(inputStream);
	}


    
    /**
     * default constructor.
     */
    protected NioPipelineParser() {
        super();        
    }

    /**
     * Constructor when we are given a message listener and an input stream
     * (could be a TCP connection or a file)
     *
     * @param sipMessageListener
     *            Message listener which has methods that get called back from
     *            the parser when a parse is complete
     * @param in
     *            Input stream from which to read the input.
     * @param debug
     *            Enable/disable tracing or lexical analyser switch.
     */
    public NioPipelineParser(SIPTransactionStack sipStack, SIPMessageListener sipMessageListener,
             boolean debug, int maxMessageSize) {
        this();
        this.sipStack = sipStack;
        this.smp = sipStack.getMessageParserFactory().createMessageParser(sipStack);
        this.sipMessageListener = sipMessageListener;
        this.maxMessageSize = maxMessageSize;
        this.sizeCounter = this.maxMessageSize;

    }

    /**
     * This is the constructor for the pipelined parser.
     *
     * @param mhandler
     *            a SIPMessageListener implementation that provides the message
     *            handlers to handle correctly and incorrectly parsed messages.
     * @param in
     *            An input stream to read messages from.
     */

    public NioPipelineParser(SIPTransactionStack sipStack, SIPMessageListener mhandler,
            int maxMsgSize) {
        this(sipStack, mhandler, false, maxMsgSize);
    }

    /**
     * Add a class that implements a SIPMessageListener interface whose methods
     * get called * on successful parse and error conditons.
     *
     * @param mlistener
     *            a SIPMessageListener implementation that can react to correct
     *            and incorrect pars.
     */

    public void setMessageListener(SIPMessageListener mlistener) {
        sipMessageListener = mlistener;
    }
    
	private int readChunk(InputStream inputStream, byte[] where, int offset, int length) throws IOException {
		int read =  inputStream.read(where, offset, length);
		sizeCounter -= read;
		checkLimits();
		return read;
	}
	
	private int readSingleByte(InputStream inputStream) throws IOException {
		sizeCounter --;
		checkLimits();
		return inputStream.read();
	}
	
	private void checkLimits() {
		if(maxMessageSize > 0 && sizeCounter < 0) throw new RuntimeException("Max Message Size Exceeded " + maxMessageSize);
	}

    /**
     * read a line of input. Note that we encode the result in UTF-8
     */
    private String readLine(InputStream inputStream) throws IOException {
    	partialLineRead = false;
        int counter = 0;
        int increment = 1024;
        int bufferSize = increment;
        byte[] lineBuffer = new byte[bufferSize];
        // handles RFC 5626 CRLF keepalive mechanism
        byte[] crlfBuffer = new byte[2];
        int crlfCounter = 0;
        while (true) {
            char ch;
            int i = readSingleByte(inputStream);
            if (i == -1) {
                partialLineRead = true;
                currentStreamEnded = true;
                break;
            } else
                ch = (char) ( i & 0xFF);
            
            if (ch != '\r')
                lineBuffer[counter++] = (byte) (i&0xFF);
            else if (counter == 0)            	
            	crlfBuffer[crlfCounter++] = (byte) '\r';
                       
            if (ch == '\n') {
            	if(counter == 1 && crlfCounter > 0) {
            		crlfBuffer[crlfCounter++] = (byte) '\n';            		
            	} 
            	break;            	
            }
            
            if( counter == bufferSize ) {
                byte[] tempBuffer = new byte[bufferSize + increment];
                System.arraycopy((Object)lineBuffer,0, (Object)tempBuffer, 0, bufferSize);
                bufferSize = bufferSize + increment;
                lineBuffer = tempBuffer;
                
            }
        }
        if(counter == 1 && crlfCounter > 0) {
        	return new String(crlfBuffer,0,crlfCounter,"UTF-8");
        } else {
        	return new String(lineBuffer,0,counter,"UTF-8");
        }
        
    }
    

}