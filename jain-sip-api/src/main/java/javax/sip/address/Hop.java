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
 * @author BEA Systems, NIST
 * @version 1.2
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

