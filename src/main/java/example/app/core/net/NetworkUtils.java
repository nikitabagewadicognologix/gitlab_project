
package example.app.core.net;

import static example.app.core.io.IOUtils.close;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * The NetworkUtils class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class NetworkUtils {

	public static final int DEFAULT_PORT = 11235;

	public static final long DEFAULT_SO_TIMEOUT = TimeUnit.SECONDS.toMillis(15);

	public static int findAvailablePort() {
		return findAvailablePort(DEFAULT_PORT);
	}

	public static int findAvailablePort(int defaultPort) {
		try {
			ServerSocket serverSocket = new ServerSocket(0);
			int port = serverSocket.getLocalPort();
			close(serverSocket);
			return port;
		}
		catch (IOException ignore) {
			return defaultPort;
		}
	}

	private static int intValue(Number number) {
		return (number != null ? number.intValue() : 0);
	}

	public static ServerSocket newServerSocket(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			return serverSocket;
		}
		catch (IOException e) {
			throw new RuntimeException(String.format("Failed to create a ServerSocket on port [%d]", port), e);
		}
	}

	public static Socket newSocket(String host, int port) {
		try {
			Socket socket = new Socket(host, port);
			socket.setReuseAddress(true);
			socket.setSoTimeout(intValue(DEFAULT_SO_TIMEOUT));
			return socket;
		}
		catch (IOException e) {
			throw new RuntimeException(String.format(
				"Failed to create a client Socket on host [%s] and port [%d]", host, port));
		}
	}

	public static int parsePort(String port) {
		try {
			assertThat(port).describedAs("Port must be specified").isNotNull();
			return Integer.parseInt(port.trim());
		}
		catch (Throwable t) {
			throw new IllegalArgumentException(String.format("Port [%s] is not valid", port), t);
		}
	}
}
