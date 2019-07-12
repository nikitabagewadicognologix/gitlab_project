
package example.app.server;

import static example.app.core.io.IOUtils.close;
import static example.app.core.net.NetworkUtils.newServerSocket;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import example.app.core.net.AbstractClientServer;

/**
 * The EchoServer class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class EchoServer extends AbstractClientServer implements Runnable {

	protected static final int DEFAULT_EXECUTOR_SERVICE_THREAD_COUNT = 10;

	public static void main(String[] args) {
		newEchoServer(args).run();
	}

	public static EchoServer newEchoServer(String[] args) {
		return newEchoServer(parsePort(args));
	}

	public static EchoServer newEchoServer(int port) {
		return new EchoServer(port);
	}

	private final int port;

	private ExecutorService echoService;

	private final ServerSocket serverSocket;

	public EchoServer(int port) {
		assertThat(port).describedAs("Port [%d] must be greater than 1024 and less than 65536", port)
			.isGreaterThan(1024).isLessThan(65536);

		this.port = port;
		this.serverSocket = newServerSocket(port);

		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
	}

	public boolean isNotRunning() {
		return !isRunning();
	}

	public boolean isRunning() {
		return isRunning(getServerSocket());
	}

	public int getPort() {
		return this.port;
	}

	protected ServerSocket getServerSocket() {
		return this.serverSocket;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void run() {
		logger.info("Starting EchoServer on port {}...", getPort());
		runEchoService(getServerSocket());
	}

	public EchoServer runAndWaitFor() {
		return runAndWaitFor(DEFAULT_DURATION_MILLISECONDS);
	}

	public EchoServer runAndWaitFor(long duration) {
		run();
		waitFor(duration);
		return this;
	}

	protected void runEchoService(ServerSocket serverSocket) {
		if (isRunning(serverSocket)) {
			echoService = Executors.newFixedThreadPool(10);

			echoService.submit(() -> {
				try {
					while (isRunning(serverSocket)) {
						Socket echoClient = serverSocket.accept();

						logger.info("EchoClient connected from {}", echoClient.getRemoteSocketAddress());

						echoService.submit(() -> {
							sendResponse(echoClient, receiveMessage(echoClient));
							close(echoClient);
						});
					}
				}
				catch (IOException e) {
					if (isRunning()) {
						logger.warn("An IO error occurred while listening for echo clients", e);
					}
				}
			});

			logger.info("EchoServer running on port {}", getPort());
		}
	}

	protected String receiveMessage(Socket socket) {
		BufferedReader echoClientReader = null;

		try {
			String message = super.receiveMessage(socket);
			logger.info("Received message [{}] from echo client [{}]", message, socket.getRemoteSocketAddress());
			return message;
		}
		catch (IOException e) {
			logger.error("Failed to receive message from echo client [{}]", socket.getRemoteSocketAddress());
			logger.debug("", e);
			return "What?";
		}
	}

	protected void sendResponse(Socket socket, String message) {
		try {
			logger.info("Sending response [{}] to echo client [{}]", message, socket.getRemoteSocketAddress());
			sendMessage(socket, message);
		}
		catch (IOException e) {
			logger.error("Failed to send response [{}] to echo client [{}]", message, socket.getRemoteSocketAddress());
			logger.debug("", e);
		}
	}

	public void shutdown() {
		logger.info("Stopping EchoServer...");

		closeServerSocket();
		stopEchoService();

		logger.info("EchoServer stopped");
	}

	protected void closeServerSocket() {
		ServerSocket serverSocket = getServerSocket();

		close(serverSocket, "Failed to close ServerSocket bound to address [{}], listening on port [{}]",
			serverSocket.getInetAddress(), serverSocket.getLocalPort());
	}

	protected void stopEchoService() {
		if (this.echoService != null) {
			this.echoService.shutdown();

			try {
				if (!this.echoService.awaitTermination(30, TimeUnit.SECONDS)) {
					this.echoService.shutdownNow();

					if (!this.echoService.awaitTermination(30, TimeUnit.SECONDS)) {
						this.logger.warn("Failed to shutdown EchoService");
					}
				}
			}
			catch (InterruptedException ignore) {
				this.echoService.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	public boolean waitFor() {
		return waitFor(DEFAULT_DURATION_MILLISECONDS);
	}

	public boolean waitFor(long duration) {
		return waitFor(this::isNotRunning, duration);
	}
}
