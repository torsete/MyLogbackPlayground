package torsete.util;

/**
 * Exceptions fra {@link GSXMLContext} som anvenderen af {@link GSXMLContext} ikke kan gøre noget for at afhjælpe.
 * <p>
 * Årsagen kan aflæses med {@link #getCause()}.
 */
public class GSXMLContextException extends RuntimeException {
    GSXMLContextException(Exception pException) {
        super(pException);
    }
}
