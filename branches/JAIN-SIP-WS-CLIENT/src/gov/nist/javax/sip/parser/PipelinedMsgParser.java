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

/*
 *
 * Lamine Brahimi and Yann Duponchel (IBM Zurich) noticed that the parser was
 * blocking so I threw out some cool pipelining which ran fast but only worked
 * when the phase of the moon matched its mood. Now things are serialized and
 * life goes slower but more reliably.
 *
 */
import gov.nist.core.CommonLogger;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.LogLevels;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.ContentLength;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.stack.BlockingQueueDispatchAuditor;
import gov.nist.javax.sip.stack.ConnectionOrientedMessageChannel;
import gov.nist.javax.sip.stack.QueuedMessageDispatchBase;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This implements a pipelined message parser suitable for use with a stream -
 * oriented input such as TCP. The client uses this class by instatiating with
 * an input stream from which input is read and fed to a message parser. It
 * keeps reading from the input stream and process messages in a never ending
 * interpreter loop. The message listener interface gets called for processing
 * messages or for processing errors. The payload specified by the
 * content-length header is read directly from the input stream. This can be
 * accessed from the SIPMessage using the getContent and getContentBytes methods
 * provided by the SIPMessage class.
 *
 * @version 1.2 $Revision: 1.39 $ $Date: 2010-12-02 22:04:22 $
 *
 * @author M. Ranganathan
 *
 * @see SIPMessageListener
 */
public final class PipelinedMsgParser implements Runnable {
	private static StackLogger logger = CommonLogger.getLogger(PipelinedMsgParser.class);

	private static final String CRLF = "\r\n";

    /**
     * The message listener that is registered with this parser. (The message
     * listener has methods that can process correct and erroneous messages.)
     */
    protected SIPMessageListener sipMessageListener;
    private Thread mythread; // Preprocessor thread
    //private byte[] messageBody;
    //private boolean errorFlag;
    private Pipeline rawInputStream;
    private int maxMessageSize;
    private int sizeCounter;
    private SIPTransactionStack sipStack;
    private MessageParser smp = null;
    private ConcurrentHashMap<String, CallIDOrderingStructure> messagesOrderingMap = new ConcurrentHashMap<String, CallIDOrderingStructure>();
    boolean isRunning = false;
    
    /**
     * default constructor.
     */
    protected PipelinedMsgParser() {
        super();        
    }

    private static int uid = 0;

