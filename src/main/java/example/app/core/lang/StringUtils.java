
package example.app.core.lang;

/**
 * The StringUtils class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class StringUtils {

	public static String defaultIfUnspecified(String value, String defaultValue) {
		return (value != null && !value.trim().isEmpty() ? value : defaultValue);
	}
}
