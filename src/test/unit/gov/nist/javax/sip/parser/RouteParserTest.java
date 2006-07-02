/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others. 
* This software is has been contributed to the public domain. 
* As a result, a formal license is not needed to use the software.
* 
* This software is provided "AS IS."  
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* 
*/
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.AcceptLanguageParser;
import gov.nist.javax.sip.parser.RouteParser;

public class RouteParserTest extends ParserTestCase {

	public void testParser() {
		// TODO Auto-generated method stub
		String[] routes =  {
		 "Route: <sip:alice@atlanta.com>\n",
	     "Route: sip:bob@biloxi.com \n",
	     "Route: <sip:TIA-P25-U2Uorig@AF.003.1300.p25dr;lr>\n",
	     "Route: sip:alice@atlanta.com, sip:bob@biloxi.com, sip:carol@chicago.com\n"};
		super.testParser(RouteParser.class,routes);
	}

}
