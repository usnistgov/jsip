package test.tck.msgflow;

import javax.sip.*;
import java.util.TooManyListenersException;
import test.tck.*;

/**
 * <p>This class collects a single event from a given provider. The collector
 * listens for incoming events until the desired
 * event has been received, all following events are ignored.
 * After extractCollectedEvent method has been
 * called, the SipEventCollecter removes itself as a listener from the provider
 * and goes back  to its original state (i.e. could be reused).
 * The collector ignores all events except the one it is
 * supposed to collect. If an extract operation is a attempted on an event
 * other than the collected event a TckInternalError is thrown.
 * All exceptions raised within the collector are let to pass through
 * and are responsibility of the using class</p>
 *
 * <p>This class does not test event dispatching! Event dispatching tests
 * should have been performed before using this class in order to assure
 * coherent test reports.</p>
 * @author Emil Ivov
 *         Network Research Team, Louis Pasteur University, Strasbourg, France.
 *  This code is in the public domain.
 * @version 1.0
 */

class SipEventCollector
{
    private static long MESSAGE_QUEUE_EMPTIES_FOR = 500;

    private SipProvider sipProvider = null;

    private RequestCollector requestCollector = null;
    private ResponseCollector responseCollector = null;
    private TimeoutCollector timeoutCollector = null;

    /**
     * Collects the first javax.sip.RequestEvent fired by the specified provider.
     * All subsequent events and events other than javax.sip.RequestEvent
     * are ignored.
     *
     * @param provider the provider that this collector should listen to
     * @throws TooManyListenersException in case the specified provider throws
     * that exception. It is the responsibility of the calling class to encapsulate
     * it with the corresponding error (e.g. TckInternalError, AssertFailure or
     * TiUnexpectedError) and throw it in its turn to upper layers.
     */
    public void collectRequestEvent(SipProvider provider) throws
        TooManyListenersException
    {
        assertInitialState();
        sipProvider = provider;

        requestCollector = new RequestCollector();
        sipProvider.addSipListener(requestCollector);
    }

    /**
     * Collects the first javax.sip.ResponseEvent fired by the specified provider.
     * All subsequent events and events other than javax.sip.ResponseEvent
     * are ignored.
     *
     * @param provider the provider that this collector should listen to
     * @throws TooManyListenersException in case the specified provider throws
     * that exception. It is the responsibility of the calling class to encapsulate
     * it with the corresponding error (e.g. TckInternalError, AssertFailure or
     * TiUnexpectedError) and throw it in its turn to upper layers.
     */
    public void collectResponseEvent(SipProvider provider) throws
        TooManyListenersException
    {
        assertInitialState();
        sipProvider = provider;

        responseCollector = new ResponseCollector();
        sipProvider.addSipListener(responseCollector);

    }

    /**
     * Collects the first javax.sip.TimeoutEvent fired by the specified provider.
     * All subsequent events and events other than javax.sip.TimeoutEvent
     * are ignored.
     *
     * @param provider the provider that this collector should listen to
     * @throws TooManyListenersException in case the specified provider throws
     * that exception. It is the responsibility of the calling class to encapsulate
     * it with the corresponding error (e.g. TckInternalError, AssertFailure or
     * TiUnexpectedError) and throw it in its turn to upper layers.
     */
    public void collectTimeoutEvent(SipProvider provider) throws
        TooManyListenersException
    {
        assertInitialState();
        sipProvider = provider;

        timeoutCollector = new TimeoutCollector();
        sipProvider.addSipListener(timeoutCollector);
    }

    /**
     * Returns the collected javax.sip.RequestEvent or null if no event has
     * been collected. After this method is called the SipEventCollector will
     * remove itself from the corresponding javax.sip.SipProvider and reset its
     * internal state so that it could be reused.
     *
     * @return the collected javax.sip.RequestEvent
     * @throws TckInternalError in case the method is called without first
     * calling the collectRequestEvent
     */
    public RequestEvent extractCollectedRequestEvent()
    {
        if (requestCollector == null)
            throw new TckInternalError(
                "A request collect was attempted when the requestCollector was null");
        RequestEvent collectedEvent = requestCollector.collectedEvent;

        sipProvider.removeSipListener(requestCollector);
        resetCollectors();
	try {
	Thread.sleep(200);
	} catch (InterruptedException ex) {
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
     * @throws TckInternalError in case the method is called without first
     * calling the collectResponseEvent
     */
    public ResponseEvent extractCollectedResponseEvent()
    {
        if (responseCollector == null)
            throw new TckInternalError(
                "A response collect was attempted when the responseCollector was null");
        ResponseEvent collectedEvent = responseCollector.collectedEvent;
        sipProvider.removeSipListener(responseCollector);
        resetCollectors();
	try {
	Thread.sleep(200);
	} catch (InterruptedException ex) {
	}
        return collectedEvent;
    }

    /**
	 * Returns the collected javax.sip.TimeoutEvent or null if no event has
	 * been collected. After this method is called the SipEventCollector will
	 * remove itself from the corresponding javax.sip.SipProvider and reset its
	 * internal state so that it could be reused.
	 *
	 * @return the collected javax.sip.TimeoutEvent
	 * @throws TckInternalError in case the method is called without first
	 * calling the collectTimeoutEvent
 	 */
    public TimeoutEvent extractCollectedTimeoutEvent()
    {
        if (timeoutCollector == null)
            throw new TckInternalError(
                "A timeout collect was attempted when the timeoutCollector was null");
        TimeoutEvent collectedEvent = timeoutCollector.collectedEvent;

        sipProvider.removeSipListener(timeoutCollector);
        resetCollectors();
        return collectedEvent;
    }

    private void resetCollectors()
    {
        sipProvider = null;

        requestCollector = null;
        responseCollector = null;
        timeoutCollector = null;

        //Wait for the message queue to go empty
        MessageFlowHarness.sleep(MESSAGE_QUEUE_EMPTIES_FOR);
    }

    private void assertInitialState()
    {
        if (sipProvider != null
            || requestCollector != null
            || responseCollector != null
            || timeoutCollector != null
            )
            throw new TckInternalError(
                "Attempting to start a collect operation "
                + "on a collector that is not in initial state!");
    }

    //========================= COLLECTOR ADAPTERS==============================

    private class RequestCollector
        implements SipListener
    {
        private RequestEvent collectedEvent = null;

        public void processRequest(RequestEvent evt)
        {
            //Ignore secondary and null events
            if (collectedEvent != null || evt == null)
                return;
            collectedEvent = evt;
        }

        public void processResponse(ResponseEvent responseEvent)
        {
        }

        public void processTimeout(TimeoutEvent timeoutEvent)
        {
        }

    }

    private class ResponseCollector
        implements SipListener
    {
        private ResponseEvent collectedEvent = null;

        public void processRequest(RequestEvent requestEvent)
        {
        }

        public void processResponse(ResponseEvent evt)
        {
            //Ignore secondary and null events
            if (collectedEvent != null || evt == null)
                return;
            collectedEvent = evt;
        }

        public void processTimeout(TimeoutEvent timeoutEvent)
        {
        }

    }

    private class TimeoutCollector
        implements SipListener
    {
        private TimeoutEvent collectedEvent = null;

        public void processRequest(RequestEvent requestEvent)
        {
        }

        public void processResponse(ResponseEvent responseEvent)
        {
        }

        public void processTimeout(TimeoutEvent evt)
        {
            //Ignore secondary and null events
            if (collectedEvent != null || evt == null)
                return;
            collectedEvent = evt;
        }
    }

}
