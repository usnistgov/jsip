/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import javax.sip.*;
import java.util.Locale;
import java.text.ParseException;

/**
* ContentLanguage header
* <pre>
*Fielding, et al.            Standards Track                   [Page 118]
*RFC 2616                        HTTP/1.1                       June 1999
*
*  14.12 Content-Language
*
*   The Content-Language entity-header field describes the natural
*   language(s) of the intended audience for the enclosed entity. Note
*   that this might not be equivalent to all the languages used within
*   the entity-body.
*
*       Content-Language  = "Content-Language" ":" 1#language-tag
*
*   Language tags are defined in section 3.10. The primary purpose of
*   Content-Language is to allow a user to identify and differentiate
*   entities according to the user's own preferred language. Thus, if the
*   body content is intended only for a Danish-literate audience, the
*   appropriate field is
*
*       Content-Language: da
*
*   If no Content-Language is specified, the default is that the content
*   is intended for all language audiences. This might mean that the
*   sender does not consider it to be specific to any natural language,
*   or that the sender does not know for which language it is intended.
*
*   Multiple languages MAY be listed for content that is intended for
*   multiple audiences. For example, a rendition of the "Treaty of
*   Waitangi," presented simultaneously in the original Maori and English
*   versions, would call for
*
*       Content-Language: mi, en
*
*   However, just because multiple languages are present within an entity
*   does not mean that it is intended for multiple linguistic audiences.
*   An example would be a beginner's language primer, such as "A First
*   Lesson in Latin," which is clearly intended to be used by an
*   English-literate audience. In this case, the Content-Language would
*   properly only include "en".
*
*   Content-Language MAY be applied to any media type -- it is not
*   limited to textual documents.
*</pre>
*/
public class ContentLanguage extends SIPHeader implements 
		javax.sip.header.ContentLanguageHeader{
	
        /** languageTag field.
         */    
	protected Locale locale;

	public ContentLanguage() {
            super (CONTENT_LANGUAGE);
        }

        /** Default constructor.
         * @param lang String to set
         */        
	public ContentLanguage( String languageTag ) {
		super (CONTENT_LANGUAGE);
		this.locale = new Locale(languageTag,Locale.US.getCountry());
	}
           
        /**
         * Canonical encoding of the  value of the header.
         * @return encoded body of header.
         */
	public String encodeBody() {
		return this.locale.getLanguage();
	}
        
        /** get the languageTag field.
         * @return String
         */        
	public String getLanguageTag () {
            return this.locale.getLanguage();
        }
	
        /** set the languageTag field
         * @param languageTag -- language tag to set.
         */        
	public void setLanguageTag( String languageTag) {
            this.locale = new Locale(languageTag,Locale.US.getCountry());
        }
        
        /**
         * Gets the language value of the ContentLanguageHeader.
         *
         *
         *
         * @return the Locale value of this ContentLanguageHeader
         *
         */
        public Locale getContentLanguage() {
            return locale;
        }        
        
        /**
         * Sets the language parameter of this ContentLanguageHeader.
         *
         *
         *
         * @param language - the new Locale value of the language of
         *
         * ContentLanguageHeader
         *
         */
        public void setContentLanguage(Locale language)  {
            this.locale = language;
        }        

}
