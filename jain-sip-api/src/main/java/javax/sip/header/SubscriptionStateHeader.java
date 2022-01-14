/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright © 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : SubscriptionStateHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     13/12/2002  Phelim O'Doherty    Initial version, extension header to
 *                                          support RFC3265
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.InvalidArgumentException;
import java.text.ParseException;

/**
  * This interface represents the Subscription State header, as
 * defined by <a href = "http://www.ietf.org/rfc/rfc3265.txt">RFC3265</a>, this
 * header is not part of RFC3261.
 * <p>
 * NOTIFY requests MUST contain SubscriptionState headers which indicate the
 * status of the subscription. The subscription states are:
 * <ul>
 * <li> active - If the SubscriptionState header value is "active", it means
 * that the subscription has been accepted and (in general) has been authorized.
 * If the header also contains an "expires" parameter, the subscriber SHOULD
 * take it as the authoritative subscription duration and adjust accordingly.
 * The "retry-after" and "reason" parameters have no semantics for "active".
 * <li> pending - If the SubscriptionState value is "pending", the
 * subscription has been received by the notifier, but there is insufficient
 * policy information to grant or deny the subscription yet. If the header also
 * contains an "expires" parameter, the subscriber SHOULD take it as the
 * authoritative subscription duration and adjust accordingly. No further
 * action is necessary on the part of the subscriber. The "retry-after" and
 * "reason" parameters have no semantics for "pending".
 * <li> terminated - If the SubscriptionState value is "terminated", the
 * subscriber should consider the subscription terminated. The "expires"
 * parameter has no semantics for "terminated". If a reason code is present, the
 * client should behave as described in the reason code defined in this Header.
 * If no reason code or an unknown reason code is present, the client MAY
 * attempt to re-subscribe at any time (unless a "retry-after" parameter is
 * present, in which case the client SHOULD NOT attempt re-subscription until
 * after the number of seconds specified by the "retry-after" parameter).
 * </ul>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface SubscriptionStateHeader extends Parameters, Header {

    /**
     * Sets the relative expires value of the SubscriptionStateHeader. The
     * expires value MUST be greater than zero and MUST be less than 2**31.
     *
     * @param expires - the new expires value of this SubscriptionStateHeader.
     * @throws InvalidArgumentException if supplied value is less than zero.
     */
    public void setExpires(int expires) throws InvalidArgumentException;

    /**
     * Gets the expires value of the SubscriptionStateHeader. This expires value is
     * relative time.
     *
     * @return the expires value of the SubscriptionStateHeader.
     */
    public int getExpires();

    /**
     * Sets the retry after value of the SubscriptionStateHeader. The retry after value
     * MUST be greater than zero and MUST be less than 2**31.
     *
     * @param retryAfter - the new retry after value of this SubscriptionStateHeader
     * @throws InvalidArgumentException if supplied value is less than zero.
     */
    public void setRetryAfter(int retryAfter) throws InvalidArgumentException;

    /**
     * Gets the retry after value of the SubscriptionStateHeader. This retry after
     * value is relative time.
     *
     * @return the retry after value of the SubscriptionStateHeader.
     */
    public int getRetryAfter();

    /**
     * Gets the reason code of SubscriptionStateHeader.
     *
     * @return the comment of this SubscriptionStateHeader, return null if no reason code
     * is available.
     */
    public String getReasonCode();

    /**
     * Sets the reason code value of the SubscriptionStateHeader.
     *
     * @param reasonCode - the new reason code string value of the SubscriptionStateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the reason code.
     */
    public void setReasonCode(String reasonCode) throws ParseException;

    /**
     * Gets the state of SubscriptionStateHeader.
     *
     * @return the state of this SubscriptionStateHeader.
     */
    public String getState();

    /**
     * Sets the state value of the SubscriptionStateHeader.
     *
     * @param state - the new state string value of the SubscriptionStateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the state.
     */
    public void setState(String state) throws ParseException;


    /**
     * Name of SubscriptionStateHeader
     */
    public final static String NAME = "Subscription-State";

//Reason Code Constants

    /**
     * Reason Code: The reason why the subscription was terminated is Unknown.
     */
    public final static String UNKNOWN = "unknown";

    /**
     * Reason Code: The subscription has been terminated, but the subscriber SHOULD retry
     * immediately with a new subscription. One primary use of such a status
     * code is to allow migration of subscriptions between nodes. The
     * "retry-after" parameter has no semantics for "deactivated".
     */
    public final static String DEACTIVATED = "deactivated";

    /**
     * Reason Code: The subscription has been terminated, but the client SHOULD retry at
     * some later time. If a "retry-after" parameter is also present, the client
     * SHOULD wait at least the number of seconds specified by that parameter
     * before attempting to re-subscribe.
     */
    public final static String PROBATION = "probation";

    /**
     * Reason Code: The subscription has been terminated due to change in authorization
     * policy. Clients SHOULD NOT attempt to re-subscribe. The "retry-after"
     * parameter has no semantics for "rejected".
     */
    public final static String REJECTED = "rejected";

    /**
     * Reason Code: The subscription has been terminated because it was not refreshed before
     * it expired. Clients MAY re-subscribe immediately. The "retry-after"
     * parameter has no semantics for "timeout".
     */
    public final static String TIMEOUT = "timeout";

    /**
     * Reason Code: The subscription has been terminated because the notifier could not
     * obtain authorization in a timely fashion. If a "retry-after" parameter
     * is also present, the client SHOULD wait at least the number of seconds
     * specified by that parameter before attempting to re-subscribe; otherwise,
     * the client MAY retry immediately, but will likely get put back into
     * pending state.
     */
    public final static String GIVE_UP = "giveup";

    /**
     * Reason Code: The subscription has been terminated because the resource state which was
     * being monitored no longer exists. Clients SHOULD NOT attempt to
     * re-subscribe. The "retry-after" parameter has no semantics for "noresource".
     */
    public final static String NO_RESOURCE = "noresource";

// State constants

    /**
     * State: The subscription has been accepted and (in general) has been
     * authorized.
     */
    public final static String ACTIVE = "active";

    /**
     * State: The subscription has been terminated, if a reason code is present,
     * the client should behave as described in the reason code.
     */
    public final static String TERMINATED = "terminated";

    /**
     * State: The subscription has been received by the notifier, but there is
     * insufficient policy information to grant or deny the subscription yet.
     */
    public final static String PENDING = "pending";

}

