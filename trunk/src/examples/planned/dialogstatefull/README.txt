
Planned features for the next version.
-------------------------------------

This code fragment is provided for your feedback.

JAIN-SIP 1.1 forces you to be either dialog stateful or stateless.
A new stack configuration property javax.sip.AUTOMATIC_DIALOG_SUPPORT
is added. When set to false, the stack will not automatically create
a dialog for you. Your application has to create it by calling a new
provider method sipProvider.createDialog(Transaction).
