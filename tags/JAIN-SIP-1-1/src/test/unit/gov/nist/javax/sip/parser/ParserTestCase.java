/*
 * Created on Jul 27, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.header.SIPHeader;
import junit.framework.*;
import java.lang.reflect.*;

/**
 * Superclass for all test cases in this directory. The printlns will be
 * replaced with logger calls.
 *  
 */
public abstract class ParserTestCase extends TestCase {

private HeaderParser createParser(Class parserClass, String header) {
		try {
			Constructor constructor = parserClass.getConstructor
				(new Class[]{ String.class} );
			return (HeaderParser)constructor.newInstance(new String[]{header});
		} catch (Exception ex ) {
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
				hp = createParser(parserClass, ((SIPHeader)hdr.clone()).encode().trim()+"\n");
				System.out.println(hdr.encode());
				assertEquals(hdr, hp.parse());

			}
		} catch (java.text.ParseException ex) {
			ex.printStackTrace();
			fail(getClass().getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception " + getClass().getName());
			System.exit(0);
		}
	}

	public abstract void testParser();

   

}
