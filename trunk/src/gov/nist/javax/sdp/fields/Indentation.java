/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
/**
* Internal utility class for pretty printing and header formatting.
*/
class Indentation {
	private int indentation;
	protected Indentation() { indentation = 0; }
	protected Indentation( int initval) { indentation = initval; }
	protected void setIndentation( int initval ) { indentation = initval; }
	protected int  getCount()  { return indentation; }
	protected void increment() { indentation ++ ; }
	protected void decrement() { indentation --;  }
	protected String getIndentation() {
		String retval = "";
		for (int i = 0; i < indentation; i++) retval += " ";
		return retval;
	}
}
