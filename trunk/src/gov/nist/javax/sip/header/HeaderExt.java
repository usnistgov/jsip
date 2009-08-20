/*
 * JBoss, Home of Professional Open Source
 * This code has been contributed to the public domain by the author.
 */
package gov.nist.javax.sip.header;

import javax.sip.header.Header;

/**
 * Extensions to the Header interface supported by the implementation and 
 * will be included in the next spec release.
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface HeaderExt extends  Header {

    /**
     * Gets the header value (i.e. what follows the name:) as a string
     * @return the header value (i.e. what follows the name:)
     * @since 2.0
     */
    public String getValue();
}
