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

import gov.nist.core.CommonLogger;
import gov.nist.core.LogLevels;
import gov.nist.core.StackLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Websocket handshake rev 13 and rev 8
 *
 */
public class WebSocketHttpHandshake {

	private static StackLogger logger = CommonLogger
			.getLogger(WebSocketHttpHandshake.class);
	
	private HashMap<String, String> headers = new HashMap<String, String>();

	public byte[] createHttpResponse(String request) throws Exception {
		
		if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
			logger.logDebug("Request=" + request);
		}
		InputStream is = new ByteArrayInputStream(request.getBytes());

		boolean isSecure = false;

		String line = readLine(is);

		if (line == null) {
			return null;
		}

		String[] parts = line.split(" ");
		if (parts.length >= 3) {
			headers.put("ctx", parts[1]);
		}
		while (!line.isEmpty()) {
			line = readLine(is);

			if (line == null) {
				continue;
			}

			if (line.isEmpty()) {
				continue;
			}

			parts = line.split(":", 2);
			if (parts.length != 2) {
				continue;
			}
			if (parts[0].toLowerCase().startsWith("sec-websocket-key")) {
				isSecure = true;
			}
			headers.put(parts[0].trim(), parts[1].trim());
		}
		if (isSecure) {
			byte[] key3 = new byte[8];
			is.read(key3);
		}

		//answer the handshake
		StringBuilder sb = new StringBuilder();
		String lineSeparator = "\r\n";
		sb.append("HTTP/1.1 101 Web Socket Protocol Handshake").append("\r\n");
		sb.append("Upgrade: WebSocket").append(lineSeparator);
		sb.append("Connection: Upgrade").append(lineSeparator);
		if (isSecure) {
			sb.append("Sec-");
		}

		sb.append("WebSocket-Origin: ").append(headers.get("Origin")).append(lineSeparator);


		if (isSecure) {
			sb.append("Sec-");
		}
		sb.append("WebSocket-Location: ws://").append(headers.get("Host")).
		append(headers.get("ctx")).append(lineSeparator);

		sb.append("Sec-WebSocket-Accept: ").append(computeRev13Response(headers.get("Sec-WebSocket-Key"))).append(lineSeparator);
		//sb.append("myheader: nothing");
		//.append("Sec-WebSocket-Protocol: chat")
		//        .append("Server: Kaazing Gateway\n" + 
		//        		"Date: Tue, 21 Aug 2012 00:59:35 GMT\n" + 
		//        		"Access-Control-Allow-Origin: http://www.websocket.org\n" + 
		//        		"Access-Control-Allow-Credentials: true\n" + 
		//        		"Access-Control-Allow-Headers: content-type\n" + 
		//        		"Access-Control-Allow-Headers: authorization\n" + 
		//        		"Access-Control-Allow-Headers: x-websocket-extensions\n" + 
		//        		"Access-Control-Allow-Headers: x-websocket-version\n" + 
		//        		"Access-Control-Allow-Headers: x-websocket-protocol\n");
		if (headers.get("Protocol") != null) {
			if (isSecure) {
				sb.append("Sec-");
			}
			sb.append("Protocol: ").append(headers.get("Protocol")).append(lineSeparator);
		}

		if (headers.get("Sec-WebSocket-Protocol") != null) {
			sb.append("Sec-WebSocket-Protocol: ").append(headers.get("Sec-WebSocket-Protocol")).append(lineSeparator);
		}
		sb.append(lineSeparator);

		String response = sb.toString();

		if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
			logger.logDebug("Response=" + response);
		}
		byte[] output = sb.toString().getBytes();

		return output;
	}

	static String computeRev13Response(String key) throws IOException {
		key = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA1");
			md.reset();
		} catch (NoSuchAlgorithmException ex) {
			// log.log(Level.SEVERE, "MD5 Algorithm not found", ex);
			ex.printStackTrace();
		}
		byte[] digest = md.digest(key.getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		return new String(Base64Coder.encode(digest));
		
	}


	private String readLine(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();

		int cr = '\r';
		int nl = '\n';

		boolean gotcr = false;

		while (true) {
			int input = is.read();

			if (input == -1) {
				return null;
			}

			if (input == cr) {
				gotcr = true;
				continue;
			}
			if (input == nl && gotcr) {
				break;
			} else if (input == nl) {
				//we do this only because the protocol
				//tells ut that there must be a cr before the nl.
				return null;
			}
			sb.append((char) input);
		}
		return sb.toString();
	}
}
