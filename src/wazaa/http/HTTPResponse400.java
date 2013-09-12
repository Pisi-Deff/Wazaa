package wazaa.http;

public class HTTPResponse400 extends HTTPResponse {
	public static final int RETURNCODE = 400;
	public static final String RETURNSTRING = "Bad Request";
	public static final String CONTENTTYPE = "text/html";
	
	public HTTPResponse400() {
		super(RETURNCODE, RETURNSTRING, CONTENTTYPE, createBody());
	}
	
	public static String createBody() {
		return "<html>\n"
				+ "<head>\n" +
				"<title>400 Bad Request</title>\n</head>\n"
				+ "<body>\n"
				+ "400 Bad Request\n"
				+ "<br /><br />\n"
				+ "Usage: http://host:port/command?args\n"
				+ "</body>\n"
				+ "</html>";
	}
}
