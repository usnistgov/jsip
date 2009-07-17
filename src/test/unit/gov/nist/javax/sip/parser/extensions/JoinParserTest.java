package test.unit.gov.nist.javax.sip.parser.extensions;

import gov.nist.javax.sip.parser.extensions.JoinParser;
import test.unit.gov.nist.javax.sip.parser.ParserTestCase;

public class JoinParserTest extends ParserTestCase {

    public void testParser() {
        String to[] =
        {   "Join: 12345th5z8z\n",
            "Join: 12345th5z8z;to-tag=tozght6-45;from-tag=fromzght789-337-2\n",
        };

        super.testParser(JoinParser.class,to);


    }

}
