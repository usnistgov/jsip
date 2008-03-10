package gov.nist.javax.sip.clientauthutils;

import java.util.*;

import javax.sip.*;
import javax.sip.header.*;
import javax.sip.address.*;
import javax.sip.message.*;

/**
 * A cache of authorization headers to be used for subsequent processing when we set up calls.
 * We cache credentials on a per proxy domain per user basis.
 * 
 */

class CredentialsCache
{
   
    /**
     * The key for this map is the proxy domain name.
     * A given proxy authorizes a user for a number of domains.
     * The Hashtable value of the mapping is a mapping of user names
     * to AuthorizationHeader list for that proxy domain.
     */
    private Hashtable<String,Hashtable<String,List<AuthorizationHeader>>> authenticatedCalls =  new Hashtable<String,Hashtable<String,List<AuthorizationHeader>>>();

   
    /**
     * Cache the bindings of call-id and authorization header.
     *
     * @param callid the id of the call that the <tt>authorization</tt> header
     * belongs to.
     * @param authorization the authorization header that we'd like to cache.
     */
    void cacheAuthorizationHeader(String proxyDomain, 
                                  AuthorizationHeader authorization)
    {
    	String user  = authorization.getUsername();
    	assert proxyDomain != null;
    	
    	Hashtable<String,List<AuthorizationHeader>> authHeaders = authenticatedCalls.get(proxyDomain);
    	if ( authHeaders == null) {
    		authHeaders = new Hashtable<String,List<AuthorizationHeader>>();
    		authenticatedCalls.put(proxyDomain, authHeaders);
    	}
    	
    	// BUGBUG -- this is not quite correct. Should differentiate this on a per callId basis but leads
    	// to lots of garbage collection headaches!
    	LinkedList<AuthorizationHeader> alist = new LinkedList<AuthorizationHeader>();
    	authHeaders.put(user, alist);
    	
    	alist.add(authorization);
    }

    /**
     * Returns an authorization header cached for the specified call id and null
     * if no authorization header has been previously cached for this call.
     *
     * @param callid the call id that we'd like to retrive a cached
     * authorization header for.
     *
     * @return authorization header corresponding to that user for the given proxy domain.
     */
    Collection<AuthorizationHeader> getCachedAuthorizationHeaders(String proxyDomain, String user)
    {
    	if ( user == null) throw new NullPointerException("Null arg!");
        Hashtable<String,List<AuthorizationHeader>> authHeaderMap = authenticatedCalls.get(proxyDomain);
        if ( authHeaderMap == null	) return null;
        else {
        	return authHeaderMap.get(user);
        }
        
    }

}


