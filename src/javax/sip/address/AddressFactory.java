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
 * File Name     : AddressFactory.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.address;

import java.text.ParseException;

/**
 * This interface provides factory methods that allow an application to create 
 * Address objects, URI's, SipURI's and TelURL's from a particular 
 * implementation of this specification. This class is a singleton and can be 
 * retrieved from the {@link javax.sip.SipFactory#createAddressFactory()}.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface AddressFactory {

    /**
     * Creates a URI based on given URI string. The URI string is parsed in 
     * order to create the new URI instance. Depending on the scheme the 
     * returned may or may not be a SipURI or TelURL cast as a URI.
     *
     * @param uri - the new string value of the URI.
     * @throws ParseException if the URI string is malformed.
     */
    public URI createURI(String uri) throws ParseException;

    /**
     * Creates a SipURI based on the given user and host components. The user
     * component may be null. 
     * <p>
     * This create method first builds a URI in string form using the given 
     * components as follows:
     * <ul>
     * <li>Initially, the result string is empty.
     * <li>The scheme followed by a colon character ('sip:') is appended to 
     * the result.
     * <li>The user and host are then appended. Any character that is not a 
     * legal URI character is quoted.
     * </ul>
     * <br>
     * The resulting URI string is then parsed in order to create the new 
     * SipURI instance as if by invoking the createURI(String) constructor; 
     * this may cause a URISyntaxException to be thrown.
     * <p>
     * An application that wishes to create a 'sips' URI should call the 
     * {@link SipURI#setSecure(boolean)} with an argument of 'true' on the 
     * returned SipURI. 
     *
     * @param user - the new string value of the user, this value may be null.
     * @param host - the new string value of the host.
     * @throws ParseException if the URI string is malformed. 
     */
    public SipURI createSipURI(String user, String host) 
                                throws ParseException;    

    /**
     * Creates a TelURL based on given URI string. The scheme should 
     * not be included in the phoneNumber string argument.
     *
     * @param phoneNumber the new string value of the phoneNumber.
     * @throws ParseException if the URI string is malformed. 
     */
    public TelURL createTelURL(String phoneNumber) throws ParseException;
    
    /**
     * Creates an Address with the new address string value. The address 
     * string is parsed in order to create the new Address instance. 
     * Valid arguments obey the syntax for <code>name-addr</code> tokens in 
     * RFC3261, for example "Bob <sip:bob@biloxi.com>".  It is recommended to 
     * use the to use the name-addr form containing '<' '>' to avoid confusion 
     * of URI parameters. As a special case, the 
     * string argument "*" creates a wildcard Address object with the property 
     * that <code>((SipURI)Address.getURI()).getUser()</code> returns a 
     * String contain one character "*".
     *
     * @param address - the new string value of the address.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the address value. 
     */
    public Address createAddress(String address) throws ParseException;

    /**
     * Creates an Address with the new URI attribute value. 
     *
     * @param uri - the URI value of the address.
     */
    public Address createAddress(URI uri);
    
    /**
     * Creates an Address with the new display name and URI attribute
     * values.
     *
     * @param displayName - the new string value of the display name of the
     * address. A <code>null</code> value does not set the display name.
     * @param uri - the new URI value of the address.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the displayName value. 
     */
    public Address createAddress(String displayName, URI uri) throws ParseException;


    
    
}

