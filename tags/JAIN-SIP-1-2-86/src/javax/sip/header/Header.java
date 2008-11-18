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
 * File Name     : Header.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.io.Serializable;

/**
 * This interface is the super interface of all SIP headers supported explicitly
 * by this specification. Extension Headers can be supported by this specification as required 
 * by extending the {@link ExtensionHeader} assuming other endpoints understand 
 * the Header. This specification supports the following headers not defined in 
 * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a> documented in 
 * the following standards track RFCs:
 * <ul>
 * <li> RAckHeader - this header is specific to the reliability of provisional 
 * Responses. This functionality is defined in <a href = "http://www.ietf.org/rfc/rfc3262.txt">RFC3262</a>.
 * <li> RSeqHeader - this header is specific to the reliability of provisional 
 * Responses. This functionality is defined in <a href = "http://www.ietf.org/rfc/rfc3262.txt">RFC3262</a>.
 * <li> AllowEventsHeader - this header is specific to the event notification
 * framework. This functionality is defined in <a href = "http://www.ietf.org/rfc/rfc3265.txt">RFC3265</a>.
 * <li> EventHeader - this header is specific to the event notification
 * framework. This functionality is defined in <a href = "http://www.ietf.org/rfc/rfc3265.txt">RFC3265</a>.
 * <li> SubscriptionStateHeader - this header is specific to the event notification
 * framework. This functionality is defined in <a href = "http://www.ietf.org/rfc/rfc3265.txt">RFC3265</a>.
 * <li> ReasonHeader - The Reason Header provides information on why a SIP 
 * request was issued, often useful when creating services and used to 
 * encapsulate a final status code in a provisional response. This functionality 
 * is defined in <a href = "http://www.ietf.org/rfc/rfc3326.txt">RFC3326</a>.
 * </ul>
 * SIP header fields are similar to HTTP header fields in both syntax and
 * semantics.  Some header fields only make sense in requests or responses. 
 * These are called request header fields and response header fields, respectively. If a
 * header field appears in a message not matching its category (such as a
 * request header field in a response), it MUST be ignored.
 * <p>
 *<b>Header Handling</b>:<br>
 * Any SIP header whose grammar is of the form:
 * <br>
 * <center>header  =  "header-name" HCOLON header-value *(COMMA header-value)
 * </center>
 * <br>
 * allows for combining header fields of the same name into a comma-separated
 * list. In this specification each Header object has a single value or attribute pair. 
 * For example a Header whose grammer is of the form:
 * <br> 
 * <center>Allow: Invite, Bye;</center>
 * <br>
 * would be represented in a SIP message with two AllowHeader objects each 
 * containing a single attribute, Invite and Bye respectively. Implementations 
 * MUST be able to parse multiple header field rows with the same name in any 
 * combination of the single-value-per-line or comma-separated value forms and 
 * translate them into the relevent Header objects defined in this specification.
 * <p> 
 * The relative order of header objects within messages is not significant. 
 * However, it is RECOMMENDED that required header and headers which are needed
 * for proxy processing (Via, Route, Record-Route, Proxy-Require, Max-Forwards,
 * and Proxy-Authorization, for example) appear towards the top of the message
 * to facilitate rapid parsing.
 * <p>
 * The relative order of header objects with the same field name is important. 
 * Multiple headers with the same name MAY be present in a message if and only if 
 * the entire field-value for that header field can be defined as a 
 * comma-separated list as defined by RFC 3261. The exceptions to this rule are 
 * the WWW-Authenticate, Authorization, Proxy-Authenticate, and 
 * Proxy-Authorization header fields. Multiple header objects with these
 * names MAY be present in a message, but since their grammar does not follow
 * the general form listed above, they MUST NOT be combined into a single
 * header field row when sent over the network. 
 * <p>
 * Even though an arbitrary number of parameter pairs may be attached to a 
 * header object, any given parameter-name MUST NOT appear more than once.
 * 
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface Header extends Cloneable, Serializable {


    /**
     * Gets the unique string name of this Header. A name constant is defined in
     * each individual Header identifying each Header.
     *
     * @return the name of this specific Header
     */
    public String getName();

    /**
     * Compare this SIP Header for equality with another. This method overrides
     * the equals method in java.lang.Object. This method is over-ridden further 
     * for each required header (To, From, CSeq, Call-ID, Max-Forwards, and Via) 
     * which object equality is outlined as specified by  
     * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>. All optional 
     * headers are compared using object equality that is each field in the 
     * header is used for comparision. When comparing header fields, field names 
     * are always case-insensitive. Unless otherwise stated in the
     * definition of a particular header field, field values, parameter names,
     * and parameter values are case-insensitive. Tokens are always
     * case-insensitive. Unless specified otherwise, values expressed as quoted
     * strings are case-sensitive.
     *
     * @param obj the object to compare this Header with.
     * @return <code>true</code> if <code>obj</code> is an instance of this class
     * representing the same SIP Header as this, <code>false</code> otherwise.
     */
    public boolean equals(Object obj);

    /**
     * Creates and returns a deep copy of the Header. This methods must ensure a
     * deep copy of the Header, so that when a message is cloned the Header can
     * be modified without effecting the original Header in the message. This
     * provides useful functionality for proxying Requests and Responses, for
     * example:
     * <ul>
     * <li>Recieve a message.
     * <li>Create a deep clone of the message and headers.
     * <li>Modify necessary headers.
     * <li>Proxy the message using the send methods on the SipProvider.
     * </ul>
     * This method overrides the clone method in java.lang.Object.
     *
     * @return a deep copy of Header
     */
    public Object clone();
    
    /**
     * Gets a integer hashcode representation of the Header. This method 
     * overrides the hashcode method in java.lang.Object.
     *
     * @return integer representation of Header hashcode
     * @since v1.2
     */
    public int hashCode();
    
    /**
     * Gets a string representation of the Header. This method overrides the
     * toString method in java.lang.Object.
     *
     * @return string representation of Header
     */
    public String toString();

}

