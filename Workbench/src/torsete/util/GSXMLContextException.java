package torsete.util;

/**
 * Exceptions fra {@link GSXMLContext} som anvenderen af {@link GSXMLContext} ikke kan g�re noget for at afhj�lpe.
 * <p>
 * �rsagen kan afl�ses med {@link #getCause()}.
 */
public class GSXMLContextException extends RuntimeException {
    GSXMLContextException(Exception pException) {
        super(pException);
    }
}
