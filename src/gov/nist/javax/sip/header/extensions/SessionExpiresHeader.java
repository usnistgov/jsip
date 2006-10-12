package gov.nist.javax.sip.header.extensions;

import javax.sip.header.*;

/*
 * Extension for SessionTimer RFC 4028
 * 
 * 
 */


public interface SessionExpiresHeader extends Parameters, Header {
   
	public final static String NAME = "Session-Expires";
	
}
