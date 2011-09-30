/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others.
* This software is has been contributed to the public domain.
* As a result, a formal license is not needed to use the software.
*
* This software is provided "AS IS."
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
*
*/
package test.tck.msgflow;

import javax.sip.*;

import org.apache.log4j.Logger;

import java.util.TooManyListenersException;
import test.tck.*;

/**
 * <p>
 * This class collects a single event from a given provider. The collector
 * listens for incoming events until the desired event has been received, all
 * following events are ignored. After extractCollectedEvent method has been
 * called, the SipEventCollecter removes itself as a listener from the provider
 * and goes back to its original state (i.e. could be reused). The collector
 * ignores all events except the one it is supposed to collect. If an extract
 * operation is a attempted on an event other than the collected event a
 * TckInternalError is thrown. All exceptions raised within the collector are
 * let to pass through and are responsibility of the using class
 * </p>
 *
 * <p>
 * This class does not test event dispatching! Event dispatching tests should
 * have been performed before using this class in order to assure coherent test
 * reports.
 * </p>
 *
 * @author Emil Ivov Network Research Team, Louis Pasteur University,
 *         Strasbourg, France.
 * @version 1.0
 */

class SipEventCollector {
    private static long MESSAGE_QUEUE_EMPTIES_FOR = 500;

    private RequestCollector requestCollector = null;

    private ResponseCollector responseCollector = null;

    private TimeoutCollector timeoutCollector = null;

    private DialogTerminationCollector dialogTerminationCollector = null;

    private TransactionTerminationCollector transactionTerminationCollector = null;

    private static Logger logger = Logger.getLogger(SipEventCollector.class);

    private void initCollectors(SipProvider sipProvider) {
        this.requestCollector = new RequestCollector(sipProvider);
        this.responseCollector = new ResponseCollector(sipProvider);
        this.timeoutCollector = new TimeoutCollector(sipProvider);
        this.dialogTerminationCollector = new DialogTerminationCollector(
                sipProvider);
        this.transactionTerminationCollector = new TransactionTerminationCollector(
                sipProvider);
    }

    /**
     * Collects the first javax.sip.RequestEvent fired by the specified
     * provider. All subsequent events and events other than
     * javax.sip.RequestEvent are ignored.
     *
     * @param provider
     *            the provider that this collector should listen to
     * @throws TooManyListenersException
     *             in case the specified provider throws that exception. It is
     *             the responsibility of the calling class to encapsulate it
     *             with the corresponding error (e.g. TckInternalError,
     *             AssertFailure or TiUnexpectedError) and throw it in its turn
     *             to upper layers.
     */
    public void collectRequestEvent(SipProvider provider)
            throws TooManyListenersException {

        initCollectors(provider);
        provider.addSipListener(requestCollector);
    }

    /**
     * Collects the first javax.sip.ResponseEvent fired by the specified
     * provider. All subsequent events and events other than
     * javax.sip.ResponseEvent are ignored.
     *
     * @param provider
     *            the provider that this collector should listen to
     * @throws TooManyListenersException
     *             in case the specified provider throws that exception. It is
     *             the responsibility of the calling class to encapsulate it
     *             with the corresponding error (e.g. TckInternalError,
     *             AssertFailure or TiUnexpectedError) and throw it in its turn
     *             to upper layers.
     */
    public void collectResponseEvent(SipProvider provider)
            throws TooManyListenersException {

        initCollectors(provider);
        provider.addSipListener(responseCollector);

    }

    /**
     * Collects the first javax.sip.TimeoutEvent fired by the specified
     * provider. All subsequent events and events other than
     * javax.sip.TimeoutEvent are ignored.
     *
     * @param provider
     *            the provider that this collector should listen to
     * @throws TooManyListenersException
     *             in case the specified provider throws that exception. It is
     *             the responsibility of the calling class to encapsulate it
     *             with the corresponding error (e.g. TckInternalError,
     *             AssertFailure or TiUnexpectedError) and throw it in its turn
     *             to upper layers.
     */
    public void collectTimeoutEvent(SipProvider provider)
            throws TooManyListenersException {
        initCollectors(provider);
        provider.addSipListener(timeoutCollector);
    }

    /**
     * Collects the first javax.sip.TransactionTerminated fired by the specified
     * provider. All subsequent events and events other than
     * javax.sip.TimeoutEvent are ignored.
     *
     * @param provider
     *            the provider that this collector should listen to
     * @throws TooManyListenersException
     *             in case the specified provider throws that exception. It is
     *             the responsibility of the calling class to encapsulate it
     *             with the corresponding error (e.g. TckInternalError,
     *             AssertFailure or TiUnexpectedError) and throw it in its turn
     *             to upper layers.
     */
    public void collectTransactionTermiatedEvent(SipProvider provider)
            throws TooManyListenersException {
        initCollectors(provider);
        provider.addSipListener(transactionTerminationCollector);
    }

