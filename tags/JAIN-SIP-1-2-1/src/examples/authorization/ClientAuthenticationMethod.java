/*
 * ClientAuthenticationMethod.java
 *
 * Created on January 8, 2003, 9:49 AM
 */

package examples.authorization;

/**
 * Get this interface from the nist-sip IM
 * @author  olivier deruelle
 */
public interface ClientAuthenticationMethod {
    
    /**
     * Initialize the Client authentication method. This has to be
     * done outside the constructor.
     * @throws Exception if the parameters are not correct.
     */
    public void initialize(String realm,String userName,String uri,String nonce
    ,String password,String method,String cnonce,String algorithm) throws Exception;
    
    
    /**
     * generate the response
     * @returns null if the parameters given in the initialization are not
     * correct.
     */
    public String generateResponse();
    
}
