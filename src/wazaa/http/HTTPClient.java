package wazaa.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	
	private String response;
	private String responseFileName;
	private byte[] responseFileBytes;
	
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
		
		this.setName("HTTPClientThread");
		start();
	}
	
	@Override
	public void run() {
		try {
			System.out.println("HTTPClient attempting connection to <"
					+ url.toString() + ">");
			HttpURLConnection conn = 
					(HttpURLConnection) url.openConnection();
			conn.setReadTimeout(15000);
			conn.setRequestMethod(method);
			conn.setRequestProperty("User-Agent", 
					Wazaa.WAZAANAME + " v" + Wazaa.WAZAAVER);
			
			if (method.equals("POST") && postContent != null) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/json");
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(conn.getOutputStream()));
				out.write(postContent);
				out.flush();
				out.close();
				conn.getOutputStream().close();
			}
			
			conn.connect();
			
			System.out.println("HTTPClient made connection to <"
					+ url.toString() + ">: "
					+ conn.getResponseCode());
			
			String disposition = conn.getHeaderField("Content-Disposition");
			
			if (disposition != null) {
				// extracts file name from header field
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					responseFileName = disposition.substring(index + 10,
							disposition.length() - 1);
				}
			} else if (url.toString().matches("/getfile\\?")) {
				// extracts file name from URL
				int lastSlash = url.toString().lastIndexOf("/");
				int lastEquals = url.toString().lastIndexOf("=");
				int latter = 
						(lastSlash < lastEquals ? lastEquals : lastSlash);
				responseFileName = url.toString().substring(
						latter + 1,
						url.toString().length());
			}
			
			if (responseFileName != null) {
				ByteArrayOutputStream byteStream = 
						new ByteArrayOutputStream();
				InputStream in = conn.getInputStream();
				int bytesRead = -1;
				byte[] buffer = new byte[1024];
				while ((bytesRead = in.read(buffer)) != -1) {
					byteStream.write(buffer, 0, bytesRead);
				}
				responseFileBytes = byteStream.toByteArray();
				System.out.println("Received file: " + responseFileName);
			} else {
				BufferedReader in = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				response = "";
				String line;
				while ((line = in.readLine()) != null) { 
					response += line;
				}
				in.close();
				System.out.println("Response: " + response);
			}
		} catch (IOException e) {
			System.out.println("HTTPClient failed connection to <"
					+ url.toString() + ">");
		}
	}
	
	public String getResponse() {
		return response;
	}
	
	public String getResponseFileName() {
		return responseFileName;
	}
	
	public byte[] getResponseFileBytes() {
		return responseFileBytes;
	}
}
