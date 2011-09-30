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
 * File Name     : ContentLanguageHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.util.Locale;

/**
 * The Content-Language header field is used to indicate the language of the
 * message body.
 * <p>
 * For Example:<br>
 * <code>Content-Language: fr</code>
 *
 * @see ContentDispositionHeader
 * @see ContentLengthHeader
 * @see ContentEncodingHeader
 * @see ContentTypeHeader
 *
 * @author BEA Systems, NIST 
 * @version 1.2
 *
 */
public interface ContentLanguageHeader extends Header{

    /**
     * Gets the language value of the ContentLanguageHeader.
     *
     * @return the Locale value of this ContentLanguageHeader
     */
    public Locale getContentLanguage();

    /**
     * Sets the language parameter of this ContentLanguageHeader.
     *
     * @param language - the new Locale value of the language of
     * ContentLanguageHeader
     */
    public void setContentLanguage(Locale language);

    /**
     * Name of ContentLanguageHeader
     */
    public final static String NAME = "Content-Language";

}

