package wazaa.http;

public class HTTPResponse404 extends HTTPResponse {
	public static final int RETURNCODE = 404;
	public static final String RETURNSTRING = "Not Found";
	public static final String CONTENTTYPE = "text/html";
	
	public HTTPResponse404() {
		super(RETURNCODE, RETURNSTRING, CONTENTTYPE, createBody());
	}
	
	public static String createBody() {
		return "<html>\n"
				+ "<head>\n" +
				"<title>404 Not Found</title>\n</head>\n"
				+ "<body>\n"
				+ "404 Not Found\n"
				+ "<br /><br />\n"
				+ "Usage: http://host:port/command?args\n"
				+ "</body>\n"
				+ "</html>";
	}
}
