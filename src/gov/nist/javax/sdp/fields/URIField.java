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
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;

import gov.nist.core.*;
import java.net.*;

/** Implementation of URI field.
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*/

public class URIField extends SDPField implements javax.sdp.URI {

    private static final long serialVersionUID = -4322063343955734258L;
    protected URL url;
    protected String urlString;

    public URIField() {
        super(URI_FIELD);
    }

    public String getURI() {
        return urlString;
    }

    public void setURI(String uri) {
        this.urlString = uri;
        this.url = null;
    }

    public URL get() {
        if (this.url != null) {
            return this.url;
        } else {
            try {
                this.url = new URL(this.urlString);
                return this.url;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public void set(URL uri) {
        this.url = uri;
        this.urlString = null;
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        if (urlString != null) {
            return URI_FIELD + urlString + Separators.NEWLINE;
        } else if (url != null) {
            return URI_FIELD + url.toString() + Separators.NEWLINE;
        } else
            return "";
    }

}
