package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.RetryAfter;
import gov.nist.javax.sip.parser.RetryAfterParser;



public class RetryAfterParserTest extends ParserTestCase {
       
    @Override
    public void testParser() {
        // TODO Auto-generated method stub
        String rr[] = {
                "Retry-After: 18000;duration=3600\n",
                "Retry-After: 120;duration=3600;ra=oli\n",
                "Retry-After: 1220 (I'm in a meeting)\n",
                "Retry-After: 1230 (I'm in a meeting);fg=der;duration=23\n"
            };

            super.testParser(RetryAfterParser.class, rr);
            try {
                RetryAfter retryAfter = new RetryAfter();
                retryAfter.setRetryAfter(100);
                retryAfter.setDuration(10);
                retryAfter.setComment("foo");
                System.out.println(new RetryAfterParser(retryAfter.encode()).parse().toString());
                assertTrue( retryAfter.equals(new RetryAfterParser(retryAfter.encode()).parse()));
            } catch (Exception ex) {
                fail("Could not parse encoded header");
            }
        
    }

}
