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
 * File Name     : InReplyToHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;

/**
 * The In-Reply-To header field enumerates the Call-IDs that this call
 * references or returns. These Call-IDs may have been cached by the client
 * then included in this header field in a return call.
 * <p>
 * This allows automatic call distribution systems to route return calls to the
 * originator of the first call. This also allows callees to filter calls, so
 * that only return calls for calls they originated will be accepted. This
 * field is not a substitute for request authentication.
 * <p>
 * For Example:<br>
 * <code>In-Reply-To: 70710@saturn.jcp.org, 17320@saturn.jcp.org</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface InReplyToHeader extends Header {

    /**
     * Sets the Call-Id of the InReplyToHeader. The CallId parameter uniquely
     * identifies a serious of messages within a dialogue.
     *
     * @param callId - the string value of the Call-Id of this InReplyToHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the callId value.
     */
    public void setCallId(String callId) throws ParseException;

    /**
     * Returns the Call-Id of InReplyToHeader. The CallId parameter uniquely
     * identifies a series of messages within a dialogue.
     *
     * @return the String value of the Call-Id of this InReplyToHeader
     */
    public String getCallId();

    /**
     * Name of InReplyToHeader
     */
    public final static String NAME = "In-Reply-To";

}

