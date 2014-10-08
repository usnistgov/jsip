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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogLevels;
import gov.nist.core.StackLogger;

/**
 * 
 * Decodes a web socket frame from wire protocol version 8 format. This code was originally based on <a
 * href="https://github.com/joewalnes/webbit">webbit</a>.
 *
 * @author vladimirralev
 *
 */
public class WebSocketCodec {

	private static StackLogger logger = CommonLogger
			.getLogger(WebSocketCodec.class);
	
	private static final byte OPCODE_CONT = 0x0;
	private static final byte OPCODE_TEXT = 0x1;
	private static final byte OPCODE_BINARY = 0x2;
	private static final byte OPCODE_CLOSE = 0x8;
	private static final byte OPCODE_PING = 0x9;
	private static final byte OPCODE_PONG = 0xA;

	// Websocket metadata
	private int fragmentedFramesCount;
	private boolean frameFinalFlag;
	private int frameRsv;
	private int frameOpcode;
	private long framePayloadLength;
	private byte[] maskingKey = new byte[4];
	private final boolean allowExtensions;
	private final boolean maskedPayload;
	private boolean closeOpcodeReceived;

	
	// THe payload inside the websocket frame starts at this index
	private int payloadStartIndex = -1;

	// Buffering incomplete and overflowing frames
	private byte[] buffer = new byte[66000];
	private int writeIndex = 0;
	private int readIndex;
	
	// Total webscoket frame (metadata + payload)
	private long totalPacketLength = -1;


	public WebSocketCodec(boolean maskedPayload, boolean allowExtensions) {

		this.maskedPayload = maskedPayload;
		this.allowExtensions = allowExtensions;
	}
	private byte readNextByte() {
		if(readIndex >= writeIndex) {
			throw new IllegalStateException();
		}
		return this.buffer[readIndex++];
	}

	public byte[] decode(InputStream is)
			throws Exception {
		int bytesRead = is.read(buffer, writeIndex, buffer.length - writeIndex);
		
		if(bytesRead < 0) bytesRead = 0;
		
		// Update the count in the buffer
		writeIndex += bytesRead;
		
		// Start over from scratch. This is rare and doesn't affect performance
		readIndex = 0;
		
		// All TCP slow-start algorithms will be cut off right here without further analysis
		if(writeIndex<4) {
			if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
				logger.logDebug("Abort decode. Write index is at " + writeIndex);
			}
			return null;
		}

		byte b = readNextByte();
		frameFinalFlag = (b & 0x80) != 0;
		frameRsv = (b & 0x70) >> 4;
		frameOpcode = b & 0x0F;

