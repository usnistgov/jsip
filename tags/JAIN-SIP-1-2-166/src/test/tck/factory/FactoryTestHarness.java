package test.tck.factory;

import javax.sip.header.*;
import test.tck.*;

public class FactoryTestHarness extends TestHarness {

    protected static String hosts[] =
        {
            "herbivore.ncsl.nist.gov",
            "foo.bar.com",
            "129.6.55.181",
            "herbivore.ncsl.nist.gov:5070",
            "big.com",
            "big.com",
            "big.com",
            "gateway.com",
            "10.1.2.3",
            "example.com",
            "alice",
            "registrar.com",
            "10.10.30.186:666",
            "abc123.xyz-test." };

    static protected String urls[] =
        {
            "sip:herbivore.ncsl.nist.gov:5070;maddr=129.6.55.251;lr",
            "sip:1-301-975-3664@foo.bar.com;user=phone",
            "sip:129.6.55.181",
            "sip:herbivore.ncsl.nist.gov:5070;maddr=129.6.55.251;method=INVITE?contact=sip:foo.bar.com",
            "sip:j.doe@big.com",
            "sip:j.doe:secret@big.com;transport=tcp",
            "sip:j.doe@big.com?subject=project",
            "sip:j.doe@big.com?subject=",
            "sip:+1-212-555-1212:1234@gateway.com;user=phone",
            "sip:1212@gateway.com",
            "sip:alice@10.1.2.3",
            "sip:alice@example.com",
            "sip:alice",
            "sip:alice@registrar.com;method=REGISTER",
            "sip:annc@10.10.30.186:6666;early=no;play=http://10.10.30.186:8080/examples/pin.vxml",
            "http://10.10.30.186:8080/examples/pin.vxml",
            "sip:&=+$,;?/%00-_.!~*'()@abc123.xyz-test.:12345;weird123[]/:&+$=weird123[]/:&+$?[]/?:+$=[]/?:+$",
            "sip:[2001:db8::9:1];real=[2001:db8::9:255]\n",
            "sip:user;par=u%40example.net@example.com\n"
        };

    protected static String telUrls[] =
        { "tel:+463-1701-4291", "tel:46317014291;phone-context=+5", "tel:+1-212-555-1212" };

    protected static String phoneNumbers[] =
        { "+463-1701-4291", "46317014291;phone-context=+5", "+1-212-555-1212" };

    protected static String addresses[] =
        {
            "herbivore.ncsl.nist.gov",
            "1-301-975-3664@foo.bar.com",
            "129.6.55.181",
            "herbivore.ncsl.nist.gov:5070",
            "j.doe@big.com",
            "secret@big.com",
            "j.doe@big.com",
            "1212@gateway.com",
            "alice@10.1.2.3",
            "alice@example.com",
            "alice",
            "alice@registrar.com",
            "annc@10.10.30.186:666" // 666 is the sign of the beast. heh. heh.
    };
    protected static String msgString =
        "INVITE sip:littleguy@there.com:5060 SIP/2.0\r\n"
            + "Via: SIP/2.0/UDP 65.243.118.100:5050\r\n"
            + "From: Me  <sip:M.Ranganathan@sipbakeoff.com>;tag=1234\r\n"
            + "To: \"littleguy@there.com\" <sip:littleguy@there.com:5060> \r\n"
            + "Call-ID: Q2AboBsaGn9!?x6@sipbakeoff.com \r\n"
            + "CSeq: 1 INVITE \r\n"
            // BUG submitted by Ben Evans (opencloud):
            //  was missing \r\n on Accept and Accept-Language headers
            // (works OK in NIST-SIP but not valid SIP syntax)
            + "Accept: application/sdp;level=1\r\n"
            + "Accept-Language: da\r\n"
            + "Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\"\r\n"
            + "Accept-Encoding: identity; q=0.5\r\n"
            + "Authorization: Digest username=\"UserB\", realm=\"MCI WorldCom SIP\","
            + " nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\", opaque=\"\","
            + " uri=\"sip:ss2.wcom.com\", "
            + "response=\"dfe56131d1958046689cd83306477ecc\"\r\n"
            + "Contact:<sip:utente@127.0.0.1:5000;transport=udp>;expires=3600\r\n"
            + "Content-Type: text/html; charset=ISO-8859-4\r\n"
            + "Date: Sun, 07 Jan 2001 19:05:06 GMT\r\n"
            + "Organization: Boxes by Bob\r\n"
            + "Content-Length: 0\r\n\r\n";

