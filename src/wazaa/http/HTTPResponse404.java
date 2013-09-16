package wazaa.http;

public class HTTPResponse404 extends HTTPResponse {
	public static final int RETURNCODE = 404;
	public static final String RETURNSTRING = "Not Found";
	public static final String CONTENTTYPE = "text/html";
	
	public HTTPResponse404() {
		super(RETURNCODE, RETURNSTRING, CONTENTTYPE, createBody());
	}
	
	public static String createBody() {
		return "1";
	}
}
