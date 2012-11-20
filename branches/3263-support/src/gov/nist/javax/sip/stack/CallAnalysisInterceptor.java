package gov.nist.javax.sip.stack;

import java.util.Properties;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.CallAnalyzer.MetricAnalysisConfiguration;
import gov.nist.javax.sip.stack.CallAnalyzer.MetricReference;

import javax.sip.SipStack;
import javax.sip.message.Message;

/**
 * This sample interceptor keeps track of requests stuck in JAIN SIP threads and prints a thread dump
 * when such event occurs periodically.
 * @author Vladimir Ralev
 *
 */
public class CallAnalysisInterceptor implements SIPEventInterceptor {

	private CallAnalyzer callAnalyzer;
	private static final MetricReference interceptorCheckpoint = new MetricReference("ick");
	
	public void afterMessage(Message message) {
		callAnalyzer.leave(interceptorCheckpoint);
		
	}

	public void beforeMessage(Message message) {
		callAnalyzer.enter(interceptorCheckpoint);
	}

	public void destroy() {
		callAnalyzer.stop();
		callAnalyzer = null;
		
	}
	public void init(SipStack stack) {
		callAnalyzer = new CallAnalyzer(((SipStackImpl) stack));
		Properties props = ((SipStackImpl) stack).getConfigurationProperties();
		Long checkingInterval = Long.parseLong(
				props.getProperty(CallAnalysisInterceptor.class.getName() + ".checkingInterval", "1000"));
		Long minStuckTime = Long.parseLong(
				props.getProperty(CallAnalysisInterceptor.class.getName() + ".minStuckTIme", "4000"));
		Long minTimeBetweenDumps = Long.parseLong(
				props.getProperty(CallAnalysisInterceptor.class.getName() + ".minTimeBetweenDumps", "2000"));
		MetricAnalysisConfiguration config = new MetricAnalysisConfiguration(
				checkingInterval, minTimeBetweenDumps, minStuckTime);
		callAnalyzer.configure(interceptorCheckpoint, config);
	}
	

}
