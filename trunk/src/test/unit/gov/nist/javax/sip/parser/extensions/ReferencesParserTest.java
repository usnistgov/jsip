package test.unit.gov.nist.javax.sip.parser.extensions;

import gov.nist.javax.sip.parser.extensions.ReferencesParser;
import test.unit.gov.nist.javax.sip.parser.ParserTestCase;

public class ReferencesParserTest extends ParserTestCase {

    @Override
    public void testParser() {
        // TODO Auto-generated method stub
        String to[] =
        {   "References:  LAXMGC0120100111203033001119@209.244.63.13.0\n",
            "References: 12345th5z8z;rel=chain;branch=z9hG4bK-XX-087cVNEsVKSG9mZd5CU399jNoQ\n",
            "References: 12345th5z8z;rel=chain;branch=z9hG4bK-XX-087cVNEsVKSG9mZd5CU399jNoQ;method=INVITE\n",
            
        };

        super.testParser(ReferencesParser.class,to);
    }

}
