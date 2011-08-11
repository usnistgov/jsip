This example demonstrates the use of gov.nist.javax.sip.SIP_MESSAGE_VALVE property.

In summary the property specifies a class name that will be instantiated when the SIP stack is
started. Then for every request and response that arrive at the stack this instance will be called
giving you a chance to manipulate or drop the message.

In the example you can see that the regular processRequest and processResponse callbacks from
SipListener are not invoked because we are dropping them from the SipMessageValve's callbacks.

Additionally, we send error response 603 to all requests directly from the valve. This way we
avoid any of the overhead associated with transactions, dialogs, validation, etc. You can use
this feature to implement congestion control or DoS attack security.

In summary here are potential uses of the valve:
1. Congestion control - silent drop or error response
2. DoS attack prevention - because the valve is very early in the pipeline, no significant resources
are allocated for these messages.
3. Manipulate SIP message headers before reaching the application
4. Acquire locks in the early stage of the pipeline, so you can avoid race conditions or message
 re-ordering. Implementing message serialization/queuing based on locks.
5. Implement SIP logic that doesn't require statefulness. For example, you might have a full SIP
service, but some requests are simply a keepalive protocol. They don't really need strict transactions
or dialogs, thus you can make the response to the keepalives in the valve.