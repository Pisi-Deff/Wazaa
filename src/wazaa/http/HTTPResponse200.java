package wazaa.http;

public class HTTPResponse200 extends HTTPResponse {
	public static final int RETURNCODE = 200;
	public static final String RETURNSTRING = "OK";
	
	public HTTPResponse200(String contentType, String body) {
		super(RETURNCODE, RETURNSTRING, contentType, body);
	}
	
	public HTTPResponse200(String contentType, byte[] body) {
		super(RETURNCODE, RETURNSTRING, contentType, body);
	}
}