		if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
			logger.logDebug("Decoding WebSocket Frame opCode=" + frameOpcode);
		}
		
		
		if(frameOpcode == 8) {
			//https://code.google.com/p/chromium/issues/detail?id=388243#c15
			this.closeOpcodeReceived = true;
		}


		// MASK, PAYLOAD LEN 1
		b = readNextByte();
		boolean frameMasked = (b & 0x80) != 0;
		int framePayloadLen1 = b & 0x7F;

		if (frameRsv != 0 && !allowExtensions) {
			protocolViolation("RSV != 0 and no extension negotiated, RSV:" + frameRsv);
			return null;
		}

		if (maskedPayload && !frameMasked) {
			protocolViolation("unmasked client to server frame");
			return null;
		}

		protocolChecks();

		try {
			// Read frame payload length
			if (framePayloadLen1 == 126) {
				int byte1 = 0xff & readNextByte();
				int byte2 = 0xff & readNextByte();
				int value = (byte1<<8) | byte2;
				framePayloadLength = value;
			} else if (framePayloadLen1 == 127) {
				long value = 0;
				for(int q=0;q<8;q++) {
					value &= (0xff&readNextByte())<<(7-q);
				}
				framePayloadLength = value;

				if (framePayloadLength < 65536) {
					protocolViolation("invalid data frame length (not using minimal length encoding): " + framePayloadLength);
					return null;
				}
			} else {
				framePayloadLength = framePayloadLen1;
			}
			
			if(framePayloadLength < 0) {
				protocolViolation("Negative payload size: " + framePayloadLength);
			}

			if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
				logger.logDebug("Decoding WebSocket Frame length=" + framePayloadLength);
			}

			// Analyze the mask
			if (frameMasked) {
				for(int q=0; q<4 ;q++)
					maskingKey[q] = readNextByte();
			}
		}
		catch (IllegalStateException e) {
			// the stream has ended we don't have enough data to continue
			return null;
		}

		// Remember the payload position
		payloadStartIndex = readIndex;
		totalPacketLength = readIndex + framePayloadLength;

		// Check if we have enough data at all
		if(writeIndex < totalPacketLength) {
			if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
				logger.logDebug("Abort decode. Write index is at " + writeIndex + " and totalPacketLength is " + totalPacketLength);
			}
			return null; // wait for more data
		}

		// Unmask data if needed and only if the condition above is true
		if (frameMasked) {
			unmask(buffer, payloadStartIndex, (int) (payloadStartIndex + framePayloadLength));
		}

		// Finally isolate the unmasked payload, the bytes are plaintext here
		byte[] plainTextBytes = new byte[(int) framePayloadLength];
		System.arraycopy(buffer, payloadStartIndex, plainTextBytes, 0, (int) framePayloadLength);
		
		// Now move the pending data to the begining of the buffer so we can continue having good stream
		for(int q=1; q<writeIndex - totalPacketLength; q++) {
			buffer[q] = buffer[(int)totalPacketLength + q];
		}
		writeIndex -= totalPacketLength;
		
		if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
			logger.logDebug("writeIndex = " + writeIndex + " " + totalPacketLength);
		}
		
		// All done, we are ready to be called again
		return plainTextBytes;
	}
	
	protected static byte[] encode(byte[] msg, int rsv, boolean fin, boolean maskPayload) throws Exception {
		return encode(msg, rsv, fin, maskPayload, OPCODE_TEXT);
	}


	protected static byte[] encode(byte[] msg, int rsv, boolean fin, boolean maskPayload, byte opcode) throws Exception {
		ByteArrayOutputStream frame = new ByteArrayOutputStream();

		int length = msg.length;

		if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
			logger.logDebug("Encoding WebSocket Frame opCode=" + opcode + " length=" + length);
		}

		int b0 = 0;
		if (fin) {
			b0 |= 1 << 7;
		}
		b0 |= rsv % 8 << 4;
		b0 |= opcode % 128;


		if (length <= 125) {
			frame.write(b0);
			byte b = (byte) (maskPayload ? 0x80 | (byte) length : (byte) length);
			frame.write(b);
		} else if (length <= 0xFFFF) {
			frame.write(b0);
			frame.write(maskPayload ? 0xFE : 126);
			frame.write(length >>> 8 & 0xFF);
			frame.write(length & 0xFF);
		} else {
			frame.write(b0);
			frame.write(maskPayload ? 0xFF : 127);
			for(int q=0;q<8;q++) {
				frame.write((0xFF)&(length>>q));
			}
		}
		if(maskPayload) {
			byte[] mask = new byte[] {1,1,1,1};
			frame.write(mask);
			applyMask(msg, 0, msg.length, mask);
		}
		frame.write(msg);
		return frame.toByteArray();

	}

	private void unmask(byte[] frame, int startIndex, int endIndex) {
		applyMask(frame, startIndex, endIndex, maskingKey);
	}
	
	public static void applyMask(byte[] frame, int startIndex, int endIndex, byte[] mask) {
		for (int i = 0; i < endIndex-startIndex; i++) {
			frame[startIndex+i] = (byte) (frame[startIndex+i] ^ mask[i % 4]);
		}
	}

	private void protocolViolation(String reason)  {
		throw new RuntimeException(reason);
	}

	private void protocolChecks() {
		if (frameOpcode > 7) { // control frame (have MSB in opcode set)

			// control frames MUST NOT be fragmented
			if (!frameFinalFlag) {
				protocolViolation("fragmented control frame");
			}

			// check for reserved control frame opcodes
			if (!(frameOpcode == OPCODE_CLOSE || frameOpcode == OPCODE_PING || frameOpcode == OPCODE_PONG)) {
				protocolViolation("control frame using reserved opcode " + frameOpcode);
			}
		} else { // data frame
			// check for reserved data frame opcodes
			if (!(frameOpcode == OPCODE_CONT || frameOpcode == OPCODE_TEXT || frameOpcode == OPCODE_BINARY)) {
				protocolViolation("data frame using reserved opcode " + frameOpcode);
			}

			// check opcode vs message fragmentation state 1/2
			if (fragmentedFramesCount == 0 && frameOpcode == OPCODE_CONT) {
				protocolViolation("received continuation data frame outside fragmented message");
			}

			// check opcode vs message fragmentation state 2/2
			if (fragmentedFramesCount != 0 && frameOpcode != OPCODE_CONT && frameOpcode != OPCODE_PING) {
				protocolViolation("received non-continuation data frame while inside fragmented message");
			}
		}
	}
	
	public boolean isCloseOpcodeReceived() {
		return this.closeOpcodeReceived;
	}
}
