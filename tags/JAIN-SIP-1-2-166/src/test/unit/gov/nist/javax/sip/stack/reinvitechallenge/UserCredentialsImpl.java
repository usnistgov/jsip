package test.unit.gov.nist.javax.sip.stack.reinvitechallenge;

import gov.nist.javax.sip.clientauthutils.UserCredentials;

public class UserCredentialsImpl implements UserCredentials {
	
	public String getPassword() {
		return "password";
	}

	
	public String getSipDomain() {
		return "127.0.0.1";
	}

	
	public String getUserName() {		
		return "user";
	}

}
