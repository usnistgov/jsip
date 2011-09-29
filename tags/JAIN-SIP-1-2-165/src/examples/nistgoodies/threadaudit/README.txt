This example demonstrates how an application can monitor the health of the internal threads of
the SIP Stack.

The SIP Stack implements an internal thread auditor that allows applications to detect catastrophic
failures like an internal thread terminating because of an exception or getting stuck in a deadlock
condition. Events like these will make the stack inoperable and therefore require immediate action
from the application layer (e.g., alarms, traps, reboot, failover, etc.)

The threads currently monitored by the Thread Auditor are:
	- EventScannerThread
	- SIPTransactionStack Global Timer Thread
	- UDPMessageProcessorThread
	- UDPMessageChannel

(TCP and TLS threads are currently not monitored)

The thread auditor is disabled by default. To enable it, the application must define the
following system property:

	gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS

This property defines how often the application intends to audit the SIP Stack (i.e., the time in
miliseconds between successive audits).

The application must run a timer or a separate thread to periodically audit the stack by calling
the following method:

		String auditReport = ((SipStackImpl) sipStack).getThreadAuditor().auditThreads();

The audit will return a null string if everything is fine (i.e., if all threads appear to be healthy).
Otherwise the returned string will contain a report like this:

		Thread Auditor Report:
      	   Thread [UDPMessageProcessorThread] has failed to respond to an audit request.

