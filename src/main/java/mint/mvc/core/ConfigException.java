package mint.mvc.core;

/**
 * If any configuration is incorrect.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
class ConfigException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	ConfigException() {
    }

    ConfigException(String message) {
        super(message);
    }

    ConfigException(Throwable cause) {
        super(cause);
    }

    ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
