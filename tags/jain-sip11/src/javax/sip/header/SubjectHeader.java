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
 * File Name     : SubjectHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;



import java.text.ParseException;



/**

 * The Subject header field provides a summary or indicates the nature of the

 * call, allowing call filtering without having to parse the session

 * description.  The session description does not have to use the same subject

 * indication as the invitation.

 * <p>

 * For Example:<br>

 * <code>Subject: Where is the Moscone?</code>

 *

 * @version 1.1

 * @author Sun Microsystems

 */



public interface SubjectHeader extends Header {



    /**

     * Sets the subject value of the SubjectHeader to the supplied string

     * subject value.

     *

     * @param subject - the new subject value of this header.

     * @throws ParseException which signals that an error has been reached

     * unexpectedly while parsing the subject value.

     */

    public void setSubject(String subject) throws ParseException;



    /**

     * Gets the subject value of SubjectHeader.

     *

     * @return subject of SubjectHeader.

     */

    public String getSubject();





    /**

     * Name of SubjectHeader

     */

    public final static String NAME = "Subject";

}

