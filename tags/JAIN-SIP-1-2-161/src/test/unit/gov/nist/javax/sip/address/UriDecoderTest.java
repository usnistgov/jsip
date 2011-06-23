/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
package test.unit.gov.nist.javax.sip.address;

import gov.nist.javax.sip.address.UriDecoder;
import junit.framework.TestCase;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class UriDecoderTest extends TestCase {

	/**
	 * @param name
	 */
	public UriDecoderTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link gov.nist.javax.sip.address.UriDecoder#decode(java.lang.String)}.
	 */
	public void testDecode() {
		assertEquals("j@son", UriDecoder.decode("j%40son"));
		assertEquals("test", UriDecoder.decode("test"));
		assertEquals("sips:alice@atlanta.com?subject=project x&priority=urgent", UriDecoder.decode("sips:alice@atlanta.com?subject=project%20x&priority=urgent"));
		assertEquals("sip:atlanta.com;method=REGISTER?to=alice@atlanta.com", UriDecoder.decode("sip:atlanta.com;method=REGISTER?to=alice%40atlanta.com"));
		assertEquals("sip:alice@atlanta.com;transport=TCP", UriDecoder.decode("sip:%61lice@atlanta.com;transport=TCP"));
		assertEquals("sip:biloxi.com;transport=tcp;method=REGISTER?to=sip:bob@biloxi.com", UriDecoder.decode("sip:biloxi.com;transport=tcp;method=REGISTER?to=sip:bob%40biloxi.com"));
	}

}
