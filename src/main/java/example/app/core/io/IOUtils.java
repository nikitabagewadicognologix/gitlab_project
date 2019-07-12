
package example.app.core.io;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IOUtils class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class IOUtils {

	protected static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

	public static boolean close(Closeable closeable) {
		return close(closeable, "");
	}

	public static boolean close(Closeable closeable, String message, Object... args) {
		if (closeable != null) {
			try {
				closeable.close();
				return true;
			}
			catch (IOException ignore) {
				logger.warn(message, args);
			}
		}

		return false;
	}
}
