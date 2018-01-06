package torsete.util;
/**
 * Exceptions fra {@link GSDocument} som anvenderen af {@link GSDocument} ikke kan g�re noget for at afhj�lpe.
 * <p>
 * �rsagen kan afl�ses med {@link #getCause()}.
 */
public class GSDocumentException extends RuntimeException {
    GSDocumentException(Exception pException) {
        super(pException);
    }
}

