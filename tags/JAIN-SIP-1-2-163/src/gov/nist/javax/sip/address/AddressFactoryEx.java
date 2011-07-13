/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package gov.nist.javax.sip.address;

import java.text.ParseException;

import javax.sip.address.AddressFactory;

/**
 * This interface is extension to {@link javax.sip.address.AddressFactory}. It
 * declares methods which may be useful to user.
 * 
 * @author baranowb
 * @version 1.2
 */
public interface AddressFactoryEx extends AddressFactory {

	/**
	 * Creates SipURI instance from passed string. 
	 * 
	 * @param sipUri
	 *            - uri encoded string, it has form of:
	 *            <sips|sip>:username@host[:port]. NOTE: in case of IPV6, host must be
	 *            enclosed within [].
	 * @throws ParseException if the URI string is malformed. 
	 */
	public javax.sip.address.SipURI createSipURI(String sipUri) throws ParseException;

}
