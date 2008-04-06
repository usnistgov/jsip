package gov.nist.javax.sip.clientauthutils;

public interface AccountManager {
	
	/**
	 * Returns the user credentials for a given SIP Domain.
	 * You can implement any desired method (such as popping up a dialog for example ) 
	 * to retrieve the credentials.
	 * 
	 * @param domain
	 * 
	 * @return -- the user credentials associated with the domain. 
	 */

	UserCredentials getCredentials(String domain);

}
