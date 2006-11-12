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
 * File Name     : PriorityHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;



import java.text.ParseException;



/**

 * The Priority header field indicates the urgency of the request as perceived

 * by the client.  The Priority header field describes the priority that the

 * SIP request should have to the receiving human or its agent. For example,

 * it may be factored into decisions about call routing and acceptance. For

 * these decisions, a message containing no Priority header field SHOULD be

 * treated as if it specified a Priority of <var>"Normal"</var>.

 * <p>

 * The Priority header field does not influence the use of communications

 * resources such as packet forwarding priority in routers or access to

 * circuits in PSTN gateways.

 * <p>

 * The currently defined priority values are:

 * <ul>

 * <li> EMERGENCY

 * <li> URGENT

 * <li> NORMAL

 * <li> NON_URGENT

 * </ul>

 * For Example:<br>

 * <code>Subject: Weekend plans<br>

 * Priority: non-urgent</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2

 */



public interface PriorityHeader extends Header {



    /**

     * Set priority of PriorityHeader

     *

     * @param priority - the new string priority value

     * @throws ParseException which signals that an error has been reached

     * unexpectedly while parsing the priority value.

     */

    public void setPriority(String priority) throws ParseException;



    /**

     * Gets the string priority value of the PriorityHeader.

     *

     * @return the string priority value of the PriorityHeader

     */

    public String getPriority();





    /**

     * Urgent priority constant

     */

    public static final String URGENT = "Urgent";



    /**

     * Normal priority constant

     */

    public static final String NORMAL = "Normal";



    /**

     * Non-urgent priority constant

     */

    public static final String NON_URGENT = "Non-Urgent";



    /**

     * Emergency priority constant - It is RECOMMENDED that the value of

     * "emergency" only be used when life, limb, or property are in imminent

     * danger. Otherwise, there are no semantics defined for this header

     * field.

     */

    public static final String EMERGENCY = "Emergency";





    /**

     * Name of PriorityHeader

     */

    public final static String NAME = "Priority";



}

