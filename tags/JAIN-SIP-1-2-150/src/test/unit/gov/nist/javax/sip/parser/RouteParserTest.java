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


import gov.nist.javax.sip.parser.RouteParser;

public class RouteParserTest extends ParserTestCase {

    public void testParser() {
        // TODO Auto-generated method stub
        String[] routes =  {
         "Route: <sip:alice@atlanta.com>\n",
         "Route: \"AliceA\" <sip:alice@atlanta.com>\n",
         // technically illegal >
         "Route: sip:bob@biloxi.com \n",
         "Route: <sip:TIA-P25-U2Uorig@AF.003.1300.p25dr;lr>\n",
         // pmusgrave - illegal (must have <>'s )
         // "Route: sip:alice@atlanta.com, sip:bob@biloxi.com, sip:carol@chicago.com\n",
        "Route: <sip:bigbox3.site3.atlanta.com;lr>,<sip:server10.biloxi.com;lr>\n",
        "Route: <sip:3Zqkv5dajqaaas0tCjCxT0xH2ZEuEMsFl0xoasip%3A%2B3519116786244%40siplab.domain.com@213.0.115.163:7070;lr>\n"};
        super.testParser(RouteParser.class,routes);
    }

}
