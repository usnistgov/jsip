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
 * File Name     : RAckHeader.java
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
import java.text.ParseException;

/**
 * This interface represents the RAck header, as defined by 
 * <a href = "http://www.ietf.org/rfc/rfc3262.txt">RFC3262</a>, this 
 * header is not part of RFC3261.
 * <p>
 * The PRACK messages contain an RAck header field, which indicates the 
 * sequence number of the provisional response that is being acknowledged (each 
 * provisional response is given a sequence number, carried in the RSeq header 
 * field in the Provisional response). The acknowledgements are not cumulative, 
 * and the specifications recommend a single outstanding provisional response at 
 * a time, for purposes of congestion control.
 * <p> 
 * The RAck header contains two numbers and a method tag. The first number is 
 * the sequence number from the RSeqHeader in the provisional response that is 
 * being acknowledged. The next number is the sequence number that is copied 
 * from the CSeqHeader along with the method tag, from the response that is being 
 * acknowledged. 
 * <p>
 * For Example:<br>
 * <code>RAck: 776656 1 INVITE</code>
 * <p>
 * A server must ignore Headers that it does not understand. A proxy must not 
 * remove or modify Headers that it does not understand.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface RAckHeader extends Header {

    /**
     * Sets the method of RAckHeader, which correlates to the method of the 
     * CSeqHeader of the provisional response being acknowledged.
     *
     * @param method - the new string value of the method of the RAckHeader 
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method value.
     */
    public void setMethod(String method) throws ParseException;

    /**
     * Gets the method of RAckHeader.
     *
     * @return method of RAckHeader.
     */
    public String getMethod();

    /**
     * Sets the sequence number value of the CSeqHeader of the provisional 
     * response being acknowledged. The sequence number MUST be expressible as 
     * a 32-bit unsigned integer and MUST be less than 2**31.
     *
     * @param cSeqNumber - the new cSeq number of this RAckHeader.
     * @throws InvalidArgumentException if supplied value is less than zero.
     */
    public void setCSeqNumber(int cSeqNumber) throws InvalidArgumentException;

    /**
     * Gets the CSeq sequence number of this RAckHeader.
     *
     * @return the integer value of the cSeq number of the RAckHeader.
     */
    public int getCSeqNumber();
    

    /**
     * Sets the sequence number value of the RSeqHeader of the provisional 
     * response being acknowledged. The sequence number MUST be expressible as 
     * a 32-bit unsigned integer and MUST be less than 2**31.
     *
     * @param rSeqNumber - the new rSeq number of this RAckHeader.
     * @throws InvalidArgumentException if supplied value is less than zero.
     */
    public void setRSeqNumber(int rSeqNumber) throws InvalidArgumentException;

    /**
     * Gets the RSeq sequence number of this RAckHeader.
     *
     * @return the integer value of the RSeq number of the RAckHeader.
     */
    public int getRSeqNumber();    
    
    /**
     * Name of RAckHeader.
     */
    public final static String NAME = "RAck";

}

