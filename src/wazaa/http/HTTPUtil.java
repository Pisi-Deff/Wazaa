package wazaa.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import wazaa.Wazaa;

public class HTTPUtil {
	public static String buildSearchFileCommand(
			Map<String, String> commandArgs) {
		StringBuilder s = new StringBuilder("searchfile?");
		s.append("name=" + commandArgs.get("name")); 
		s.append("&sendip=" + commandArgs.get("sendip"));
		s.append("&sendport=" + commandArgs.get("sendport"));
		Integer ttl = Integer.parseInt(commandArgs.get("ttl"));
		s.append("&ttl=" + (ttl - 1));
		if (commandArgs.containsKey("id")) {
			s.append("&id=" + commandArgs.get("id"));
		}
		s.append("&noask=");
		String myMachine = "";
		try {
			myMachine = InetAddress.getLocalHost().getHostAddress()
					+ ":" +
					Wazaa.getHTTPServer().getPort();
		} catch (UnknownHostException e) { }
		boolean addMyMachine = true;
		if (commandArgs.containsKey("noask") 
				&& !commandArgs.get("noask").isEmpty()) {
			s.append(commandArgs.get("noask"));
			if (commandArgs.get("noask").contains(myMachine)) {
				addMyMachine = false;
			} else {
				s.append("_");
			}
		}
		if (addMyMachine) {
			s.append(myMachine);
		}
		return s.toString();
	}
}
