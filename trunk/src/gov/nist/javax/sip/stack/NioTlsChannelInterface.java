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
package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.SipStackImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
/**
 * Common interface for TLS channels. We should be able to invoke some methods in generic way.
 * @author vladimirralev
 *
 */
public interface NioTlsChannelInterface {
	/**
	 * Be able to send already encrypted data or metadata or some SSL frame padding to comply with some extension
	 * @param msg
	 * @throws IOException
	 */
	void sendEncryptedData(byte[] msg) throws IOException ;
	/**
	 * Initialize the buffer again.
	 * @return
	 */
	ByteBuffer prepareAppDataBuffer();
	/**
	 * Initialize the buffer again.
	 * @return
	 */
	ByteBuffer prepareAppDataBuffer(int capacity);
	/**
	 * Initialize the buffer again.
	 * @return
	 */
	ByteBuffer prepareEncryptedDataBuffer();
	/**
	 * Add plain text data in the queue. It will be encrpted later in generic way
	 * @param bytes
	 * @throws Exception
	 */
	void addPlaintextBytes(byte[] bytes) throws Exception;
	/**
	 * Returns the SIP Stack associated with this channel 
	 * @return
	 */
	SipStackImpl getSIPStack();
	/**
	 * Returns the Client Transaction associated with this channel
	 * @return
	 */
	ClientTransactionExt getEncapsulatedClientTransaction();
}
