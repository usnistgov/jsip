package gov.nist.javax.sdp.parser;
import  gov.nist.core.*;
import  java.text.ParseException;
import  gov.nist.javax.sdp.fields.*;

/**
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public abstract class SDPParser extends ParserCore {

	

	public abstract SDPField parse() throws ParseException;


}


