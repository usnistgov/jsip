package gov.nist.javax.sip.stack;

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
	ByteBuffer prepareEncryptedDataBuffer();
	/**
	 * Add plain text data in the queue. It will be encrpted later in generic way
	 * @param bytes
	 * @throws Exception
	 */
	void addPlaintextBytes(byte[] bytes) throws Exception;
}
