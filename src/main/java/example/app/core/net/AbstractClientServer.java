
package example.app.core.net;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AbstractClientServer class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractClientServer {

	protected static final long DEFAULT_DURATION_MILLISECONDS = TimeUnit.SECONDS.toMillis(15);
	protected static final long DEFAULT_INTERVAL_MILLISECONDS = 500L;

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected static boolean hasNotTimedOut(long timeout) {
		return (System.currentTimeMillis() < timeout);
	}

	protected static boolean hasTimedOut(long timeout) {
		return (System.currentTimeMillis() >= timeout);
	}

	protected static int parsePort(String[] args) {
		return parsePort(Arrays.asList(args));
	}

	protected static int parsePort(List<String> args) {
		assertThat(args.size()).describedAs("Usage:\n$java ... example.app.server.EchoServer <port>").isGreaterThan(0);
		return NetworkUtils.parsePort(args.get(0));
	}

	protected boolean isRunning(ServerSocket serverSocket) {
		return (serverSocket != null && !serverSocket.isClosed() && serverSocket.isBound());
	}

	protected PrintWriter newPrintWriter(Socket socket) throws IOException {
		return new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
	}

	protected BufferedReader newBufferedReader(Socket socket) throws IOException {
		return new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	protected String receiveMessage(Socket socket) throws IOException {
		return newBufferedReader(socket).readLine();
	}

	protected Socket sendMessage(Socket socket, String message) throws IOException {
		PrintWriter printWriter = newPrintWriter(socket);

		printWriter.println(message);
		printWriter.flush();

		return socket;
	}

	protected boolean waitFor(Condition condition) {
		return waitFor(condition, DEFAULT_DURATION_MILLISECONDS);
	}

	@SuppressWarnings("all")
	protected boolean waitFor(Condition condition, long duration) {
		assertThat(condition).describedAs("Condition must not be null").isNotNull();

		long timeout = (System.currentTimeMillis() + duration);

		try {
			while (hasNotTimedOut(timeout) && condition.evaluate()) {
				synchronized (condition) {
					TimeUnit.MILLISECONDS.timedWait(condition, DEFAULT_DURATION_MILLISECONDS);
				}
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		return !condition.evaluate();
	}

	protected interface Condition {
		boolean evaluate();
	}
}
