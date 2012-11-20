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

import junit.framework.*;

/** A test suite for all parser tests

    @author Dan Muresan
 */
public class ParserTestSuite extends TestSuite {

    public static void main(String[] args) {
         junit.textui.TestRunner.run(new ParserTestSuite("ParserTestSuite"));
     System.exit(0);
    }

    public ParserTestSuite(String name) {
        super(name);
        addTestSuite(AcceptParserTest.class);
        addTestSuite(AcceptEncodingParserTest.class);
        addTestSuite(AcceptLanguageParserTest.class);
        addTestSuite(AddressParserTest.class);
        addTestSuite(AllowEventsParserTest.class);
        addTestSuite(AllowParserTest.class);
        addTestSuite(AuthenticationInfoParserTest.class);
        addTestSuite(AuthorizationParserTest.class);
        addTestSuite(CallIDParserTest.class);
        addTestSuite(CallInfoParserTest.class);
        addTestSuite(ContactParserTest.class);
        addTestSuite(ContentDispositionParserTest.class);
        addTestSuite(ContentEncodingParserTest.class);
        addTestSuite(ContentLanguageParserTest.class);
        addTestSuite(ContentLengthParserTest.class);
        addTestSuite(ContentTypeParserTest.class);
        addTestSuite(CSeqParserTest.class);
        addTestSuite(DateParserTest.class);
        addTestSuite(ErrorInfoParserTest.class);
        addTestSuite(EventParserTest.class);
        addTestSuite(ExpiresParserTest.class);
        addTestSuite(FromParserTest.class);
        addTestSuite(InReplyToParserTest.class);
        addTestSuite(MaxForwardsParserTest.class);
        addTestSuite(MimeVersionParserTest.class);
        addTestSuite(MinExpiresParserTest.class);
        addTestSuite(OrganizationParserTest.class);
        addTestSuite(ReferToParserTest.class);
        addTestSuite(SupportedParserTest.class);
        addTestSuite(URLParserTest.class);
        addTestSuite(ViaParserTest.class);
        addTestSuite(TimeStampParserTest.class);
    }

    public static Test suite() {
        return new ParserTestSuite("ParserTestSuite");
    }

}
