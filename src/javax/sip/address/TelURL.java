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
 * File Name     : TelURL.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *  1.2     19/05/2005  Phelim O'Doherty    Added phone context methods
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.address;

import java.text.ParseException;
import javax.sip.header.Parameters;

 /**
 * This class represents Tel URLs, which are used for addressing. The Tel URL
 * starts with the scheme <code>tel:</code>. This tells the
 * local entity that what follows is a URL that should be parsed as described
 * in <a href = "http://www.ietf.org/rfc/rfc2806.txt">RFC2806</a>. After that,
 * the URL contains the phone number of the remote entity.
 * <p>
 * Within a SIP Message, TelURLs can be used to indicate the source and intended
 * destination of a Request, redirection addresses and the current destination
 * of a Request. All these Headers may contain TelURLs.
 * <p>
 * The TelURL interface extends the generic URI interface and provides
 * additional convenience methods for the following components of a TelURL
 * address, above the generic URI class:
 * <ul>
 * <li>ISDN Subaddress - Phone numbers can also contain subaddresses, which
 * are used to identify different remote entities under the same phone number.
 * <li>Post Dial - Phone numbers can also contain a post-dial sequence.
 * This is what is often used with voice mailboxes and other services that
 * are controlled by dialing numbers from your phone keypad while the call is
 * in progress.
 * <li>Global - Phone numbers can be either "global" or "local". Global numbers
 * are unambiguous everywhere. Local numbers are usable only within a certain
 * area.
 * <li>URL parameters - Parameters affecting a request constructed from this
 * URL. URL parameters are added to the end of the URL component and are 
 * separated by semi-colons. URL parameters take the form:<br>
 * parameter-name "=" parameter-value
 * </ul>
 * See <a href = "http://www.ietf.org/rfc/rfc2806.txt">RFC2806</a> for more 
 * information on the use of TelURL's.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface TelURL extends URI, Parameters {


    /**
     * Returns <code>true</code> if this TelURL is global i.e. if the TelURI
     * has a global phone user.
     *
     * @return <code>true</code> if this TelURL represents a global phone user,
     * and <code>false</code> otherwise.
     */
    public boolean isGlobal();

    /**
     * Sets phone user of this TelURL to be either global or local. The default
     * value is false, hence the TelURL is defaulted to local.
     *
     * @param global - the boolean value indicating if the TelURL has a global
     * phone user.
     */
    public void setGlobal(boolean global);

    /**
     * Sets post dial of this TelURL. The post-dial sequence describes what and
     * when the local entity should send to the phone line.
     *
     * @param postDial - new value of the <code>postDial</code> parameter
     * @throws ParseException  which signals that an error has been reached
     * unexpectedly while parsing the postDial value.
     */
    public void setPostDial(String postDial) throws ParseException;

    /**
     * Returns the value of the <code>postDial</code> parameter, or null if it
     * is not set.
     *
     * @return  the value of the <code>postDial</code> parameter
     */
    public String getPostDial();

    /**
     * Sets phone number of this TelURL. The phone number may either be local or
     * global determined by the isGlobal method in this interface. The phoneNumber
     * argument should not contain the "+" associated with telephone numbers.
     *
     * @param phoneNumber - new value of the <code>phoneNumber</code> parameter
     * @throws ParseException  which signals that an error has been reached
     * unexpectedly while parsing the phoneNumber value.
     */
    public void setPhoneNumber(String phoneNumber) throws ParseException;

    /**
     * Returns the value of the <code>phoneNumber</code> parameter. This method
     * will not return the "+" associated with telephone numbers.
     *
     * @return  the value of the <code>phoneNumber</code> parameter
     */
    public String getPhoneNumber();    
    
    /**
     * Sets ISDN subaddress of this TelURL. If a subaddress is present, it is
     * appended to the phone number after ";isub=".
     *
     * @param isdnSubAddress - new value of the <code>isdnSubAddress</code>
     * parameter
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the isdnSubAddress value.
     */
    public void setIsdnSubAddress(String isdnSubAddress) throws ParseException;

    /**
     * Returns the value of the <code>isdnSubAddress</code> parameter, or null
     * if it is not set.
     *
     * @return  the value of the <code>isdnSubAddress</code> parameter
     */
    public String getIsdnSubAddress();
    
    /**
     * Sets the phone context of this TelURL.
     *
     * @param phoneContext - new value of the <code>phoneContext</code>
     * parameter
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the phoneContext value.
     * @since v1.2
     */
    public void setPhoneContext(String phoneContext) throws ParseException;

    /**
     * Returns the value of the <code>phoneContext</code> parameter, or null
     * if it is not set.
     *
     * @return  the value of the <code>phoneContext</code> parameter
     * @since v1.2
     */
    public String getPhoneContext();
        
    
    /**
     * This method returns the URI as a string. 
     *
     * @return String The stringified version of the URI
     */    
    public String toString();
}
