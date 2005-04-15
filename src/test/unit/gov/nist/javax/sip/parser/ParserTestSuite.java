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
        addTestSuite(URLParserTest.class);
        addTestSuite(ViaParserTest.class);
    }

    public static Test suite() {
        return new ParserTestSuite("ParserTestSuite");
    }

}
