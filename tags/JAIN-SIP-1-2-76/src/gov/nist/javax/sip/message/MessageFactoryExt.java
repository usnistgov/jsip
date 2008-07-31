package gov.nist.javax.sip.message;

import javax.sip.header.UserAgentHeader;

/**
 * Intefaces that will be supported by the next release of JAIN-SIP.
 * 
 * @author mranga
 *
 */
public interface MessageFactoryExt {
	/**
	 * Set the common UserAgent header for all Messages created from this message factory.
	 * This header is applied to all Messages created from this Factory object except those
	 * that take String for an argument and create Message from the given String.
	 * 
	 * @param userAgent -- the user agent header to set.
	 * 
	 */
	
	public void setCommonUserAgentHeader(UserAgentHeader userAgent);

	
	/**
	 * Set default charset used for encoding String content. Note that this
	 * will be applied to all content that is encoded. The default is UTF-8.
	 * 
	 * @param charset -- charset to set.
	 * @throws NullPointerException if null arg
	 * @throws IllegalArgumentException if Charset is not a known charset.
	 * 
	 */
	public  void setDefaultContentEncodingCharset(String charset) 
			throws NullPointerException,IllegalArgumentException ;
	
}