    protected static String multiHeaders[] =
        {
            "Allow: INVITE , FooBar\r\n",
            "Accept-Encoding: compress;q=0.5,gzip;q=1.0\r\n",
            "Accept: application/sdp;level=1,application/x-private,text/html\r\n",
            "Contact: Bo Bob Biggs "
                // BUG submitted by Ben Evans (opencloud): SIP syntax does not
                // permit space between < and URI
                + "<sip:user@example.com?Route=%3Csip:sip.example.com%3E>,"
                + "Joe Bob Briggs <sip:mranga@nist.gov>\r\n",
            "Proxy-Require: foo, 1234 \r\n",
            "Record-Route: <sip:bob@biloxi.com>,"
                + "<sip:bob@biloxi.com;maddr=10.1.1.1>,"
                + "<sip:+1-650-555-2222@iftgw.there.com;"
                + "maddr=ss1.wcom.com>\r\n",

        // JvB: added, bug report by Young-Geun Park on 17-8-2009              
        "Record-Route: sip:07077005000@abc.com,tel:07077005000\r\n",

        // JvB: removed this one, P-Asserted-Identity isn't officially part of the TCK
        // "P-Asserted-Identity: sip:07077005000@abc.com,tel:07077005000\r\n",
        };

    protected static String headers[] =
        {
            "Accept: application/sdp;level=1\n",
            "Accept: application/x-private\n",
            "Accept: text/html\n",
            "Accept: text/*\n",
            "Accept: text/*strange_extension*\n",
            "Accept: application/sdp;level=1;q=1.0\n",
            "Accept-Encoding: compress\n",
            "Accept-Encoding: gzip\n",
            "Accept-Encoding:\n",
            "Accept-Encoding: *\n",
            "Accept-Encoding: *special*\n",
            "Accept-Encoding: compress;q=0.5\n",
            "Accept-Encoding: gzip;q=1.0\n",
            "Accept-Encoding: identity; q=0.5\n",
            "Accept-Language: da\n",
            "Accept-Language:\n",
            "Accept-Language: en-gb;q=0.8\n",
            "Accept-Language: *\n",

            "Alert-Info: <http://www.example.com/sounds/moo.wav>;purpose=icon\n",
            "Alert-Info: <http://www.example.com/alice/> ;purpose=info\n",
            "Alert-Info: <http://wwww.example.com/alice/photo1.jpg>\n",
            "Allow: INVITE \r\n",
            "Allow: FooBars\r\n",
            "Allow-Events: pack1.pack2\n",
            "Allow-Events: pack1\n",

            // JvB: added
            "u: pack1\n", "U: pack1\n",

            "Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\"\n",
            // Issue 4 posted by Larry B
            "Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\",rspauth=\"hello\"\n",
            "Authorization: Digest username=\"UserB\", realm=\"MCI WorldCom SIP\","
                + " nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\", opaque=\"\","
                + " uri=\"sip:ss2.wcom.com\","
                + " response=\"dfe56131d1958046689cd83306477ecc\"\n",
            "Authorization: Digest username=\"apro,kop\","
                + "realm=\"Realm\","
                + "nonce=\"MTA1MDMzMjE5ODUzMjUwM2QyMzBhOTJlMTkxYjIxYWY1NDlhYzk4YzNiMGYz\""
                + ",uri=\"sip:nortelnetworks.com:5060\","
                + "response=\"dbfba6c0e9664b45b7d224d2b52a1d01\","
                + "algorithm=MD5,"
                + "cnonce=\"VG05eWRHVnNJRTVsZEhkdmNtdHpNVEExTURNek16WTFOREUyTUE9PQ==\","
                + "qop=auth-int,nc=00000001\n",
            "Call-ID: f0b40bcc-3485-49e7-ad1a-f1dfad2e39c9@10.5.0.53\n",
            "Call-ID: f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com\n",
            "i:f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com\n",
            "I:f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com\n",
            "Call-ID: 1@10.0.0.1\n",
            "Call-ID: kl24ahsd546folnyt2vbak9sad98u23naodiunzds09a3bqw0sdfbsk34poouymnae0043nsed09mfkvc74bd0cuwnms05dknw87hjpobd76f\n",
            "Call-Id: 281794\n",
            "Call-Info: <http://wwww.example.com/alice/photo.jpg> ;purpose=icon\n",
            "Call-Info: <http://www.example.com/alice/> ;purpose=info\n",
            "Call-Info: <http://wwww.example.com/alice/photo1.jpg>\n",
            "Contact:<sip:utente@127.0.0.1:5000;transport=udp>;expires=3600\n",
            "Contact:BigGuy<sip:utente@127.0.0.1:5000>;expires=3600\n",
            "Contact: sip:4855@166.35.224.216:5060\n",
            "Contact: sip:user@host.company.com\n",
        // BUG reported by Ben Evans (opencloud): SIP syntax
        // does not permit space between < and URI
            "Contact: Bo Bob Biggs"
                + "<sip:user@example.com?Route=%3Csip:sip.example.com%3E>\n",
            "Contact: Joe Bob Briggs <sip:mranga@nist.gov>\n",
            "Contact: \"Mr. Watson\" <sip:watson@worcester.bell-telephone.com>"
                + " ; q=0.7; expires=3600",
            "Contact: \"Mr. Watson\" <mailto:watson@bell-telephone.com>;q=0.1\n",
            "Contact: LittleGuy <sip:UserB@there.com;user=phone>\n",
            "Contact: <sip:+1-972-555-2222@gw1.wcom.com;user=phone>\n",
            "Contact: <tel:+1-972-555-2222>\n",
            "Contact:*\n",
            "Contact: *displayname* <sip:user@provider.com>\n",
            "Contact:BigGuy<sip:utente@127.0.0.1;5000>;Expires=3600\n",

            // JvB: added
            "m:BigGuy<sip:utente@127.0.0.1;5000>;Expires=3600\n",
            "M:BigGuy<sip:utente@127.0.0.1;5000>;Expires=3600\n",

            "Content-Disposition: session\n",
            "Content-Disposition: render;handling=hand;optional=opt \n",
            "Content-Encoding: gzip \n",

            // JvB: added
            "e: gzip \n",
            "E: gzip \n",
            "Content-Language: fr \n",
            "l: 345\n",
            "L: 345\n",
            "Content-Length: 3495\n",
            "Content-Length: 0 \n",
            "c: text/html; charset=ISO-8859-4\n",
            "Content-Type: text/html; charset=ISO-8859-4\n",
            "Content-Type: application/sdp\n",
            "Content-Type: application/sdp; o=we ;l=ek ; i=end \n",
            "CSeq: 17 INVITE\n",
            "CSeq: 17 ACK\n",
            "CSeq : 18   BYE\n",
            "CSeq:1 CANCEL\n",
            "CSeq: 3 BYE\n",
            "Date: Sun, 07 Jan 2001 19:05:06 GMT\n",

            // JvB: Date is officially 1 or 2 digits
            "Date: Mon, 8 Jan 2001 19:05:06 GMT\n",
            "Error-Info: <sip:not-in-service-recording@atlanta.com>\n",
            "Error-Info: <sip:not-in-service-recording@atlanta.com>;param1=oli\n",

            // JvB: These ones were missing
            "Expires: 3600\n",
            "Event: register\n",
            "o: presence\n",
            "O: presence\n",

            // BUG from Ben Evans (opencloud): space before >
            "From: foobar at com <sip:4855@166.34.120.100>;tag=1024181795\n",
            "From: sip:user@company.com\n",
            "From: sip:caller@university.edu\n",
            "from: sip:localhost\n",
            "from: * <sip:localhost>\n",// '*' is a token in from

            // JvB: test short form too
            "f: sip:user@company.com\n",
            "F: sip:user@company.com\n",

            "FROM: \"A. G. Bell\" <sip:agb@bell-telephone.com> ;tag=a48s\n",
            "In-Reply-To: 70710@saturn.bell-tel.com\n",
            "In-Reply-To: 70710 \n",
            "Max-Forwards: 34\n",
            "Max-Forwards: 10 \n",
            "MIME-Version: 1.0 \n",
            "Min-Expires: 60 \n",
            "Organization: Boxes by Bob\n",
            "Priority: emergency\n",
            "Proxy-Authenticate: Digest realm=\"MCI WorldCom SIP\","
                + "domain=\"sip:ss2.wcom.com\", "
                + "nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\","
                + "opaque=\"\", stale=FALSE, algorithm=MD5\n",
            "Proxy-Authorization: Digest realm=\"MCI WorldCom SIP\","
                + "domain=\"sip:ss2.wcom.com\", "
                + "nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\","
                + "opaque=\"\", stale=FALSE, algorithm=MD5\n",
            "Proxy-Require: foo \n",
            "Proxy-Require: 1234 \n",
            "RAck: 776656 1 INVITE\n",
            "Reason: SIP ;cause=200 ;text=\"Call completed elsewhere\"\n",
            "Reason: Q.850 ;cause=16 ;text=\"Terminated\"\n",
            "Reason: SIP ;cause=600 ;text=\"Busy Everywhere\"\n",
            // API BUG: what should the default cause code be? - NIST uses -1 but this is not specified.
            // TBD -- documentation fix in jain-sip 1.2 should specify the default cause to be -1
            "Reason: SIP \n",
            "Record-Route: <sip:bob@biloxi.com>",
            "Record-Route: <sip:bob@biloxi.com;maddr=10.1.1.1>",
            "Record-Route: <sip:+1-650-555-2222@iftgw.there.com;"
                + "maddr=ss1.wcom.com>\n",
            "Refer-To: <sip:+1-650-555-2222@ss1.wcom.com;user=phone>;tag=5617\n",
            "Refer-To: T. A. Watson <sip:watson@bell-telephone.com>\n",
            "Refer-To: LittleGuy <sip:UserB@there.com>\n",
            "Refer-To: sip:mranga@120.6.55.9\n",
            "Refer-To: sip:mranga@129.6.55.9 ; tag=696928473514.129.6.55.9\n",

            // JvB: added
            "r: sip:mranga@120.6.55.9\n",
            "R: sip:mranga@120.6.55.9\n",

            "Reply-To: Bob <sip:bob@biloxi.com>\n",
            "Require: 100rel \n",
            "Retry-After: 18000;duration=3600\n",
            "Retry-After: 120;duration=3600;ra=oli\n",
            // BUG reported by Ben Evans, opencloud:
            // javadoc says default duration is 0 but RI returns -1,
            // The RI was fixed to return 0
            "Retry-After: 1230 (I'm in a meeting);fg=der;duration=23\n",
            "Route: <sip:alice@atlanta.com>\n",
            "Route: <sip:bob@biloxi.com>\n",
            "RSeq: 988789 \n",
            "Server: Softphone/Beta1.5 \n",
            "Server: HomeServer v2\n",
            "Server: Nist/Beta1 (beta version) \n",
            "Server: Nist proxy (beta version)\n",
            "Server: Nist1.0/Beta2 UbiServer/vers.1.0 (new stuff) (Cool) \n",

            // JvB: added for 1.2 to support RFC3903
            "SIP-ETag: tag123\n",
            "SIP-If-Match: tag123\n",

            "Subject: Where is the Moscone?\n",
            "Subject: Need more boxes\n",

            // JvB: added
            "s: Need more boxes\n",
            "S: Need more boxes\n",

            // API BUG: default value of retry-after param not specified.
            // TBD -- documentation bug in JAIN-SIP to be fixed in 1.2
            "Subscription-State: active \n",
            "Subscription-State: terminated;reason=rejected \n",
            "Subscription-State: pending;reason=probation;expires=36\n",
            "Subscription-State: pending;retry-after=10;expires=36\n",
            "Subscription-State: pending;generic=void\n",
            "Supported: 100rel \n",
            "Supported: foo1\n",
            "Supported:\n",   // JvB: it may be empty! bug in parser

            // JvB: added
            "k: foo1\n",
            "K: foo1\n",

            "Timestamp: 54 \n",
            "Timestamp: 52.34 34.5 \n",
            "Timestamp: 52.34 .5 \n",       // JvB: also valid syntax!

            "To: <sip:+1-650-555-2222@ss1.wcom.com;user=phone>;tag=5617\n",
            "To: T. A. Watson <sip:watson@bell-telephone.com>\n",
            "To: LittleGuy <sip:UserB@there.com>\n",
            "To: sip:mranga@120.6.55.9\n",
            "To: sip:mranga@129.6.55.9 ; tag=696928473514.129.6.55.9\n",

            // JvB: added
            "t: sip:mranga@120.6.55.9\n",
            "T: sip:mranga@120.6.55.9\n",

            "Unsupported: foo \n",
            "User-Agent: Softphone/Beta1.5 \n",
            "User-Agent: Nist/Beta1 (beta version) \n",
            "User-Agent: Nist UA (beta version)\n",
            "User-Agent: Nist1.0/Beta2 Ubi/vers.1.0 (very cool) \n",
            "Via: SIP/2.0/UDP 135.180.130.133\n",
            "Via: SIP/2.0/UDP 166.34.120.100;branch=0000045d-00000001\n",
            "Via: SIP/2.0/UDP host.example.com;received=135.180.130.133\n",
            "Via: SIP/2.0/UDP [2001:db8::9:1];received=[2001:db8::9:255];branch=z9hG4bKas3-111\n",

            // JvB: added
            "v: SIP/2.0/UDP 135.180.130.133\n",
            "V: SIP/2.0/UDP 135.180.130.133\n",

            "WWW-Authenticate: Digest realm=\"MCI WorldCom SIP\","
                + "domain=\"sip:ss2.wcom.com\","
                + "nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\","
                + "opaque=\"\", stale=FALSE, algorithm=MD5\n",
            "WWW-Authenticate: Digest realm=\"MCI WorldCom SIP\","
                + "qop=\"auth\" , nonce-value=\"oli\"\n",
            "Warning: 307 isi.edu \"Session parameter 'foo' not understood\"\n",
            "Warning: 301 isi.edu \"Incompatible network address type 'E.164'\"\n",
            "Route: \"displayname\\\\\"<sip:x@y.z>;x=\"\";y=\"\\\"\\\\\"\n"
            };

    protected Header createRiHeaderFromString(String hdr) {
        try {
            StringBuffer name = new StringBuffer();
            StringBuffer body = new StringBuffer();
            int n = 0;
            for (n = 0; n < hdr.length(); n++) {
                if (hdr.charAt(n) != ':') {
                    name.append(hdr.charAt(n));
                } else
                    break;
            }
            n++;

            for (; n < hdr.length(); n++) {
                body.append(hdr.charAt(n));
            }
            Header riHeader =
                riHeaderFactory.createHeader(
                    name.toString().trim(),
                    body.toString().trim());
            return riHeader;
        } catch (java.text.ParseException ex) {
            return null;
        }
    }

    public FactoryTestHarness(String name) {
        super(name);
    }

}
