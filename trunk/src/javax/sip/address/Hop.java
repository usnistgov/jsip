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
 * File Name     : Hop.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     19/12/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.address;


/**
 * The Hop interface defines a location a request can transit on the way to 
 * its destination, i.e. a route. It defines the host, port and transport of 
 * the location. This interface is used to identify locations in the 
 * {@link Router} interface.
 *
 * @see Router
 *
 * @author Sun Microsystems
 * @since 1.1
 *
 */

public interface Hop {

     /**
     * Returns the host part of this Hop.
     *
     * @return  the string value of the host.
     */     
    public String getHost();

    /**
     * Returns the port part of this Hop.
     *
     * @return  the integer value of the port.
     */    
    public int getPort();    

    /**
     * Returns the transport part of this Hop.
     *
     * @return the string value of the transport.
     */
    public String getTransport();

    /**
     * This method returns the Hop as a string. 
     *
     * @return the stringified version of the Hop
     */    
    public String toString();
    
}

