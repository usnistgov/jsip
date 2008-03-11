/*
 * DigestClientAlgorithm.java
 *
 * Created on January 7, 2003, 10:45 AM
 */

package examples.authorization;


import java.security.*;

/**
 * Get this interface from the nist-sip IM
 * @author  olivier deruelle
 */
public class DigestClientAuthenticationMethod implements ClientAuthenticationMethod{
    
    private String realm;
    private String userName;
    private String uri;
    private String nonce;
    private String password;
    private String method;
    private String cnonce;
    private MessageDigest messageDigest;
    
    
     /**
     * to hex converter
     */
    private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
    '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    
    /**
     * convert an array of bytes to an hexadecimal string
     * @return a string
     * @param b bytes array to convert to a hexadecimal
     * string
     */
    
    public static String toHexString(byte b[]) {
        int pos = 0;
        char[] c = new char[b.length*2];
        for (int i=0; i< b.length; i++) {
            c[pos++] = toHex[(b[i] >> 4) & 0x0F];
            c[pos++] = toHex[b[i] & 0x0f];
        }
        return new String(c);
    }
    
    public void initialize(String realm, String userName, String uri, String nonce,
    String password, String method, String cnonce,String algorithm) throws Exception {
        if (realm==null) throw new Exception("The realm parameter is null");
            this.realm=realm;
        if (userName==null) throw new Exception("The userName parameter is null");
            this.userName=userName;
        if (uri==null) throw new Exception("The uri parameter is null");
            this.uri=uri;
        if (nonce==null) throw new Exception("The nonce parameter is null");
            this.nonce=nonce;
        if (password==null) throw new Exception("The password parameter is null");
            this.password=password;
        if (method==null) throw new Exception("The method parameter is null");
            this.method=method;
        this.cnonce=cnonce;
        if (algorithm==null) throw new Exception("The algorithm parameter is null");
            try {
            messageDigest = MessageDigest.getInstance(algorithm);
        }
        catch ( NoSuchAlgorithmException ex ) {
	    System.out.println("DEBUG, DigestClientAuthenticationMethod, initialize(): "+
            "ERROR: Digest algorithm does not exist.");
            throw new Exception("ERROR: Digest algorithm does not exist.");
	}
    }
    
     /** 
      * generate the response
      */
    public String generateResponse() {
	 if (userName == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): "+
            "ERROR: no userName parameter");
            return null;
         }
	 if (realm == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): "+
            "ERROR: no realm parameter");
            return  null; 
         }
               
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): "+
         "Trying to generate a response for the user: "+userName+" , with "+
         "the realm: "+ realm);
                
	 if (password == null)  {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): "+
            "ERROR: no password parameter");
		return null;
         }
         if (method == null)  {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): "+
            "ERROR: no method parameter");
		return null;
         }     
         if (uri== null)  {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): "+
            "ERROR: no uri parameter");
		return null;
         }     
         if (nonce== null)  {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): "+
            "ERROR: no nonce parameter");
		return null;
         }    
         if (messageDigest== null)  {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): "+
            "ERROR: the algorithm is not set");
		return null;
         }   
	
         
         /*******    GENERATE RESPONSE      ************************************/
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(), userName:"+userName+"!");
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(), realm:"+realm+"!");
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(), password:"+password+"!");
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(), uri:"+uri+"!");
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(), nonce:"+nonce+"!");
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(), method:"+method+"!");
         // A1
         String A1 = userName + ":" + realm+ ":" +   password ;
         byte mdbytes[] = messageDigest.digest(A1.getBytes());
         String HA1 = toHexString(mdbytes);
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(), HA1:"+HA1+"!");
         //A2
         String A2 = method.toUpperCase() + ":" + uri ;
         mdbytes = messageDigest.digest(A2.getBytes());
         String HA2 = toHexString(mdbytes);
         System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(), HA2:"+HA2+"!");
         //KD
         String KD = HA1 + ":" + nonce;
         if (cnonce != null) {
             if(cnonce.length()>0) KD += ":" + cnonce;
         }
         KD += ":" + HA2;
         mdbytes = messageDigest.digest(KD.getBytes());
         String response = toHexString(mdbytes);
        
         System.out.println("DEBUG, DigestClientAlgorithm, generateResponse():"+
         " response generated: "+response);
         
         return response;
    }
    
    
}
