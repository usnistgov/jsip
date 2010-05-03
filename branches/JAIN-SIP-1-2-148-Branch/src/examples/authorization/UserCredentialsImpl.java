package examples.authorization;

import gov.nist.javax.sip.clientauthutils.UserCredentials;

public class UserCredentialsImpl implements UserCredentials {
    private String userName;
    private String sipDomain;
    private String password;

    public UserCredentialsImpl(String userName, String sipDomain, String password) {
        this.userName = userName;
        this.sipDomain = sipDomain;
        this.password = password;
    }

  
    public String getPassword() {
        return password;
    }


    public String getSipDomain() {
       return sipDomain;
    }

   
    public String getUserName() {
       
        return userName;
    }

}
