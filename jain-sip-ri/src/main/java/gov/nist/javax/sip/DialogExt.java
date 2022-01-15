package gov.nist.javax.sip;

import javax.sip.Dialog;
import javax.sip.SipProvider;

/**
 * Extensions for Next specification revision. These interfaces will remain unchanged and be
 * merged with the next revision of the spec.
 * 
 * 
 * @author mranga
 * 
 */
public interface DialogExt extends Dialog {

  /**
   * Returns the SipProvider that was used for the first transaction in this Dialog
   * 
   * @return SipProvider
   * 
   * @since 2.0
   */
  public SipProvider getSipProvider();

  /**
   * Sets a flag that indicates that this Dialog is part of a BackToBackUserAgent. If this flag
   * is set, INVITEs are not allowed to interleave and timed out ACK transmission results in a
   * BYE being sent to the other side. Setting this flag instructs the stack to automatically
   * handle dialog errors. Once this flag is set for a dialog, it cannot be changed.
   * This flag can be set on a stack-wide basis, on a per-provider basis or on a per Dialog basis.
   * This flag must only be set at the time of Dialog creation. If the flag is set after the first
   * request or response is seen by the Dialog, the behavior of this flag is undefined.
   * 
   * @since 2.0
   */
  public void setBackToBackUserAgent();

  /**
   * Turn off sequence number validation for this dialog. This passes all requests to the
   * application layer including those that arrive out of order. This is good for testing
   * purposes. Validation is delegated to the application and the stack will not attempt to
   * block requests arriving out of sequence from reaching the application. In particular, the
   * validation of CSeq and the ACK retransmission recognition are delegated to the application.
   * Your application will be responsible for error handling of these cases.
   * 
   * @since 2.0
   */
  public void disableSequenceNumberValidation();

  /**
   * retrieve the value of release references to know if the stack performs optimizations
   * on cleanup to save on memory
   * 
   * @return release references value
   * 
   * @since 2.0
   */
  public boolean isReleaseReferences();

  /**
   * If set to true it will release all references that it no longer needs. This will include the
   * reference to the
   * Request, Transactions, Any unused timers etc. This will significantly reduce memory
   * consumption under high load
   * 
   * @param releaseReferences
   * 
   * @since 2.0
   */
  public void setReleaseReferences(boolean releaseReferences);

  /**
   * Sets the early dialog timeout period. Overrides the value set by the stack configuration
   * property
   * EARLY_DIALOG_TIMEOUT_SECONDS.
   * A dialog may remain in early state indefinitely. UACs may kill a dialog in early state of
   * periodic
   * provisional responses are not seen for 3 minutes. This allows you to override the RFC
   * specified value of 3 minutes hence allowing for fast fail over from unresponsive servers.
   */
  public void setEarlyDialogTimeoutSeconds(int timeoutValue);

  /**
   * True if this dialog has been created following the forking of a previous dialog.
   * This could happen by example if Alice would Invite a forking proxy whom would 180Ring Bob but
   * finally 200Ok on Carol. The original Dialog known by Alice after Bob's ringing would be overriden
   * by a new dialog bound to Carol. The original dialog can be retrieved using {@link #getOriginalDialog()}
   */
  public boolean isForked();

  /**
   * @see {@link #isForked()}
   * 
   * @return The original dialog.
   */
  public Dialog getOriginalDialog();
}
