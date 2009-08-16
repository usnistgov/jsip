package gov.nist.javax.sip.clientauthutils;

/**
 * Interface for those clients that only supply 
 * hash(user:domain:password). This is more secure than simply supplying
 * password because the password cannot be extracted.
 * 
 */
public interface UserCredentialHash {
    
    /**
     * Get the user name.
     * 
     * @return userName
     */
    public String getUserName();
    
    
    /**
     * Get the SipDomain.
     * 
     * @return
     */
    public String getSipDomain();
    
    
    /**
     * Get the MD5(userName:sipdomain:password)
     * 
     * @return the MD5 hash of userName:sipDomain:password.
     */
    public String getHashUserDomainPassword();

}
