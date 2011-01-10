/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
*
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*
* .
*
*/
package gov.nist.javax.sip.address;

import gov.nist.javax.sip.parser.*;

import java.text.ParseException;
import java.util.regex.Pattern;

import javax.sip.address.*;

/**
 * Implementation of the JAIN-SIP address factory.
 * @version 1.2 $Revision: 1.11 $ $Date: 2010-07-27 10:55:29 $
 *
 * @author M. Ranganathan   <br/>
 *
 *
 *
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/>
 * Network Research Team (http://www-r2.u-strasbg.fr))<br/>
 * Louis Pasteur University - Strasbourg - France<br/>
 *
 */
public class AddressFactoryImpl implements AddressFactoryEx {
    public static final Pattern SCHEME_PATTERN = Pattern.compile("\\p{Alpha}[[{\\p{Alpha}][\\p{Digit}][\\+][-][\\.]]*");
    
    /** Creates a new instance of AddressFactoryImpl
     */
    public AddressFactoryImpl() {
    }


    /**
     *
     *Create an empty address object.
     *
     *SPEC_REVISION
     */

    public javax.sip.address.Address createAddress() {
        return new AddressImpl();
    }
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
    public javax.sip.address.Address createAddress(
        String displayName,
        javax.sip.address.URI uri) {
        if (uri == null)
            throw new NullPointerException("null  URI");
        AddressImpl addressImpl = new AddressImpl();
        if (displayName != null)
            addressImpl.setDisplayName(displayName);
        addressImpl.setURI(uri);
        return addressImpl;

    }


    public javax.sip.address.Address createAddress(javax.sip.address.URI uri) {
        if (uri == null)
            throw new NullPointerException("null address");
        AddressImpl addressImpl = new AddressImpl();
        addressImpl.setURI(uri);
        return addressImpl;
    }

    /**
     * Creates an Address with the new address string value. The address
     * string is parsed in order to create the new Address instance. Create
     * with a String value of "*" creates a wildcard address. The wildcard
     * can be determined if
     * <code>((SipURI)Address.getURI).getUser() == *;</code>.
     *
     * @param address - the new string value of the address.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the address value.
     */
    public javax.sip.address.Address createAddress(String address)
        throws java.text.ParseException {
        if (address == null)
            throw new NullPointerException("null address");

        if (address.equals("*")) {
            AddressImpl addressImpl = new AddressImpl();
            addressImpl.setAddressType(AddressImpl.WILD_CARD);
            SipURI uri = new SipUri();
            uri.setUser("*");
            addressImpl.setURI( uri );
            return addressImpl;
        } else {
            StringMsgParser smp = new StringMsgParser();
            return smp.parseAddress(address);
        }
    }


    /** create a sip uri.
     *
     *@param uri -- the uri to parse.
     */
    public javax.sip.address.SipURI createSipURI(String uri) throws ParseException {
        if (uri == null)
            throw new NullPointerException("null URI");
        try {
            StringMsgParser smp = new StringMsgParser();
            SipUri sipUri = smp.parseSIPUrl(uri);
            return (SipURI) sipUri;
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }

    }

    /** Create a SipURI
     *
     *@param user -- the user
     *@param host -- the host.
     */
    public javax.sip.address.SipURI createSipURI(String user, String host) throws ParseException {
        if (host == null)
            throw new NullPointerException("null host");

        StringBuilder uriString = new StringBuilder("sip:");
        if (user != null) {
            uriString.append(user);
            uriString.append("@");
        }

        //if host is an IPv6 string we should enclose it in sq brackets
        if (host.indexOf(':') != host.lastIndexOf(':')
            && host.trim().charAt(0) != '[')
            host = '[' + host + ']';

        uriString.append(host);

       
        try {

           return this.createSipURI(uriString.toString());
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }

    /**
     * Creates a TelURL based on given URI string. The  '+' should
     * not be included in the phoneNumber string argument. If scheme is not present, it will be added.
     *
     * @param uri - the new string value of the phoneNumber.
     * @throws URISyntaxException if the URI string is malformed.
     */
    public javax.sip.address.TelURL createTelURL(String uri)
        throws ParseException {
        if (uri == null)
            throw new NullPointerException("null url");
        String telUrl = null;
        if(uri.startsWith("tel:"))
        {
        	telUrl = uri;
        }else
        {
        	telUrl = "tel:" + uri;
        }
        try {
            StringMsgParser smp = new StringMsgParser();
            TelURLImpl timp = (TelURLImpl) smp.parseUrl(telUrl);
            return (TelURL) timp;
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }
    
    
    /**
     * Creates a URI based on given URI string. The URI string is parsed in
     * order to create the new URI instance. Depending on the scheme the
     * returned may or may not be a SipURI or TelURL cast as a URI.
     *
     * @param uri - the new string value of the URI.
     * @throws URISyntaxException if the URI string is malformed.
     */

    public javax.sip.address.URI createURI(String uri) throws ParseException {
        if (uri == null)
            throw new NullPointerException("null arg");
        try {
            URLParser urlParser = new URLParser(uri);
            String scheme = urlParser.peekScheme();
            if (scheme == null)
                throw new ParseException("bad scheme", 0);
            if (scheme.equalsIgnoreCase("sip") || scheme.equalsIgnoreCase("sips")) {
               return this.createSipURI(uri);
            } else if (scheme.equalsIgnoreCase("tel")) {
                return this.createTelURL(uri);
            }
            // Issue 316 : the scheme should match ALPHA *(ALPHA / DIGIT / "+" / "-" / "." )
            if(!SCHEME_PATTERN.matcher(scheme).matches()) {
                throw new ParseException("the scheme " + scheme + " from the following uri " + uri + " doesn't match ALPHA *(ALPHA / DIGIT / \"+\" / \"-\" / \".\" ) from RFC3261",0);
            }
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }        
        return new gov.nist.javax.sip.address.GenericURI(uri);
    }

}