    private static synchronized int getNewUid() {
        return uid++;
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
    public PipelinedMsgParser(SIPTransactionStack sipStack, SIPMessageListener sipMessageListener,
            Pipeline in, boolean debug, int maxMessageSize) {
        this();
        this.sipStack = sipStack;
        smp = sipStack.getMessageParserFactory().createMessageParser(sipStack);
        this.sipMessageListener = sipMessageListener;
        rawInputStream = in;
        this.maxMessageSize = maxMessageSize;
        mythread = new Thread(this);
        mythread.setName("PipelineThread-" + getNewUid());

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

    public PipelinedMsgParser(SIPTransactionStack sipStack, SIPMessageListener mhandler, Pipeline in,
            int maxMsgSize) {
        this(sipStack, mhandler, in, false, maxMsgSize);
    }

    /**
     * This is the constructor for the pipelined parser.
     *
     * @param in -
     *            An input stream to read messages from.
     */

    public PipelinedMsgParser(SIPTransactionStack sipStack, Pipeline in) {
        this(sipStack, null, in, false, 0);
    }

    /**
     * Start reading and processing input.
     */
    public void processInput() {
        mythread.start();
    }

    /**
     * Create a new pipelined parser from an existing one.
     *
     * @return A new pipelined parser that reads from the same input stream.
     */
    protected Object clone() {
        PipelinedMsgParser p = new PipelinedMsgParser();

        p.rawInputStream = this.rawInputStream;
        p.sipMessageListener = this.sipMessageListener;
        Thread mythread = new Thread(p);
        mythread.setName("PipelineThread");
        return p;
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

    /**
     * read a line of input. Note that we encode the result in UTF-8
     */
  
    
    private String readLine(InputStream inputStream) throws IOException {
        int counter = 0;
        int increment = 1024;
        int bufferSize = increment;
        byte[] lineBuffer = new byte[bufferSize];
        // handles RFC 5626 CRLF keepalive mechanism
        byte[] crlfBuffer = new byte[2];
        int crlfCounter = 0;
        while (true) {
            char ch;
            int i = inputStream.read();
            if (i == -1) {
                throw new IOException("End of stream");
            } else
                ch = (char) ( i & 0xFF);
            // reduce the available read size by 1 ("size" of a char).
            if (this.maxMessageSize > 0) {
                this.sizeCounter--;
                if (this.sizeCounter <= 0)
                    throw new IOException("Max size exceeded!");
            }
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
            final Queue<SIPMessage> messagesForCallID = callIDOrderingStructure.getMessagesForCallID();
            if(sipStack.sipEventInterceptor != null) {
            	sipStack.sipEventInterceptor.beforeMessage(messagesForCallID.peek());
            }
            try {                                                                                
                semaphore.acquire();                                        
            } catch (InterruptedException e) {
                logger.logError("Semaphore acquisition for callId " + callId + " interrupted", e);
            }
            // once acquired we get the first message to process
            SIPMessage message = messagesForCallID.poll();
            if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            	logger.logDebug("semaphore acquired for message " + message);
            }
            
            try {
                sipMessageListener.processMessage(message);
            } catch (Exception e) {
            	logger.logError("Error occured processing message", e);    
                // We do not break the TCP connection because other calls use the same socket here
            } finally {                                        
                if(messagesForCallID.size() <= 0) {
                    messagesOrderingMap.remove(callId);
                    if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                    	logger.logDebug("CallIDOrderingStructure removed for message " + callId);
                    }
                }
                if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                	logger.logDebug("releasing semaphore for message " + message);
                }
                //release the semaphore so that another thread can process another message from the call id queue in the correct order
                // or a new message from another call id queue
                semaphore.release(); 
                if(messagesOrderingMap.isEmpty()) {
                    synchronized (messagesOrderingMap) {
                        messagesOrderingMap.notify();
                    }
                }
                if(sipStack.sipEventInterceptor != null) {
                	sipStack.sipEventInterceptor.afterMessage(message);
                }
            }
            if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            	logger.logDebug("dispatch task done on " + message + " threadname " + mythread.getName());
            }
        }
		public long getReceptionTime() {
			return time;
		}
    };
    /**
     * This is input reading thread for the pipelined parser. You feed it input
     * through the input stream (see the constructor) and it calls back an event
     * listener interface for message processing or error. It cleans up the
     * input - dealing with things like line continuation
     */
    public void run() {

        Pipeline inputStream = this.rawInputStream;
        final StackLogger stackLogger = logger;
        // inputStream = new MyFilterInputStream(this.rawInputStream);
        // I cannot use buffered reader here because we may need to switch
        // encodings to read the message body.
        try {
            isRunning = true;
            while (isRunning) {
                this.sizeCounter = this.maxMessageSize;
                // this.messageSize = 0;
                StringBuilder inputBuffer = new StringBuilder();

                if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                	logger.logDebug("Starting to parse.");
                }

                String line1;
                String line2 = null;
                boolean isPreviousLineCRLF = false;
                while (true) {
                    try {
                        line1 = readLine(inputStream);
                        // ignore blank lines.
                        if (line1.equals("\n")) {
                        	if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                            	logger.logDebug("Discarding blank line");
                            }
                            continue;
                        } else if(CRLF.equals(line1)) {
                        	if(isPreviousLineCRLF) {
                        		// Handling keepalive ping (double CRLF) as defined per RFC 5626 Section 4.4.1
                            	// sending pong (single CRLF)
                            	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                                    logger.logDebug("KeepAlive Double CRLF received, sending single CRLF as defined per RFC 5626 Section 4.4.1");
                                    logger.logDebug("~~~ setting isPreviousLineCRLF=false");
                                }

                                isPreviousLineCRLF = false;

                            	try {
            						sipMessageListener.sendSingleCLRF();
            					} catch (Exception e) {						
            						logger.logError("A problem occured while trying to send a single CLRF in response to a double CLRF", e);
            					}                	
                            	continue;
                        	} else {
	                        	isPreviousLineCRLF = true;
	                        	if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
	                            	logger.logDebug("Received CRLF");
	                            }
	                        	if(sipMessageListener != null && 
	                        			sipMessageListener instanceof ConnectionOrientedMessageChannel) {
	                        		((ConnectionOrientedMessageChannel)sipMessageListener).cancelPingKeepAliveTimeoutTaskIfStarted();
	                        	}
                        	}
                        	continue;
                        } else 
                            break;
                    } catch (IOException ex) {
                        // we only wait if the thread is still in a running state and hasn't been close from somewhere else
                    	// or we are leaking because the thread is waiting forever
                    	if(PostParseExecutorServices.getPostParseExecutor() != null && isRunning){
                    		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                                logger.logDebug("waiting for messagesOrderingMap " + this + " threadname " + mythread.getName());
                            synchronized (messagesOrderingMap) {
                                try {
                                    messagesOrderingMap.wait(64000);
                                } catch (InterruptedException e) {}                                
                            }  
                            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                                logger.logDebug("got notified for messagesOrderingMap " + this + " threadname " + mythread.getName());                            
                        }
                        this.rawInputStream.stopTimer();
                        if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                        	logger.logDebug("thread ending for threadname " + mythread.getName());
                        }
                        if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                        	logger.logStackTrace(LogLevels.TRACE_DEBUG);
                        }                        
                        return;
                    }
                }

                inputBuffer.append(line1);
                // Guard against bad guys.
                this.rawInputStream.startTimer();
                int bytesRead = 0;
                if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                	logger.logDebug("Reading Input stream.");
                }
                while (true) {
                    try {
                        line2 = readLine(inputStream);
                        bytesRead += line2.length();
                        if(maxMessageSize>0 && bytesRead> (maxMessageSize/2) ) throw new IOException("Pre-content-length headers size exceeded. The size of the message of the headers prior to Content-Length is too large. This must be an invalid message. Limit is MAX_MESSAGE_SIZE/2=" + maxMessageSize/2);
                        inputBuffer.append(line2);
                        if (line2.trim().equals(""))
                            break;
                    } catch (IOException ex) {
                        // we only wait if the thread is still in a running state and hasn't been close from somewhere else
                    	// or we are leaking because the thread is waiting forever
                    	if(PostParseExecutorServices.getPostParseExecutor() != null && isRunning){
                    		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                                logger.logDebug("waiting for messagesOrderingMap " + this + " threadname " + mythread.getName());
                            synchronized (messagesOrderingMap) {
                                try {
                                    messagesOrderingMap.wait(64000);
                                } catch (InterruptedException e) {}                                
                            }  
                            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                                logger.logDebug("got notified for messagesOrderingMap " + this + " threadname " + mythread.getName());                            
                        }
                        this.rawInputStream.stopTimer();
                        if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                        	logger.logDebug("thread ending for threadname " + mythread.getName());
                        }
                        if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                        	logger.logStackTrace(LogLevels.TRACE_DEBUG);
                        }                        
                        return;
                    }
                }

                // Stop the timer that will kill the read.
                this.rawInputStream.stopTimer();
                inputBuffer.append(line2);               
