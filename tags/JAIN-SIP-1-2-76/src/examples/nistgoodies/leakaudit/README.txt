This example demonstrates how an application can monitor the SIP Stack for leaked dialogs and transactions.

By leaked dialogs and transactions we mean dialogs and transactions that are no longer known by the
application but still exits within the stack for a very long time (much longer than any lingering
timer). These leaks will typically indicate bugs in the application (e.g., error conditions not properly
handled, a dialog not correctly released or abandoned, etc.), but can also indicate hidden bugs in the stack.

This is how it works: once in a while the application presents the stack with a list of active
call IDs for all the dialogs it knows about. The stack compares this list with its own list of dialogs,
tagging each suspect dialog along the way. Because dialogs are allowed lo linger long after the application
is done with them, the application must gives the stack an idea of how long a suspect dialog is allowed
to stay around before being deemed as leaked. Usually this timer value is in order of minutes and longer
than lingering timer values.

The leak audit does a similar job with client and server transactions. During each audit the stack
tags each transaction with a timestamp. If these transactions stay around longer than the transaction
timer values specified by the application, they too are released and removed from memory. The transaction
timer supplied by the application is usually shorter than the dialog timer (in the order of seconds).

At the end of each audit, the stack returns a string report containing a list of leaked dialogs
and transactions found and released.

The leak auditor doesn't have to enabled or disabled. It is a static service provided by the
SIPTransactionStack and it is up to the application to use it.

To use the leak auditor, the application must run a timer or a separate thread to periodically call the
following method:

		String auditReport = ((SipStackImpl) sipStack).auditStack(activeCallIDs,
		                                                          dialogTimerInMs,
                                                                  transactionTimerInMs);

		Suggested values:
		   dialogTimerInMs = 30 minutes (30 * 60 * 1000 millis)
		   transactionTimerInMs = 60 seconds (60 * 1000 millis)

The audit will return a null string if everything is fine (i.e., no leaks). Otherwise the report will
look like this:

  Leaked dialogs:
    dialog id: 68c069a12df10fdad557ad7c42375998@10.0.51.32:ext107@10.0.51.32:5552:ext107@10.0.51.32:974f7cdfe8ddc74, dialog state: Confirmed Dialog
    dialog id: b967d359e1eae42a0a879a80c4a815a5@10.0.51.32:ext109@10.0.51.32:1472:ext109@10.0.51.32:488ca9a41b52ce8, dialog state: Confirmed Dialog
    dialog id: 7f8cd1a754af3b0934a09683650358cb@10.0.51.32:port108@10.0.51.32:2189:port108@10.0.51.32:8dc7caa712db4c9, dialog state: Confirmed Dialog
    dialog id: a3f363e65deb6f495d4c9b14f746421e@10.0.51.32:port106@10.0.51.32:2679:port106@10.0.51.32:b122af94102b8cb, dialog state: Confirmed Dialog
    dialog id: 96cf914dae029087265756d8d3e7e542@10.0.51.32:port108@10.0.51.32:7311:port108@10.0.51.32:3ef9ff24275da08, dialog state: Confirmed Dialog
    dialog id: 3b434b8628df46bda9e63e41d024690b@10.0.51.32:port106@10.0.51.32:2692:port106@10.0.51.32:7c0e84e714e00c3, dialog state: Confirmed Dialog
    dialog id: c594b4c785c3d7fc9c770d65c06369a1@10.0.51.32:port108@10.0.51.32:7874:port108@10.0.51.32:aea32ce5a8a5045, dialog state: Confirmed Dialog
    dialog id: c429b4e98bcbb3251949e89089ae974b@10.0.51.32:port106@10.0.51.32:3318:port106@10.0.51.32:5c82e9ea3300187, dialog state: Confirmed Dialog
    Total: 8 leaked dialogs detected and removed.
  Leaked transactions:
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    gov.nist.javax.sip.stack.SIPServerTransaction, state: Trying Transaction, OR: SUBSCRIBE
    Total: 10 leaked transactions detected and removed.


Hope you find it useful.

Ricardo Borba
(Natural Convergence)