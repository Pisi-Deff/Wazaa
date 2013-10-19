package wazaa.http;

import java.util.HashMap;
import java.util.Map;

import wazaa.Wazaa;

public abstract class HTTPResponse {
	private int returnCode;
	private String returnString;
	
	private Map<String, String> extraHeaders = new HashMap<String, String>();
	
	private String contentType;
	private byte[] body;
	
	public static final String HTTPVER = "HTTP/1.0";
	
	public final static String CRLF = "\r\n";
	
	public HTTPResponse(
			int returnCode, String returnString,
			String contentType, String body) {
		this(returnCode, returnString, contentType, body.getBytes());
	}
	
	public HTTPResponse(
			int returnCode, String returnString,
			String contentType, byte[] body) {
		this.returnCode = returnCode;
		this.returnString = returnString;
		this.contentType = contentType;
		this.body = body;
	}
	
	public String getHeaders() {
		StringBuilder s = new StringBuilder();
		s.append(HTTPVER + " " + returnCode + " " + returnString + CRLF);
		s.append("Server: " + Wazaa.WAZAANAME + " v" + Wazaa.WAZAAVER + CRLF);
		s.append("Connection: close" + CRLF);
		s.append("Content-Type: " + contentType + CRLF);
		s.append("Content-Length: " + getBody().length + CRLF);
		
		for (String head : extraHeaders.keySet()) {
			s.append(head + ": " + extraHeaders.get(head) + CRLF);
		}
		
		s.append(CRLF);
		return s.toString();
	}
	
	public byte[] getBody() {
		return body;
	}
	
	public byte[] getResponse() {
		byte[] headerBytes = getHeaders().getBytes();
		byte[] resp;
		if (getBody().length > 0) {
			resp = new byte[headerBytes.length + getBody().length];
			System.arraycopy(headerBytes, 0, resp, 0, headerBytes.length);
			System.arraycopy(getBody(), 0, resp, headerBytes.length, 
					getBody().length);
		} else {
			resp = headerBytes;
		}
		return resp;
	}

	public void setHeader(String header, String value) {
		if (header != null && value != null) {
			extraHeaders.put(header, value);
		}
	}
}
