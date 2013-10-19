package wazaa.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer extends Thread {
	private int port;
	private ServerSocket serverSocket;

	public HTTPServer(int port) throws IOException {
		this.port = port;
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("HTTPServer running on port "
					+ serverSocket.getLocalPort());
		} catch (IOException e) {
			System.out.println("Unable to launch server on port " + port + "!");
			System.out.println("Perhaps something is already running on the port.");
			throw new IOException();
		}

		start();
	}

	@Override
	public void run() {
		// server infinite loop
		try {
			while (true) {
				Socket socket = serverSocket.accept();
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
			System.out.println("Server error.");
		}
	}
	
	public int getPort() {
		return this.port;
	}
}