    /**
     * Collects the first javax.sip.TransactionTerminated fired by the specified
     * provider. All subsequent events and events other than
     * javax.sip.TimeoutEvent are ignored.
     *
     * @param provider
     *            the provider that this collector should listen to
     * @throws TooManyListenersException
     *             in case the specified provider throws that exception. It is
     *             the responsibility of the calling class to encapsulate it
     *             with the corresponding error (e.g. TckInternalError,
     *             AssertFailure or TiUnexpectedError) and throw it in its turn
     *             to upper layers.
     */
    public void collectDialogTermiatedEvent(SipProvider provider)
            throws TooManyListenersException {
        initCollectors(provider);
        provider.addSipListener(dialogTerminationCollector);
    }

    /**
     * Returns the collected javax.sip.RequestEvent or null if no event has been
     * collected. After this method is called the SipEventCollector will remove
     * itself from the corresponding javax.sip.SipProvider and reset its
     * internal state so that it could be reused.
     *
     * @return the collected javax.sip.RequestEvent
     * @throws TckInternalError
     *             in case the method is called without first calling the
     *             collectRequestEvent
     */
    public RequestEvent extractCollectedRequestEvent() {
        return this.extractCollectedRequestEvent(true);
    }

    public RequestEvent extractCollectedRequestEvent(boolean toReset) {
        if (requestCollector == null)
            throw new TckInternalError(
                    "A request collect was attempted when the requestCollector was null");
        RequestEvent collectedEvent = requestCollector.collectedEvent;

        if (toReset) {
            requestCollector.provider.removeSipListener(requestCollector);
            resetCollectors();
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
            }
        }

