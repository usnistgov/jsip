/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and applicable 
 * provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. Sun, 
 * Sun Microsystems, the Sun logo, Java, Jini and JAIN are trademarks or 
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other 
 * countries.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JAIN SIP Specification
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

 * @version 1.1

 * @author Sun Microsystems

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

     * Name of MaxForwardsHeader

     */

    public final static String NAME = "Max-Forwards";

}

