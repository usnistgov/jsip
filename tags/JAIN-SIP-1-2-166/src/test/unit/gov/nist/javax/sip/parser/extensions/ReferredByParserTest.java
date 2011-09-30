package test.unit.gov.nist.javax.sip.parser.extensions;

import test.unit.gov.nist.javax.sip.parser.ParserTestCase;
import gov.nist.javax.sip.parser.extensions.ReferredByParser;

public class ReferredByParserTest extends ParserTestCase {

    public void testParser() {

        String to[] =
        {   "Referred-By: <sip:dave@denver.example.org?" +
                "Replaces=12345%40192.168.118.3%3Bto-tag%3D12345%3Bfrom-tag%3D5FFE-3994>\n",
            "Referred-By: <sip:+1-650-555-2222@ss1.wcom.com;user=phone>;tag=5617\n",
            "Referred-By: T. A. Watson <sip:watson@bell-telephone.com>\n",
            "Referred-By: LittleGuy <sip:UserB@there.com>\n",
            "Referred-By: sip:mranga@120.6.55.9\n",
            "Referred-By: sip:mranga@129.6.55.9 ; tag=696928473514.129.6.55.9\n" };


        super.testParser(ReferredByParser.class,to);
    }

}
