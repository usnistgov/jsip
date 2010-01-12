package gov.nist.javax.sip.header.extensions;

import gov.nist.javax.sip.header.ParametersHeader;

import java.text.ParseException;
import java.util.Iterator;

import javax.sip.header.ExtensionHeader;

public class References extends ParametersHeader  implements ReferencesHeader,ExtensionHeader  {

    private static final long serialVersionUID = 8536961681006637622L;
    
    
    private String callId;
    
    public References() {
        super(ReferencesHeader.NAME);
    }
  
    @Override
    public String getBranch() {
        return this.getParameter(BRANCH);
    }

    @Override
    public String getCallId() {
       return callId;
    }

    @Override
    public String getMethod() {
        return this.getParameter(METHOD);
    }

    @Override
    public String getRel() {
        return this.getParameter(REL);
    }

    @Override
    public void setBranch(String branch) throws ParseException {
       this.setParameter(BRANCH,branch);
    }

    @Override
    public void setCallId(String callId) {
        this.callId = callId;
    }

    @Override
    public void setMethod(String method) throws ParseException {
        this.setParameter(METHOD,method);
    }

    @Override
    public void setRel(String rel) throws ParseException{
      this.setParameter(REL,rel);
    }

    @Override
    public String getParameter(String name) {
        return super.getParameter(name);
    }

    @Override
    public Iterator getParameterNames() {
        return super.getParameterNames();
    }

    @Override
    public void removeParameter(String name) {
       super.removeParameter(name);
    }

    @Override
    public void setParameter(String name, String value) throws ParseException {
       super.setParameter(name,value); 
    }

    @Override
    public String getName() {
        return ReferencesHeader.NAME;
    }

    @Override
    protected String encodeBody() {
        if ( super.parameters.isEmpty()) {
            return callId ;
        } else {
            return callId + ";" + super.parameters.encode();
        }
    }

    @Override
    public void setValue(String value) throws ParseException {
        throw new UnsupportedOperationException("operation not supported");
    }

}
