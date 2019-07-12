
package example.app.client;

import static example.app.core.io.IOUtils.close;
import static example.app.core.net.NetworkUtils.newSocket;

import java.io.IOException;
import java.net.Socket;

import example.app.core.net.AbstractClientServer;

/**
 * The EchoClient class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class EchoClient extends AbstractClientServer {

	protected static final String DEFAULT_HOST = null;

	public static void main(String[] args) {
		EchoClient echoClient = newEchoClient(parsePort(args));

		for (int index = 1; index < args.length; index++) {
			System.out.println(echoClient.sendMessage(args[index]));
		}
	}

	public static EchoClient newEchoClient(int port) {
		return newEchoClient(DEFAULT_HOST, port);
	}

	public static EchoClient newEchoClient(String host, int port) {
		return new EchoClient(host, port);
	}

	private final int port;

	private final String host;

	public EchoClient(int port) {
		this(DEFAULT_HOST, port);
	}

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	protected String getHost() {
		return this.host;
	}

	protected int getPort() {
		return this.port;
	}

	public String sendMessage(String message) {
		Socket socket = null;

		try {
			socket = newSocket(getHost(), getPort());
			return receiveResponse(sendMessage(socket, message));
		}
		finally {
			close(socket);
		}
	}

	protected Socket sendMessage(Socket socket, String message) {
		try {
			return super.sendMessage(socket, message);
		}
		catch (IOException e) {
			logger.error("Failed to send message [{}] to echo server [{}]", message, socket.getRemoteSocketAddress());
			logger.error("", e);
			return socket;
		}
	}

	protected String receiveResponse(Socket socket) {
		try {
			return super.receiveMessage(socket);
		}
		catch (IOException e) {
			logger.error("Failed to receive response from echo server [{}]", socket.getRemoteSocketAddress());
			logger.error("", e);
			return "No Reply";
		}
	}
}
