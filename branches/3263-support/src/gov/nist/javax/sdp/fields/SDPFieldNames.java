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
/*****************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
******************************************************************************/
package gov.nist.javax.sdp.fields;

/**
*Field names for SDP Fields.
*
*@version 1.2
*
*@author M. Ranganathan   <br/>
*
*
*
*/

public interface SDPFieldNames {

    public static final String SESSION_NAME_FIELD = "s=";
    public static final String INFORMATION_FIELD = "i=";
    public static final String EMAIL_FIELD = "e=";
    public static final String PHONE_FIELD = "p=";
    public static final String CONNECTION_FIELD = "c=";
    public static final String BANDWIDTH_FIELD = "b=";
    public static final String ORIGIN_FIELD = "o=";
    public static final String TIME_FIELD = "t=";
    public static final String KEY_FIELD = "k=";
    public static final String ATTRIBUTE_FIELD = "a=";
    public static final String PROTO_VERSION_FIELD = "v=";
    public static final String URI_FIELD = "u=";
    public static final String MEDIA_FIELD = "m=";
    public static final String REPEAT_FIELD = "r=";
    public static final String ZONE_FIELD = "z=";
}
