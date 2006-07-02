package test.rihelper.javax.sip;

import java.util.*;
import java.util.StringTokenizer;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import gov.nist.javax.sip.stack.*;

import java.lang.reflect.*;
import java.net.InetAddress;

import gov.nist.core.*;
import gov.nist.core.net.NetworkLayer;

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
