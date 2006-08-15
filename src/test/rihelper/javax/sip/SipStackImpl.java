package test.rihelper.javax.sip;

import java.util.Properties;

import javax.sip.PeerUnavailableException;

/**
 * Wrapper of SipStack used to self-test the RI.
 * 
 * @author Ivelin Ivanov
 * 
 */
public class SipStackImpl extends gov.nist.javax.sip.SipStackImpl {

	
	public SipStackImpl(Properties configurationProperties)
			throws PeerUnavailableException {
		super(configurationProperties);
	}

}
