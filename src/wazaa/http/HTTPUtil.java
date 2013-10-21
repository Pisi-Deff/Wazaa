package wazaa.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;

import wazaa.Machine;
import wazaa.Wazaa;
import wazaa.WazaaFoundFile;

public class HTTPUtil {
	public static String buildSearchFileCommand(
			Map<String, String> commandArgs) {
		StringBuilder s = new StringBuilder("searchfile?");
		try {
			s.append("name=" + 
					URLEncoder.encode(commandArgs.get("name"), "UTF-8"));
		} catch (UnsupportedEncodingException e1) { } 
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

	public static void sendSearchFileReqs(
			Map<String, String> commandArgs, boolean mySearch)
			throws UnknownHostException {
		String[] noaskMachines = null;
		if (commandArgs.containsKey("noask")
			&& !commandArgs.get("noask").isEmpty()) {
			noaskMachines = commandArgs.get("noask")
					.split("_");
		}
		for (Machine m : Wazaa.getMachines()) {
			if (Wazaa.isMyMachine(m)) {
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
				if (mySearch) {
					Socket s = new Socket();
					s.connect(new InetSocketAddress(
							m.getIP(), m.getPort()), 300);
					commandArgs.put("sendip", 
							s.getLocalAddress().getHostAddress());
					s.close();
				}
				new HTTPClient(m, "GET", 
						buildSearchFileCommand(commandArgs));
			} catch (IOException e) { }
		}
	}

	public static HTTPClient sendGetFileReq(WazaaFoundFile file)
			throws IOException {
		String filename = file.getFileName();
		try {
			filename = URLEncoder.encode(filename, "UTF-8");
			System.out.println("enc'd: " + filename);
		} catch (Throwable e) { }
		return new HTTPClient(file.getMachine(), "GET", 
				"getfile?fullname=" + filename);
	}
}
