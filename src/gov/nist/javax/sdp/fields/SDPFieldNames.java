/*****************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      * 
******************************************************************************/
package gov.nist.javax.sdp.fields;

/**
*Field names for SDP Fields.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
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
	public static final String REPEAT_FIELD="r=";
	public static final String ZONE_FIELD= "z=";
}
