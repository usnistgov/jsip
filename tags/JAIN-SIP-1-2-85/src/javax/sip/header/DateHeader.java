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
 * File Name     : DateHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.util.*;

/**
 * The Date header field reflects the time when the request or response is
 * first sent. Retransmissions have the same Date header field value as the
 * original. The Date header field contains the date and time. Unlike
 * HTTP/1.1, SIP only supports the most recent 
 * <a href = "http://www.ietf.org/rfc/rfc1123.txt">RFC 1123</a> format for dates. 
 * SIP restricts the time zone in SIP-date to "GMT", while RFC 1123 allows any
 * time zone.
 * <p>
 * The Date header field can be used by simple end systems without a
 * battery-backed clock to acquire a notion of current time. However, in its
 * GMT form, it requires clients to know their offset from GMT.
 * <p>
 * Example:<br>
 * Date: Sat, 13 Nov 2010 23:29:00 GMT
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */



public interface DateHeader extends Header {



    /**

     * Sets date of DateHeader. The date is repesented by the Calendar object.

     *

     * @param date the Calendar object date of this header.

     */

    public void setDate(Calendar date);



    /**

     * Gets the date of DateHeader. The date is repesented by the Calender

     * object.

     *

     * @return the Calendar object representing the date of DateHeader

     */

    public Calendar getDate();



    /**

     * Name of DateHeader

     */

    public final static String NAME = "Date";



}

