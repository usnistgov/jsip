package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/** 
* A parser for The SIP contact header.
*/

public class ContactParser extends AddressParametersParser {
    
	public ContactParser(String contact) {
		super(contact);
	}

        protected ContactParser(Lexer lexer) {
		super(lexer);
		this.lexer = lexer;
	}

	public SIPHeader parse() throws ParseException {
		// past the header name and the colon.
		headerName(TokenTypes.CONTACT);
		ContactList retval = new ContactList();
		while(true) {
		   Contact contact = new Contact();
		    if (lexer.lookAhead(0) == '*')  {
			 this.lexer.match('*');
			 contact.setWildCardFlag(true);
		   } else super.parse(contact);
		   retval.add(contact);
		   this.lexer.SPorHT();
		   if (lexer.lookAhead(0) == ',')  {
			this.lexer.match(',');
			this.lexer.SPorHT();
		   } else if (lexer.lookAhead(0) == '\n') break;
		   else throw createParseException("unexpected char");
		}
		return retval;
	}

/**
        public static void main(String args[]) throws ParseException {
		String contact[] = {
		"Contact:<sip:utente@127.0.0.1:5000;transport=udp>;expires=3600\n",
		"Contact:BigGuy<sip:utente@127.0.0.1:5000>;expires=3600\n",
		"Contact: sip:4855@166.35.224.216:5060\n",
		"Contact: sip:user@host.company.com\n",
		"Contact: Bo Bob Biggs\n"+ 
	"< sip:user@example.com?Route=%3Csip:sip.example.com%3E >\n",
                        "Contact: Joe Bob Briggs <sip:mranga@nist.gov>\n",
                        "Contact: \"Mr. Watson\" <sip:watson@worcester.bell-telephone.com>"+
                        " ; q=0.7; expires=3600,\"Mr. Watson\" <mailto:watson@bell-telephone.com>"+
                        ";q=0.1\n",
                        "Contact: LittleGuy <sip:UserB@there.com;user=phone>"+
                        ",<sip:+1-972-555-2222@gw1.wcom.com;user=phone>,tel:+1-972-555-2222"+
                        "\n",
                        "Contact:*\n",
                        "Contact:BigGuy<sip:utente@127.0.0.1;5000>;Expires=3600\n"
                };
			
		for (int i = 0; i < contact.length; i++ ) {
		    ContactParser cp = 
			  new ContactParser(contact[i]);
		    ContactList cl = (ContactList) cp.parse(); 
		    System.out.println("encoded = " + cl.encode());
                    System.out.println();
		}
			
	}
**/
}


