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
 * File Name     : ContentDispositionHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;

/**
 * The Content-Disposition header field describes how the message body or,
 * for multipart messages, a message body part is to be interpreted by the
 * UAC or UAS. This SIP header field extends the MIME Content-Type. Several
 * new "disposition-types" of the Content-Disposition header are defined by
 * SIP, namely:-
 * <ul>
 * <li>session - indicates that the body part describes a session, for either
 * calls or early (pre-call) media.
 * <li>render - indicates that the body part should be displayed or otherwise
 * rendered to the user.
 * <li>icon - indicates that the body part contains an image suitable as an
 * iconic representation of the caller or callee that could be rendered
 * informationally by a user agent when a message has been received, or
 * persistently while a dialog takes place.
 * <li>alert - indicates that the body part contains information, such as an
 * audio clip, that should be rendered by the user agent in an attempt to alert
 * the user to the receipt of a request, generally a request that initiates a
 * dialog.
 * </ul>
 * For backward-compatibility, if the Content-Disposition header field is
 * missing, the server SHOULD assume bodies of Content-Type application/sdp are
 * the disposition "session", while other content types are "render".
 * <p>
 * If this header field is missing, the MIME type determines the default
 * content disposition.  If there is none, "render" is assumed.
 * <p>
 * For Example:<br>
 * <code>Content-Disposition: session</code>
 *
 * @see ContentTypeHeader
 * @see ContentLengthHeader
 * @see ContentEncodingHeader
 * @see ContentLanguageHeader
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface ContentDispositionHeader extends Parameters, Header {


    /**
     * Sets the interpretation value of the message body or message body part
     * for this ContentDispositionHeader.
     *
     * @param dispositionType the new String value of the
     * disposition type.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the dispositionType parameter.
     */
    public void setDispositionType(String dispositionType) throws ParseException;

    /**
     * Gets the interpretation of the message body or message body part of
     * this ContentDispositionHeader.
     *
     * @return interpretation of the message body or message body part
     */
    public String getDispositionType();


    /**
     * The handling parameter describes how the UAS should react if it
     * receives a message body whose content type or disposition type it
     * does not understand.  The parameter has defined values of "optional"
     * and "required".  If the handling parameter is missing, the value
     * "required" SHOULD be assumed.
     *
     * @param handling the new String value either "optional"
     * or "required".
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the handling parameter.
     */
    public void setHandling(String handling) throws ParseException;

    /**
     * Gets the handling information of the unknown content disposition of the
     * ContentDispositionHeader.
     *
     * @return handling information for unknown content dispositions.
     */
    public String getHandling();

    /**
     * Name of ContentDispositionHeader
     */
    public final static String NAME = "Content-Disposition";

    /**
     * Session Disposition Type Constant
     */
    public final static String SESSION = "Session";    

    /**
     * Render Disposition Type Constant
     */
    public final static String RENDER = "Render";    
    
    /**
     * Icon Disposition Type Constant
     */
    public final static String ICON = "Icon";    
    
    /**
     * Alert Disposition Type Constant
     */
    public final static String ALERT = "Alert";        
    
}

