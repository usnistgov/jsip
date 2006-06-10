package gov.nist.core;

/**
 * Base token class.
 * @version  JAIN-SIP-1.1
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

public class Token {
	protected String tokenValue;
	protected int tokenType;
	public String getTokenValue() {
		return this.tokenValue;
	}
	public int getTokenType() {
		return this.tokenType;
	}
	public String toString() {
		return "tokenValue = " + tokenValue + "/tokenType = " + tokenType;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
