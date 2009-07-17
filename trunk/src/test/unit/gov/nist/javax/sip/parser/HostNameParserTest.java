package test.unit.gov.nist.javax.sip.parser;

import gov.nist.core.HostNameParser;
import gov.nist.core.HostPort;

import java.text.ParseException;

import junit.framework.TestCase;

/**
 * @author sli
 *
 */
public class HostNameParserTest extends TestCase {

    private final static String validHostNames[] = { "foo.bar.com:1234",
            "proxima.chaplin.bt.co.uk", "129.6.55.181:2345",
            "foo.bar.com:         1234", "foo.bar.com     :      1234   ",
            // "[3ffe:33:0:0:202:2dff:fe32:c31c%4]",
            // "[3ffe:33:0:0:202:2dff:fe32:c31c%4:1234]"
    };

    private final static String invalidHostNames[] = { ":1234", };

    public void testHostNameParser() {
        for (int i = 0; i < validHostNames.length; i++) {
            try {
                String hostName = validHostNames[i];
                System.out.println("hostName=" + hostName);
                HostNameParser hnp = new HostNameParser(hostName);
                HostPort hp = hnp.hostPort(true);
                System.out.println("[" + hp.encode() + "]");
            } catch (ParseException ex) {
                fail(ex.getMessage());
            }
        }
    }
}
