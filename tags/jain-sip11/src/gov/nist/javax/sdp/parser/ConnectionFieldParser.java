package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;

import java.text.*;

/** Parser for Connection Field.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov> 
*@author M. Ranganathan <mranga@nist.gov>  
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
 */
public class ConnectionFieldParser extends SDPParser {

    /** Creates new ConnectionFieldParser */
    public ConnectionFieldParser(String connectionField) {
	this.lexer = new Lexer("charLexer",connectionField);
    }
    
    
    public ConnectionAddress connectionAddress(String address){
        ConnectionAddress connectionAddress=new ConnectionAddress();
      
        int begin=address.indexOf("/");
       
        if (begin !=-1 ) { 
            connectionAddress.setAddress(new Host(address.substring(0,begin) ));
             
            int middle=address.indexOf("/",begin+1);
            if (middle!=-1 ) {
                String ttl=address.substring(begin+1,middle);
                connectionAddress.setTtl(Integer.parseInt(ttl.trim()));
                   
                String addressNumber=address.substring(middle+1);
                connectionAddress.setPort(Integer.parseInt(addressNumber.trim() ) );
            }
            else {
                String ttl=address.substring(begin+1);
                connectionAddress.setTtl(Integer.parseInt(ttl.trim() ));
            }
        }
        else  connectionAddress.setAddress(new Host(address));
      
        return connectionAddress;
    }
    
    public ConnectionField connectionField() throws ParseException  {
        try{
            this.lexer.match ('c');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();
            
            ConnectionField connectionField=new ConnectionField();
            
            lexer.match(LexerCore.ID);
            this.lexer.SPorHT();
            Token token= lexer.getNextToken();
            connectionField.setNettype(token.getTokenValue());
            
            lexer.match(LexerCore.ID);
            this.lexer.SPorHT();
            token= lexer.getNextToken();
            connectionField.setAddressType(token.getTokenValue());
            this.lexer.SPorHT();
            String rest= lexer.getRest();
            ConnectionAddress connectionAddress=connectionAddress(rest.trim());
            
            connectionField.setAddress(connectionAddress);
            
            return connectionField;
        }
        catch(Exception e) {
            throw new ParseException(lexer.getBuffer(),lexer.getPtr());
        }  
    }

    public SDPField parse() throws ParseException {
	return this.connectionField();
    }
    
/**
    public static void main(String[] args) throws ParseException {
	    String connection[] = {
			"c=IN IP4 224.2.17.12/127\n",
			"c=IN IP4 224.2.1.1/127/3 \n",
			"c=IN IP4 135.180.130.88\n"
                };

	    for (int i = 0; i < connection.length; i++) {
	       ConnectionFieldParser connectionFieldParser=
                    new ConnectionFieldParser(
                            connection[i] );
		ConnectionField connectionField=connectionFieldParser.
						connectionField();
		System.out.println("toParse: " +connection[i]);
		System.out.println("encoded: " +connectionField.encode());
	    }

	}
**/



}
