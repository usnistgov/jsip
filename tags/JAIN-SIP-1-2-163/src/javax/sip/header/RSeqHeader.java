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
 * File Name     : RSeqHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version, optional header to 
 *                                          support RFC3262.
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.InvalidArgumentException;

/**
 * This interface represents the RSeq header, as defined by 
 * <a href = "http://www.ietf.org/rfc/rfc3262.txt">RFC3262</a>, this 
 * header is not part of RFC3261.
 * <p> 
 * The RSeq header is used in provisional responses in order to transmit them 
 * reliably. It contains a single numeric value from 1 to 2**32-1. The value of 
 * the RSeq in each subsequent reliable provisional response for the same request 
 * MUST be greater by exactly one. RSeq numbers MUST NOT wrap around. Because 
 * the initial one is chosen to be less than 2**31 - 1, but the maximum is 
 * 2**32 - 1, there can be up to 2**31 reliable provisional responses per 
 * request, which is more than sufficient. As the RSeq numbering space is within 
 * a single transaction. This means that provisional responses for different 
 * requests MAY use the same values for the RSeq number.
 * <p>
 * A server must ignore Headers that it does not understand. A proxy must not 
 * remove or modify Headers that it does not understand.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface RSeqHeader extends Header {
  
    /**
     * Sets the sequence number value of the RSeqHeader of the provisional 
     * response. The sequence number MUST be expressible as a 32-bit unsigned 
     * integer and MUST be less than 2**31.
     *
     * @param sequenceNumber - the new Sequence number of this RSeqHeader
     * @throws InvalidArgumentException if supplied value is less than zero.
     */
    public void setSequenceNumber(int sequenceNumber) throws InvalidArgumentException;

    /**
     * Gets the sequence number of this RSeqHeader.
     *
     * @return the integer value of the Sequence number of the RSeqHeader
     */
    public int getSequenceNumber();    
    
    /**
     * Name of RSeqHeader
     */
    public final static String NAME = "RSeq";

}

