package wazaa.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import wazaa.Machine;
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

	public static void sendSearchFileReqs(Map<String, String> commandArgs)
			throws UnknownHostException {
		String command = buildSearchFileCommand(commandArgs);
		String[] noaskMachines = null;
		if (commandArgs.containsKey("noask")
			&& !commandArgs.get("noask").isEmpty()) {
			noaskMachines = commandArgs.get("noask")
					.split("_");
		}
		for (Machine m : Wazaa.getMachines()) {
			if (m.getIP().getHostAddress().equals(
					InetAddress.
					getLocalHost().getHostAddress()) &&
				m.getPort() == 
				Wazaa.getHTTPServer().getPort()) {
				continue;
			} else if (noaskMachines != null) {
				boolean isNoAsk = false;
				for (String s : noaskMachines) {
					String[] noaskM = s.split(":");
					if (noaskM.length == 2
							&& noaskM[0].equals(
									m.getIP().getHostAddress())
							&& noaskM[1].equals(
									String.valueOf(
											m.getPort()))
							) {
						isNoAsk = true;
					}
				}
				if (isNoAsk) {
					continue;
				}
			}
			try {
				new HTTPClient(m, "GET", command);
			} catch (IOException e) { }
		}
	}
}
