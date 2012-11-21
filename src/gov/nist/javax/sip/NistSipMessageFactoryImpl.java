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
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 *******************************************************************************/

package gov.nist.javax.sip;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogLevels;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import javax.sip.*;

/**
 * Implements all the support classes that are necessary for the nist-sip stack
 * on which the jain-sip stack has been based. This is a mapping class to map
 * from the NIST-SIP abstractions to the JAIN abstractions. (i.e. It is the glue
 * code that ties the NIST-SIP event model and the JAIN-SIP event model
 * together. When a SIP Request or SIP Response is read from the corresponding
 * messageChannel, the NIST-SIP stack calls the SIPStackMessageFactory
 * implementation that has been registered with it to process the request.)
 * 
 * @version 1.2 $Revision: 1.18 $ $Date: 2010-12-02 22:04:19 $
 * 
 * @author M. Ranganathan <br/>
 * 
 *  
 */
class NistSipMessageFactoryImpl implements StackMessageFactory {
	private static StackLogger logger = CommonLogger.getLogger(NistSipMessageFactoryImpl.class);
    private SIPTransactionStack sipStack;

    /**
     * Construct a new SIP Server Request.
     * 
     * @param sipRequest
     *            is the SIPRequest from which the SIPServerRequest is to be
     *            constructed.
     * @param messageChannel
     *            is the MessageChannel abstraction for this SIPServerRequest.
     */
    public ServerRequestInterface newSIPServerRequest(SIPRequest sipRequest,
                                                      SIPTransaction sipTransaction) {

        if (sipTransaction == null || sipRequest == null) {
            throw new IllegalArgumentException("Null Arg!");
        }

        SIPTransactionStack theStack = sipTransaction.getSIPStack();
        DialogFilter retval = new DialogFilter(
                theStack);
        // If the transaction has already been created
        // then set the transaction channel.
        retval.transactionChannel = sipTransaction;
        retval.listeningPoint = sipTransaction.getMessageProcessor()
                .getListeningPoint();
        if (retval.listeningPoint == null)
            return null;
        if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
            logger.logDebug(
                    "Returning request interface for "
                            + sipRequest.getFirstLine() + " " + retval
                            + " sipTransaction = " + sipTransaction);
        return retval;
    }

    /**
     * Generate a new server response for the stack.
     * 
     * @param sipResponse
     *            is the SIPRequest from which the SIPServerRequest is to be
     *            constructed.
     * @param messageChannel
     *            is the MessageChannel abstraction for this SIPServerResponse
     */
  public ServerResponseInterface newSIPServerResponse(
            SIPResponse sipResponse, MessageChannel msgChannel) {
        // Tr is null if a transaction is not mapped.
        SIPTransaction tr = sipStack.findTransaction(sipResponse, false);
        if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
            logger.logDebug(
                    "Found Transaction " + tr + " for " + sipResponse);

        if (tr != null) {
            // Prune unhealthy responses early if handling statefully.
            // If the state has not yet been assigned then this is a
            // spurious response. This was moved up from the transaction
            // layer for efficiency.
            if (tr.getInternalState() < 0) {
                if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
                    logger.logDebug(
                            "Dropping response - null transaction state");
                return null;
                // Ignore 1xx
            } else if (TransactionState._COMPLETED == tr.getInternalState()
                    && sipResponse.getStatusCode() / 100 == 1) {
                if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
                    logger.logDebug(
                            "Dropping response - late arriving "
                                    + sipResponse.getStatusCode());
                return null;
            }
        }

        DialogFilter retval = new DialogFilter(sipStack);

        retval.transactionChannel = tr;

        retval.listeningPoint = msgChannel.getMessageProcessor()
                .getListeningPoint();
        return retval;
    }

    public NistSipMessageFactoryImpl(SIPTransactionStack sipStackImpl) {
        this.sipStack = sipStackImpl;

    }

}