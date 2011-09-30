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
/*
 * Created on Jul 26, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;


/**
 *
 */
public class CallInfoParserTest extends ParserTestCase {



    public void testParser() {
        String r[] = {
                "Call-Info: <http://wwww.example.com/alice/photo.jpg> ;purpose=icon,"
                        + "<http://www.example.com/alice/> ;purpose=info\n",
                "Call-Info: <http://wwww.example.com/alice/photo1.jpg>\n" };
        super.testParser(CallInfoParser.class,r);
    }

}
