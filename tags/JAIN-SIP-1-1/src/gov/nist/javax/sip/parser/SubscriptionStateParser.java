package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;

/**
 * Parser for SubscriptionState header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:32 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>
 * @author M. Ranganathan <mranga@nist.gov> 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class SubscriptionStateParser extends HeaderParser {

	/**
	 * Creates a new instance of SubscriptionStateParser 
	 * @param subscriptionState the header to parse
	 */
	public SubscriptionStateParser(String subscriptionState) {
		super(subscriptionState);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected SubscriptionStateParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader (SubscriptionState  object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("SubscriptionStateParser.parse");

		SubscriptionState subscriptionState = new SubscriptionState();
		try {
			headerName(TokenTypes.SUBSCRIPTION_STATE);

			subscriptionState.setHeaderName(SIPHeaderNames.SUBSCRIPTION_STATE);

			// State:
			lexer.match(TokenTypes.ID);
			Token token = lexer.getNextToken();
			subscriptionState.setState(token.getTokenValue());

			while (lexer.lookAhead(0) == ';') {
				this.lexer.match(';');
				this.lexer.SPorHT();
				lexer.match(TokenTypes.ID);
				token = lexer.getNextToken();
				String value = token.getTokenValue();
				if (value.equalsIgnoreCase("reason")) {
					this.lexer.match('=');
					this.lexer.SPorHT();
					lexer.match(TokenTypes.ID);
					token = lexer.getNextToken();
					value = token.getTokenValue();
					subscriptionState.setReasonCode(value);
				} else if (value.equalsIgnoreCase("expires")) {
					this.lexer.match('=');
					this.lexer.SPorHT();
					lexer.match(TokenTypes.ID);
					token = lexer.getNextToken();
					value = token.getTokenValue();
					try {
						int expires = Integer.parseInt(value);
						subscriptionState.setExpires(expires);
					} catch (NumberFormatException ex) {
						throw createParseException(ex.getMessage());
					} catch (InvalidArgumentException ex) {
						throw createParseException(ex.getMessage());
					}
				} else if (value.equalsIgnoreCase("retry-after")) {
					this.lexer.match('=');
					this.lexer.SPorHT();
					lexer.match(TokenTypes.ID);
					token = lexer.getNextToken();
					value = token.getTokenValue();
					try {
						int retryAfter = Integer.parseInt(value);
						subscriptionState.setRetryAfter(retryAfter);
					} catch (NumberFormatException ex) {
						throw createParseException(ex.getMessage());
					} catch (InvalidArgumentException ex) {
						throw createParseException(ex.getMessage());
					}
				} else {
					this.lexer.match('=');
					this.lexer.SPorHT();
					lexer.match(TokenTypes.ID);
					Token secondToken = lexer.getNextToken();
					String secondValue = secondToken.getTokenValue();
					subscriptionState.setParameter(value, secondValue);
				}
				this.lexer.SPorHT();
			}
		} finally {
			if (debug)
				dbg_leave("SubscriptionStateParser.parse");
		}

		return subscriptionState;
	}

	/** Test program
	public static void main(String args[]) throws ParseException {
	    String subscriptionState[] = {
	        "Subscription-State: active \n",
	        "Subscription-State: terminated;reason=rejected \n",
	        "Subscription-State: pending;reason=probation;expires=36\n",
	        "Subscription-State: pending;retry-after=10;expires=36\n",
	        "Subscription-State: pending;generic=void\n"
	    };
	    
	    for (int i = 0; i < subscriptionState.length; i++ ) {
	        SubscriptionStateParser parser =
	        new SubscriptionStateParser(subscriptionState[i]);
	        SubscriptionState ss= (SubscriptionState) parser.parse();
	        System.out.println("encoded = " + ss.encode());
	    }
	}
	 */
}
/*
 * $Log: not supported by cvs2svn $
 */
