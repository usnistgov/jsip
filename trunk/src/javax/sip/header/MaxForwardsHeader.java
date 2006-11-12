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
 * File Name     : MaxForwardsHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;


import javax.sip.InvalidArgumentException;



/**

 * The Max-Forwards header field must be used with any SIP method to limit

 * the number of proxies or gateways that can forward the request to the next

 * downstream server.  This can also be useful when the client is attempting

 * to trace a request chain that appears to be failing or looping in mid-chain.

 * <p>

 * The Max-Forwards value is an integer in the range 0-255 indicating the

 * remaining number of times this request message is allowed to be forwarded.

 * This count is decremented by each server that forwards the request. The

 * recommended initial value is 70.

 * <p>

 * This header field should be inserted by elements that can not otherwise

 * guarantee loop detection.  For example, a B2BUA should insert a Max-Forwards

 * header field.

 * <p>

 * For Example:<br>

 * <code>Max-Forwards: 6</code>

 *
 * @author BEA Systems, NIST
 * @version 1.2

 */



public interface MaxForwardsHeader extends Header {



    /**

     * This convenience function decrements the number of max-forwards by one.

     * This utility is useful for proxy functionality.

     *

     * @throws TooManyHopsException if implementation cannot decrement

     * max-fowards i.e. max-forwards has reached zero

     */

    public void decrementMaxForwards() throws TooManyHopsException;



    /**

     * Gets the maximum number of forwards value of this MaxForwardsHeader.

     *

     * @return the maximum number of forwards of this MaxForwardsHeader

     */

    public int getMaxForwards();



    /**

     * Sets the max-forwards argument of this MaxForwardsHeader to the supplied

     * <var>maxForwards</var> value.

     *

     * @param maxForwards - the number of max-forwards

     * @throws InvalidArgumentException if the maxForwards argument is less

     * than 0 or greater than 255.

     */

    public void setMaxForwards(int maxForwards) throws InvalidArgumentException;


    /**
     * Compare this MaxForwardsHeader for equality with another. This method 
     * overrides the equals method in javax.sip.Header. This method specifies 
     * object equality as outlined by  
     * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>. 
     * Two MaxForwards header fields are equivalent if their max forwards 
     * integer match. 
     *
     * @param obj the object to compare this MaxForwardsHeader with.
     * @return <code>true</code> if <code>obj</code> is an instance of this class
     * representing the same MaxForwardsHeader as this, <code>false</code> otherwise.
     * @since v1.2
     */
    public boolean equals(Object obj);  


    /**

     * Name of MaxForwardsHeader

     */

    public final static String NAME = "Max-Forwards";

}