        return collectedEvent;
    }

    /**
     * Returns the collected javax.sip.ResponseEvent or null if no event has
     * been collected. After this method is called the SipEventCollector will
     * remove itself from the corresponding javax.sip.SipProvider and reset its
     * internal state so that it could be reused.
     *
     * @return the collected javax.sip.ResponseEvent
     * @throws TckInternalError
     *             in case the method is called without first calling the
     *             collectResponseEvent
     */
    public ResponseEvent extractCollectedResponseEvent() {
        if (responseCollector == null)
            throw new TckInternalError(
                    "A response collect was attempted when the responseCollector was null");
        ResponseEvent collectedEvent = responseCollector.collectedEvent;
        responseCollector.provider.removeSipListener(responseCollector);
        resetCollectors();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
        }
        return collectedEvent;
    }

    /**
     * Returns the collected javax.sip.TimeoutEvent or null if no event has been
     * collected. After this method is called the SipEventCollector will remove
     * itself from the corresponding javax.sip.SipProvider and reset its
     * internal state so that it could be reused.
     *
     * @return the collected javax.sip.TimeoutEvent
     * @throws TckInternalError
     *             in case the method is called without first calling the
     *             collectTimeoutEvent
     */
    public TimeoutEvent extractCollectedTimeoutEvent() {
        if (timeoutCollector == null)
            throw new TckInternalError(
                    "A timeout collect was attempted when the timeoutCollector was null");
        TimeoutEvent collectedEvent = timeoutCollector.collectedEvent;

        timeoutCollector.provider.removeSipListener(timeoutCollector);
        resetCollectors();
        return collectedEvent;
    }

    /**
     * Returns the collected javax.sip.DialogTerminatedEvent or null if no event
     * has been collected. After this method is called the SipEventCollector
     * will remove itself from the corresponding javax.sip.SipProvider and reset
     * its internal state so that it could be reused.
     *
     * @return the collected javax.sip.TimeoutEvent
     * @throws TckInternalError
     *             in case the method is called without first calling the
     *             collectTimeoutEvent
     */
    public DialogTerminatedEvent extractCollectedDialogTerminatedEvent() {
        if (dialogTerminationCollector == null)
            throw new TckInternalError(
                    "A dialog-terminated collect was attempted when the dialogTerminationCollector was null");
        DialogTerminatedEvent collectedEvent = this.dialogTerminationCollector.collectedEvent;

        dialogTerminationCollector.provider.removeSipListener(dialogTerminationCollector);
        resetCollectors();
        return collectedEvent;
    }

    /**
     * Returns the collected javax.sip.DialogTerminatedEvent or null if no event
     * has been collected. After this method is called the SipEventCollector
     * will remove itself from the corresponding javax.sip.SipProvider and reset
     * its internal state so that it could be reused.
     *
     * @return the collected javax.sip.TimeoutEvent
     * @throws TckInternalError
     *             in case the method is called without first calling the
     *             collectTimeoutEvent
     */
    public TransactionTerminatedEvent extractCollectedTransactionTerminatedEvent() {
        if (transactionTerminationCollector == null)
            throw new TckInternalError(
                    "A timeout collect was attempted when the transactionTerminationCollector was null");
        TransactionTerminatedEvent collectedEvent = this.transactionTerminationCollector.collectedEvent;

        transactionTerminationCollector.provider
                .removeSipListener(transactionTerminationCollector);
        resetCollectors();
        return collectedEvent;
    }

    private void resetCollectors() {
        /**
         * sipProvider = null;
         *
         * requestCollector = null; responseCollector = null; timeoutCollector =
         * null; dialogTerminationCollector = null;
         * transactionTerminationCollector = null;
         */

        // Wait for the message queue to go empty
        MessageFlowHarness.sleep(MESSAGE_QUEUE_EMPTIES_FOR);
    }

    private void assertInitialState(Class eventClass) {
        boolean failureFlag = false;
        if (eventClass.equals(RequestEvent.class)) {
            if (requestCollector != null
                    && requestCollector.collectedEvent != null) {
                failureFlag = true;
            }
        } else if (eventClass.equals(ResponseEvent.class)) {
            if (responseCollector != null
                    && responseCollector.collectedEvent != null) {

                failureFlag = true;
            }
        } else if (eventClass.equals(TimeoutEvent.class)) {
            if (timeoutCollector != null
                    && timeoutCollector.collectedEvent != null) {
                failureFlag = true;
            }
        } else if (eventClass.equals(TransactionTerminatedEvent.class)) {
            if (this.transactionTerminationCollector != null
                    && this.transactionTerminationCollector.collectedEvent != null)
                failureFlag = true;

        } else if (eventClass.equals(DialogTerminatedEvent.class)) {
            if (this.dialogTerminationCollector != null
                    && this.dialogTerminationCollector.collectedEvent != null)
                failureFlag = true;
        }

        if (failureFlag)
            throw new TckInternalError(
                    "Attempting to start a collect operation "
                            + "on a collector that is not in initial state!");
    }

    // ========================= COLLECTOR
    // ADAPTERS==============================

    private class RequestCollector implements SipListener {
        private RequestEvent collectedEvent = null;

        private SipProvider provider;

        public RequestCollector(SipProvider sipProvider) {
            this.provider = sipProvider;
        }

        public void processRequest(RequestEvent evt) {
            // Ignore secondary and null events
            if (collectedEvent != null || evt == null)
                return;
            collectedEvent = evt;
        }

        public void processResponse(ResponseEvent responseEvent) {
        }

        public void processTimeout(TimeoutEvent timeoutEvent) {
        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            // TODO Auto-generated method stub
            logger.debug("processIOException");
        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            // TODO Auto-generated method stub
            logger.debug("processTransactionTerminated");
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            // TODO Auto-generated method stub
            logger.debug("processDialogTerminated");
        }

    }

    private class ResponseCollector implements SipListener {
        private ResponseEvent collectedEvent = null;

        private SipProvider provider;

        public ResponseCollector(SipProvider sipProvider) {
            this.provider = sipProvider;
        }

        public void processRequest(RequestEvent requestEvent) {
        }

        public void processResponse(ResponseEvent evt) {
            // Ignore secondary and null events
            if (collectedEvent != null || evt == null)
                return;
            collectedEvent = evt;
        }

        public void processTimeout(TimeoutEvent timeoutEvent) {
            logger.debug("processTimeout");
        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.error("processIOException");
        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            logger.info("transaction terminated event recieved");
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("processDialogTerminated");
        }

    }

    private class TransactionTerminationCollector implements SipListener {

        private TransactionTerminatedEvent collectedEvent;

        private SipProvider provider;

        public TransactionTerminationCollector(SipProvider sipProvider) {
            this.provider = sipProvider;
        }

        public void processRequest(RequestEvent requestEvent) {

        }

        public void processResponse(ResponseEvent responseEvent) {

        }

        public void processTimeout(TimeoutEvent timeoutEvent) {

        }

        public void processIOException(IOExceptionEvent exceptionEvent) {

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            this.collectedEvent = transactionTerminatedEvent;
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {

        }

    }

    private class DialogTerminationCollector implements SipListener {

        private DialogTerminatedEvent collectedEvent;

        private SipProvider provider;

        public DialogTerminationCollector(SipProvider sipProvider) {
            this.provider = sipProvider;
        }

        public void processRequest(RequestEvent requestEvent) {

        }

        public void processResponse(ResponseEvent responseEvent) {

        }

        public void processTimeout(TimeoutEvent timeoutEvent) {

        }

        public void processIOException(IOExceptionEvent exceptionEvent) {

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {

        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            this.collectedEvent = dialogTerminatedEvent;

        }

    }

    private class TimeoutCollector implements SipListener {
        private TimeoutEvent collectedEvent = null;

        private SipProvider provider;

        public TimeoutCollector(SipProvider sipProvider) {
            this.provider = sipProvider;
        }

        public void processRequest(RequestEvent requestEvent) {
            requestCollector.processRequest(requestEvent);
        }

        public void processResponse(ResponseEvent responseEvent) {
        }

        public void processTimeout(TimeoutEvent evt) {
            // Ignore secondary and null events
            if (collectedEvent != null || evt == null)
                return;
            collectedEvent = evt;
        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            // TODO Auto-generated method stub
            logger.info("processIOException");
        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            // TODO Auto-generated method stub
            logger.info("processTransactionTerminated");
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            // TODO Auto-generated method stub
            logger.info("processDialogTerminated");
        }
    }

}
