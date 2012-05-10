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
 * Created on Jul 27, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.SIPHeaderList;
import gov.nist.javax.sip.parser.HeaderParser;

import java.lang.reflect.Constructor;

import junit.framework.TestCase;

/**
 * Superclass for all test cases in this directory. The printlns will be replaced with logger
 * calls.
 * 
 */
public abstract class ParserTestCase extends TestCase {
    static {
        SIPHeaderList.setPrettyEncode(false);
    }
    protected HeaderParser createParser(Class parserClass, String header) {

        try {
            Constructor constructor = parserClass.getConstructor(new Class[] {
                String.class
            });
            return (HeaderParser) constructor.newInstance(new String[] {
                header
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("fatal error");
        }
        return null;
    }

    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("start " + getClass().getName());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        System.out.println("done " + getClass().getName());
    }

    protected void testParser(Class parserClass, String[] headers) {
        try {
            for (int i = 0; i < headers.length; i++) {
                System.out.print(headers[i]);
                HeaderParser hp = createParser(parserClass, headers[i]);
                SIPHeader hdr = (SIPHeader) hp.parse();

                if ( hdr instanceof SIPHeaderList<?> ) {
                	SIPHeaderList<?> list = (SIPHeaderList<?>) hdr;
                	assertNotNull( "Header should be added to list", list.getFirst() );
                	// JvB: Should be consistent, some parser classes override getFirst but leave list empty
                	assertTrue( "List should contain at least 1 header", list.size() > 0 );	
                }
                
                hp = createParser(parserClass, ((SIPHeader) hdr.clone()).encode().trim() + "\n");
                System.out.println("Encoded header = " + hdr.encode());
                assertEquals(hdr, hp.parse());

            }
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
            fail(getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception " + getClass().getName());
        }
    }

    public abstract void testParser();

}
