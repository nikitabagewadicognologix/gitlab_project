
package example.app;

import static example.app.client.EchoClient.newEchoClient;
import static example.app.core.net.NetworkUtils.findAvailablePort;
import static example.app.server.EchoServer.newEchoServer;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import example.app.server.EchoServer;

/**
 * The EchoClientServerIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class EchoClientServerIntegrationTests {

	private static int port;

	private static final Boolean RUN_SERVER = Boolean.getBoolean("example.app.server.run");

	private static EchoServer echoServer;

	@BeforeClass
	public static void setup() {
		if (RUN_SERVER) {
			echoServer = newEchoServer(port = findAvailablePort()).runAndWaitFor();
		}
		else {
			port = Integer.getInteger("example.app.server.port");
		}
	}

	@AfterClass
	public static void tearDown() {
		if (RUN_SERVER && echoServer != null) {
			echoServer.shutdown();
		}
	}

	@Test
	public void clientServerCommunicationIsSuccessful() {
		assertThat(newEchoClient(port).sendMessage("Hello!")).isEqualTo("Hello!");
	}
}
