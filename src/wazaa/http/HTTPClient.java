package wazaa.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import wazaa.Machine;
import wazaa.Wazaa;

/**
 * Class for sending HTTP requests to other Wazaas.
 * Used to propagate searchfile requests, send foundfiles, get files.
 * @author Eerik
 */
public class HTTPClient extends Thread {
	private String method;
	private String postContent;
	
	private String response = null;
	
	private URL url;
	
	public HTTPClient(Machine machine, String method, String command)
			throws IOException {
		this(machine, method, command, null);
	}
	
	public HTTPClient(Machine machine, String method,
			String command, String postContent)
			throws IOException {
		this.method = method.toUpperCase();
		if (this.method.equals("POST")) {
			if (postContent == null) {
				throw new IllegalArgumentException();
			} else {
				this.postContent = postContent;
			}
		}
		
		this.url = new URL("http://" + machine.toString()
				+ "/" + command);
	}
	
	@Override
	public void run() {
		try {
			HttpURLConnection conn = 
					(HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.setRequestProperty("User-Agent", 
					Wazaa.WAZAANAME + " v" + Wazaa.WAZAAVER);
			if (method.equals("POST") && postContent != null) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/json");
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(conn.getOutputStream()));
				out.write(postContent);
				out.close();
			}
			conn.connect();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			response = "";
			while ((line = in.readLine()) != null) { 
				response += line;
			}
			System.out.println("HTTPClient made connection to <"
					+ url.toString() + ">: "
					+ " (" + conn.getResponseCode() + ") "
					+ response);
			in.close();
		} catch (IOException e) { }
	}
	
	public String getResponse() {
		return response;
	}
}
