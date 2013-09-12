package wazaa.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer extends Thread {
	private int port;

	public HTTPServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		ServerSocket server_socket;
		try {
			server_socket = new ServerSocket(port);
			System.out.println("HTTPServer running on port "
					+ server_socket.getLocalPort());

			// server infinite loop
			while (true) {
				Socket socket = server_socket.accept();
				System.out.println("New connection accepted from: "
						+ socket.getInetAddress().getHostAddress() 
						+ ":" + socket.getPort());

				// Construct handler to process the HTTP request message.
				// and start it
				try {
					HTTPClientHandler request = new HTTPClientHandler(socket);
					request.start();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		} catch (IOException e) {
			System.out.println("Unable to launch server on port " + port + "!");
			System.out.println("Perhaps something is already running on the port.");
		}
	}
}
