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
 * File Name     : Address.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.address;

import java.io.*;
import java.text.ParseException;

/**
 * This interface represents a user's display name and URI address. The display
 * name of an address is optional but if included can be displayed to an end-user. 
 * The address URI (most likely a SipURI) is the user's address. For example a 
 * 'To' address of <code>To: Bob sip:duke@jcp.org</code> would have a display 
 * name attribute of <code>Bob</code> and an address of 
 * <code>sip:duke@jcp.org</code>.
 * 
 * @see SipURI
 * @see TelURL
 *
 * @version 1.1
 * @author Sun Microsystems
 *
 */

public interface Address extends Cloneable, Serializable{

    /**
     * Sets the display name of the Address. The display name is an
     * additional user friendly personalized text that accompanies the address.
     *
     * @param displayName - the new string value of the display name.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the displayName value.
     */
    public void setDisplayName(String displayName) throws ParseException;

    /**
     * Gets the display name of this Address, or null if the attribute is
     * not set.
     *
     * @return the display name of this Address
     */
    public String getDisplayName();

    /**
     * Sets the URI of this Address. The URI can be either a TelURL or a SipURI.
     *
     * @param uri - the new URI value of this Address.
     */
    public void setURI(URI uri);

    /**
     * Returns the URI  of this Address. The type of URI can be
     * determined by the scheme.
     *
     * @return URI parmater of the Address object
     */
    public URI getURI();


    /**
     * Returns a string representation of this Address.
     *
     * @return the stringified representation of the Address
     */
    public String toString();

    /**
     * Indicates whether some other Object is "equal to" this Address.
     * The actual implementation class of a Address object must override
     * the Object.equals method. The new equals method must ensure that the
     * implementation of the method is reflexive, symmetric, transitive and
     * for any non null value X, X.equals(null) returns false.
     *
     * @param obj - the Object with which to compare this Address
     * @return true if this Address is "equal to" the object argument and
     * false otherwise.
     * @see Object
     */
    public boolean equals(Object obj);

    /**
     * This determines if this address is a wildcard address. That is
     * <code>((SipURI)Address.getURI()).getUser() == *;</code>. This method 
     * is specific to SIP and SIPS schemes.
     *
     * @return true if this address is a wildcard, false otherwise.
     */
    public boolean isWildcard();

}

