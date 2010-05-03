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
 * File Name     : OrganizationHeader.java
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

 * The OrganizationHeader conveys the name of the organization to which the

 * entity issuing the Request or Response belongs. It may be used by client

 * software to filter calls.

 *

 * @author BEA Systems, NIST
 * @version 1.2

 */



public interface OrganizationHeader extends Header {



    /**

     * Sets the organization value of the OrganizationHeader to the

     * organization parameter supplied.

     *

     * @param organization - the new string organization value

     * @throws ParseException which signals that an error has been reached

     * unexpectedly while parsing the organization value.

     */

    public void setOrganization(String organization) throws ParseException;



    /**

     * Gets the organization value of OrganizationHeader.

     *

     * @return organization of OrganizationHeader

     */

    public String getOrganization();





    /**

     * Name of OrganizationHeader

     */

    public final static String NAME = "Organization";



}

