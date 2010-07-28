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
* of the terms of this agreement.
*
*/
package gov.nist.javax.sip.header;

import javax.sip.header.Parameters;

/**
 * Extensions to the {@link Parameters} interface supported by the implementation and 
 * will be included in the next spec release.
 * 
 * @author jean.deruelle@gmail.com
 * @ since 2.0
 */
public interface ParametersExt extends Parameters {
    /**
     * Returns the value of the named parameter, or null if it is not set. A
     * zero-length String indicates flag parameter.
     *
     * The stack remove the undeeded quotes that are imposed by SIP encoding rules to ensure unambiguous parsing.<br/>
     * The stripQuotes parameter can be used to get the original value as it has been received by the stack ie with the quotes 
     *
     * @param name name of parameter to retrieve
     * @param stripQuotes will return the value of the parameter as it has been received when the message came into the stack
     * @return the value of specified parameter
     * @since 2.0
     */
    public String getParameter(String name, boolean stripQuotes);

}
