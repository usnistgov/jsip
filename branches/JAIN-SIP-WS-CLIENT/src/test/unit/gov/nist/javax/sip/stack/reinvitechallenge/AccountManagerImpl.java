package test.unit.gov.nist.javax.sip.stack.reinvitechallenge;

import javax.sip.ClientTransaction;



import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;

public class AccountManagerImpl implements AccountManager {

	
	public UserCredentials getCredentials(
			ClientTransaction challengedTransaction, String realm) {
			return new UserCredentialsImpl();
	}

}
