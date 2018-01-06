package torsete.util;
/**
 * Exceptions fra {@link GSDocument} som anvenderen af {@link GSDocument} ikke kan gøre noget for at afhjælpe.
 * <p>
 * Årsagen kan aflæses med {@link #getCause()}.
 */
public class GSDocumentException extends RuntimeException {
    GSDocumentException(Exception pException) {
        super(pException);
    }
}

