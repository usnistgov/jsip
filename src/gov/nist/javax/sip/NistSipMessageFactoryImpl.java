/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.message.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.sip.*;
import gov.nist.core.*;

/**
* Implements all the support classes that are necessary for the nist-sip
* stack on which the jain-sip stack has been based.
* This is a mapping class to map from the NIST-SIP abstractions to
* the JAIN abstractions. (i.e. It is the glue code that ties
* the NIST-SIP event model and the JAIN-SIP event model together.
* When a SIP Request or SIP Response is read from the corresponding
* messageChannel, the NIST-SIP stack calls the SIPStackMessageFactory 
* implementation that has been registered with it to process the request.)
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
* <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class NistSipMessageFactoryImpl
implements SIPStackMessageFactory
{
    
    
    SipStackImpl 	 sipStackImpl;
    
    /**
     *Construct a new SIP Server Request.
     *@param sipRequest is the SIPRequest from which the SIPServerRequest
     * is to be constructed.
     *@param messageChannel is the MessageChannel abstraction for this
     * 	SIPServerRequest.
     */
    public SIPServerRequestInterface
    newSIPServerRequest
    ( SIPRequest sipRequest, MessageChannel messageChannel ) {
	
        if (messageChannel == null || sipRequest == null )  {
                throw new IllegalArgumentException("Null Arg!");
	}
        
        NistSipMessageHandlerImpl retval = new NistSipMessageHandlerImpl();
	if (messageChannel instanceof SIPTransaction) {
		// If the transaction has already been created
		// then set the transaction channel.
        	retval.transactionChannel = (SIPTransaction)messageChannel;
	}
	SIPTransactionStack theStack = 
			(SIPTransactionStack) messageChannel.getSIPStack();
        retval.sipStack = theStack;
	SipStackImpl sipStackImpl = (SipStackImpl)theStack;
	retval.sipStackImpl = sipStackImpl;
	/**
	ListeningPointImpl listeningPoint = (ListeningPointImpl)
		sipStackImpl.getListeningPoint
		(messageChannel.getPort(),
		 messageChannel.getTransport());
	**/
	retval.listeningPoint = 
		messageChannel.getMessageProcessor().getListeningPoint();
	if (retval.listeningPoint == null) return null;
	if (LogWriter.needsLogging) 
	    LogWriter.logMessage("Returning request interface for " +
			sipRequest.getFirstLine() + " " + retval + 
			" messageChannel = " + messageChannel );
        return  retval;
    }
    
    /**
     * Generate a new server response for the stack.
     *@param sipResponse is the SIPRequest from which the SIPServerRequest
     * is to be constructed.
     *@param messageChannel is the MessageChannel abstraction for this
     * 	SIPServerResponse
     */
    public SIPServerResponseInterface
    newSIPServerResponse
    (SIPResponse sipResponse, MessageChannel messageChannel) {
        NistSipMessageHandlerImpl retval = new NistSipMessageHandlerImpl();
	SIPTransactionStack theStack = (SIPTransactionStack)
			messageChannel.getSIPStack();
        retval.sipStack = theStack;
	SipStackImpl sipStackImpl = (SipStackImpl) theStack;
	retval.sipStackImpl = sipStackImpl;
        // Tr is null if a transaction is not mapped.
        SIPTransaction tr = 
		(SIPTransaction) 
		((SIPTransactionStack)theStack).
		findTransaction(sipResponse,false);
	if (LogWriter.needsLogging)
	    LogWriter.logMessage("Found Transaction " + tr + " for " +
			sipResponse);

        retval.transactionChannel = tr;

	/**
	retval.listeningPoint = (ListeningPointImpl)
		sipStackImpl.getListeningPoint
		(messageChannel.getPort(),messageChannel.getTransport());
	**/
	retval.listeningPoint = 
		messageChannel.getMessageProcessor().getListeningPoint();
        return  retval;
    }


    public NistSipMessageFactoryImpl(SipStackImpl sipStackImpl) {
	this.sipStackImpl = sipStackImpl;
    }

}
