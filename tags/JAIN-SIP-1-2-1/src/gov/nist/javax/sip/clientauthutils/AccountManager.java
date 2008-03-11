package gov.nist.javax.sip.clientauthutils;

public interface AccountManager {
	
	/**
	 * Returns the user credentials for a given SIP Domain.
	 * 
	 * @param domain
	 * 
	 * @return
	 */

	UserCredentials getCredentials(String domain);

}
