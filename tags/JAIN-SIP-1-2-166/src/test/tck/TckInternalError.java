package test.tck;

/**
 * This class serves to wrap exceptions that were thrown during the testing
 * and that were not provoked by the tested implementaion.
 *
 * @author Emil Ivov
 *         Network Research Team, Louis Pasteur University, Strasbourg, France
 *  This code is in the public domain.
 * @version 1.0
 */

public class TckInternalError extends Error
{

    public TckInternalError(String message)
    {
        super(message);
    TestHarness.abortOnFail = true;
    }

    public TckInternalError(String message, Throwable cause)
    {
        super(message, cause);
    TestHarness.abortOnFail = true;
    }

    public TckInternalError(Throwable cause)
    {
        super(cause);
    TestHarness.abortOnFail = true;
    }
}