//                smp.setParseExceptionListener(sipMessageListener);
//                smp.setReadBody(false);
                SIPMessage sipMessage = null;

                try {
                    if (stackLogger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                        stackLogger.logDebug("About to parse : " + inputBuffer.toString());
                    }
                    sipMessage = smp.parseSIPMessage(inputBuffer.toString().getBytes(), false, false, sipMessageListener);
                    if (sipMessage == null) {
                        this.rawInputStream.stopTimer();
                        continue;
                    }
                } catch (ParseException ex) {
                    // Just ignore the parse exception.
                    stackLogger.logError("Detected a parse error", ex);
                    continue;
                }

                if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                	logger.logDebug("Completed parsing message");
                }
                String clString = sipMessage.getHeaderAsFormattedString(ContentLength.NAME);
                if(clString.length()>30) throw new RuntimeException("Bad content lenght header " + clString);
                ContentLength cl = (ContentLength) sipMessage
                        .getContentLength();
                
             
                int contentLength = 0;
                if (cl != null) {
                    contentLength = cl.getContentLength();
                } else {
                    contentLength = 0;
                }

                if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                	logger.logDebug("Content length = " + contentLength);
                }
                
                if(maxMessageSize > 0 && contentLength > maxMessageSize) throw new RuntimeException("Max content size Exceeded! :" + contentLength + " allowed max size is " + maxMessageSize);

                if (contentLength == 0) {
                    sipMessage.removeContent();
                } else if (maxMessageSize == 0
                        || contentLength < this.sizeCounter) {
                    byte[] message_body = new byte[contentLength];
                    int nread = 0;
                    while (nread < contentLength) {
                        // Start my starvation timer.
                        // This ensures that the other end
                        // writes at least some data in
                        // or we will close the pipe from
                        // him. This prevents DOS attack
                        // that takes up all our connections.
                        this.rawInputStream.startTimer();
                        try {

                            int readlength = inputStream.read(message_body,
                                    nread, contentLength - nread);
                            if (readlength > 0) {
                                nread += readlength;
                            } else {
                                break;
                            }
                        } catch (IOException ex) {
                            stackLogger.logError("Exception Reading Content",ex);
                            break;
                        } finally {
                            // Stop my starvation timer.
                            this.rawInputStream.stopTimer();
                        }
                    }
                    sipMessage.setMessageContent(message_body);
                }
                // Content length too large - process the message and
                // return error from there.
                if (sipMessageListener != null) {
                    try {
                        if(PostParseExecutorServices.getPostParseExecutor() == null) {
                        	
                            /**
                             * If gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE is disabled
                             * we continue with the old logic here.
                             */
                        	if(sipStack.sipEventInterceptor != null) {
                            	sipStack.sipEventInterceptor.beforeMessage(sipMessage);
                            }
                            sipMessageListener.processMessage(sipMessage);
                            if(sipStack.sipEventInterceptor != null) {
                            	sipStack.sipEventInterceptor.afterMessage(sipMessage);
                            }
                        } else {
                            /**
                             * gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE is enabled so
                             * we use the threadpool to execute the task.
                             */
                            // we need to guarantee message ordering on the same socket on TCP
                            // so we lock and queue of messages per Call Id
                            
                            final String callId = sipMessage.getCallId().getCallId();
                            // http://dmy999.com/article/34/correct-use-of-concurrenthashmap
                            CallIDOrderingStructure orderingStructure = messagesOrderingMap.get(callId);
                            if(orderingStructure == null) {
                                CallIDOrderingStructure newCallIDOrderingStructure = new CallIDOrderingStructure();
                                orderingStructure = messagesOrderingMap.putIfAbsent(callId, newCallIDOrderingStructure);
                                if(orderingStructure == null) {
                                    orderingStructure = newCallIDOrderingStructure;       
                                    if (stackLogger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                                        stackLogger.logDebug("new CallIDOrderingStructure added for message " + sipMessage);
                                    }
                                }
                            }
                            final CallIDOrderingStructure callIDOrderingStructure = orderingStructure;                                 
                            // we add the message to the pending queue of messages to be processed for that call id here 
                            // to avoid blocking other messages with a different call id
                            // that could be processed in parallel
                            callIDOrderingStructure.getMessagesForCallID().offer(sipMessage);                                                                                   
                            
                            PostParseExecutorServices.getPostParseExecutor().execute(new Dispatch(callIDOrderingStructure, callId)); // run in executor thread
                        }
                    } catch (Exception ex) {
                        // fatal error in processing - close the
                        // connection.
                        break;
                    }
                }
            }
        } finally {
            try {
                cleanMessageOrderingMap();
                if(!inputStream.isClosed()) {
            		inputStream.close();
            	}
            } catch (IOException e) {
                InternalErrorHandler.handleException(e);
            }
        }
    }

    /**
     * Data structure to make sure ordering of Messages is guaranteed under TCP when the post parsing thread pool is used
     * @author jean.deruelle@gmail.com
     *
     */
    class CallIDOrderingStructure {
        private Semaphore semaphore;
        private Queue<SIPMessage> messagesForCallID;
        
        public CallIDOrderingStructure() {
            semaphore = new Semaphore(1, true);
            messagesForCallID = new ConcurrentLinkedQueue<SIPMessage>();
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
        public Queue<SIPMessage> getMessagesForCallID() {
            return messagesForCallID;
        }
    }

    public void close() {
        isRunning = false;
    	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("Closing pipelinedmsgparser " + this + " threadname " + mythread.getName());
        try {
            this.rawInputStream.close();            
        } catch (IOException ex) {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("Couldn't close the rawInputStream " + this + " threadname " + mythread.getName() + " already closed ? " + rawInputStream.isClosed());
            // Ignore.
        }                
        if(PostParseExecutorServices.getPostParseExecutor() != null){
        	cleanMessageOrderingMap();
        	synchronized (mythread) {
            	mythread.notifyAll();
            	//interrupting because there is a race condition on the messagesOrderingMap.wait() that
            	// eventually leads to thread leaking and OutOfMemory
            	mythread.interrupt();
    		} 
        }         
    }
    
    private void cleanMessageOrderingMap() {        
    	// not needed and can cause NPE on close if race condition
//    	for (CallIDOrderingStructure callIDOrderingStructure: messagesOrderingMap.values()) {
//			callIDOrderingStructure.getSemaphore().release();
//			callIDOrderingStructure.getMessagesForCallID().clear();
//		}
        messagesOrderingMap.clear();
        synchronized (messagesOrderingMap) {
            messagesOrderingMap.notifyAll();
        }
    	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("cleaned the messagesOrderingMap " + this + " threadname " + mythread.getName());
    }
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.38  2010/12/02 11:44:16  vralev
 * Issue number:  346
 * Obtained from: vralev
 *
 * Congestion control. Patch + Tests for HEAD
 *
 * Revision 1.37  2010/10/26 23:49:12  vralev
 * Issue number:  337
 * Obtained from: vralev
 *
 * Revision 1.36  2010/10/13 15:26:59  deruelle_jean
 * Fix for TCP calls under load freeze JAIN SIP with TCP_POST_PARSING_THREAD_POOL_SIZE > 0
 *
 * Issue number:
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:
 *
 * Revision 1.35  2010/10/12 18:47:16  deruelle_jean
 * Fix for restarting the stack when the gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE option is used
 *
 * Issue number:
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:
 *
 * Revision 1.34  2010/10/07 15:40:25  deruelle_jean
 * Adding some cleaning up to POST_PARSING_THREAD_POOL option
 *
 * Issue number:
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:
 *
 * Revision 1.33  2010/10/07 15:03:49  deruelle_jean
 * Fixing a deadlock on one post_parser_thread_pool option when there is only 1 thread and message ordering on multiple threads + adding non regression test case
 *
 * Issue number:
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:
 *
 * Revision 1.32  2010/08/19 19:18:01  deruelle_jean
 * Fixing Message Order, there could be race conditions on TCP with multiple threads the order should be maintained
 *
 * Issue number:  301
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:
 *
 * Revision 1.31  2010/07/01 18:53:25  vralev
 * Issue number:  301
 * Obtained from: vralev
 * Submitted by:  vralev
 * Reviewed by:   ranga
 *
 * Handle better conflicting multiple settings. Taking into account only the first one without creating unused threadpools for other settings. In trunk.
 *
 * Revision 1.30  2010/07/01 18:22:56  vralev
 * Issue number:  301
 * Obtained from: vralev
 * Submitted by:  vralev
 * Reviewed by:   ranga
 *
 * Revision 1.29  2010/05/06 14:07:45  deruelle_jean
 * Big update to improve performance by 50% in some cases, TCK + testsuite (cc-buildloop) green, Mobicents Sip Servlets TCK + testsuite green as well
 *
 * Issue number:
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:
 *
 * Revision 1.28  2010/03/19 17:29:46  deruelle_jean
 * Adding getters and setters for the new factories
 *
 * Issue number:
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:
 *
 * Revision 1.27  2010/03/15 17:08:57  deruelle_jean
 * Adding javadoc
 *
 * Issue number:  251
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:   Ranga
 *
 * Revision 1.26  2010/03/15 17:01:21  deruelle_jean
 * Applying patch allowing pluggable message parser implementation
 *
 * Issue number:  251
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:   Ranga
 *
 * Revision 1.25  2010/02/27 17:34:04  mranga
 * Issue number:  269
 * Fix PipelinedMessageParser.java to use UTF-8 encoding when reading stream.
 *
 * Revision 1.24  2010/02/27 06:09:00  mranga
 * Patch from Frederic
 *
 * Revision 1.23  2009/08/16 17:28:28  mranga
 * Issue number:  208
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Add authentication mechanism that uses H(username:domain:password)
 *
 * Revision 1.22  2009/07/17 18:58:02  emcho
 * Converts indentation tabs to spaces so that we have a uniform indentation policy in the whole project.
 *
 * Revision 1.21  2008/05/24 04:10:01  mranga
 *
 * Issue number:   158
 * Obtained from:
 * Submitted by:
 * Reviewed by:   mranga
 *
 * Deliver tx timeout for Canceled INVITE. Fix pipeline thread exit.
 *
 * Revision 1.20  2008/05/22 19:38:07  jbemmel
 * Fix for issue 149: the logic wasn't always closing the internal socket pipe,
 * causing the pipe reader thread to block indefinitely
 *
 * Repeatedly starting/stopping the stack then gives hanging threads
 * Revision 1.19 2007/01/28 13:06:21 mranga
 * Issue number: 99 Obtained from: Submitted by: Reviewed by: mranga
 *
 * Fixed PRACK handling null pointer exception (for proxy case) and cleanup of
 * unused variables.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *
 * Revision 1.18 2006/07/13 09:02:10 mranga Issue number: Obtained from:
 * Submitted by: jeroen van bemmel Reviewed by: mranga Moved some changes from
 * jain-sip-1.2 to java.net
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *
 * Revision 1.4 2006/06/19 06:47:27 mranga javadoc fixups
 *
 * Revision 1.3 2006/06/17 10:18:14 mranga Added some synchronization to the
 * sequence number checking. Small javadoc fixups
 *
 * Revision 1.2 2006/06/16 15:26:28 mranga Added NIST disclaimer to all public
 * domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1 2005/10/04 17:12:35 mranga
 *
 * Import
 *
 *
 * Revision 1.16 2004/11/30 23:28:14 mranga Issue number: 44 Submitted by: Rob
 * Daugherty Reviewed by: M. Ranganathan
 *
 * TCP Pipelining truncates content when other end of pipe is closed.
 *
 * Revision 1.15 2004/05/30 18:55:56 mranga Reviewed by: mranga Move to timers
 * and eliminate the Transaction scanner Thread to improve scalability and
 * reduce cpu usage.
 *
 * Revision 1.14 2004/05/16 14:13:22 mranga Reviewed by: mranga Fixed the
 * use-count issue reported by Peter Parnes. Added property to prevent against
 * content-length dos attacks.
 *
 * Revision 1.13 2004/03/19 04:22:22 mranga Reviewed by: mranga Added IO Pacing
 * for long writes - split write into chunks and flush after each chunk to avoid
 * socket back pressure.
 *
 * Revision 1.12 2004/03/18 22:01:19 mranga Reviewed by: mranga Get rid of the
 * PipedInputStream from pipelined parser to avoid a copy.
 *
 * Revision 1.11 2004/03/07 22:25:23 mranga Reviewed by: mranga Added a new
 * configuration parameter that instructs the stack to drop a server connection
 * after server transaction termination set
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this Default behavior
 * is true.
 *
 * Revision 1.10 2004/02/29 15:32:58 mranga Reviewed by: mranga bug fixes on
 * limiting the max message size.
 *
 * Revision 1.9 2004/02/29 00:46:34 mranga Reviewed by: mranga Added new
 * configuration property to limit max message size for TCP transport. The
 * property is gov.nist.javax.sip.MAX_MESSAGE_SIZE
 *
 * Revision 1.8 2004/02/25 21:43:03 mranga Reviewed by: mranga Added a couple of
 * todo's and removed some debug printlns that could slow code down by a bit.
 *
 * Revision 1.7 2004/02/25 20:52:46 mranga Reviewed by: mranga Fix TCP transport
 * so messages in excess of 8192 bytes are accepted.
 *
 * Revision 1.6 2004/01/22 18:39:41 mranga Reviewed by: M. Ranganathan Moved the
 * ifdef SIMULATION and associated tags to the first column so Prep preprocessor
 * can deal with them.
 *
 * Revision 1.5 2004/01/22 14:23:45 mranga Reviewed by: mranga Fixed some minor
 * formatting issues.
 *
 * Revision 1.4 2004/01/22 13:26:31 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *
 */