package test.tck;

/**
 * This class serves to wrap exceptions that were thrown by the Tested 
 * Implementation and that do not reflect a test failure but rather
 * an error that prevented the TCK from performing a test.
 *
 * @author Emil Ivov
 * @version 1.0
 */

public class TiUnexpectedError extends Error
{

    public TiUnexpectedError(String message)
    {
        super(message);
    }

    public TiUnexpectedError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TiUnexpectedError(Throwable cause)
    {
        super(cause);
    }
}
