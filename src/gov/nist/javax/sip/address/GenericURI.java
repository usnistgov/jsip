package gov.nist.javax.sip.address;
import  java.text.ParseException;

/** Implementation of the URI class. This relies on the 1.4 URI class.
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class GenericURI extends NetObject implements javax.sip.address.URI {
    public static final String SIP = ParameterNames.SIP_URI_SCHEME;
    public static final String SIPS = ParameterNames.SIPS_URI_SCHEME;
    public static final String TEL = ParameterNames.TEL_URI_SCHEME;
    public static final String POSTDIAL  =  ParameterNames.POSTDIAL;
    public static final String PHONE_CONTEXT_TAG 
				= ParameterNames.PHONE_CONTEXT_TAG;
    public static final String ISUB 	  = ParameterNames.ISUB;
    public static final String PROVIDER_TAG    = ParameterNames.PROVIDER_TAG;
 
    
    /** Imbedded URI
     */    
    protected String uriString;
    
    protected String scheme;
    
    /** Consturctor
     */    
    protected GenericURI() {}
    
    /** Constructor given the URI string
     * @param uriString The imbedded URI string.
     * @throws URISyntaxException When there is a syntaz error in the imbedded URI.
     */    
    public  GenericURI(String uriString) throws ParseException {
        try{
            this.uriString = uriString;
            int i=uriString.indexOf(":");
            scheme=uriString.substring(0,i);
        }
        catch(Exception e) {
            throw new ParseException("GenericURI, Bad URI format",0);
        }
    }
    
    /** Encode the URI.
     * @return The encoded URI
     */    
    public String encode() {
       return uriString;
       
    }
    
    /** Encode this URI.
     * @return The encoded URI
     */
    public String toString() {
        return this.encode(); 
     
    }
    
    /** Overrides the base clone method
     * @return The Cloned strucutre,
     */    
    public Object clone()  {
        try {
            return new GenericURI(this.uriString);
            
        }
        catch ( Exception ex){
        
            throw new RuntimeException(ex.getMessage() + this.uriString);
        }
    }
   
    /** Returns the value of the "scheme" of
     * this URI, for example "sip", "sips" or "tel".
     *
     * @return the scheme paramter of the URI
     */
    public String getScheme() {
       return scheme;
    }
    
    /** This method determines if this is a URI with a scheme of
     * "sip" or "sips".
     *
     * @return true if the scheme is "sip" or "sips", false otherwise.
     */
    public boolean isSipURI() {
        return this instanceof SipUri;
       
    }
    
}

