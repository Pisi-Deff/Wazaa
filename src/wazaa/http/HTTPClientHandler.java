package wazaa.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import wazaa.FileIOUtil;
import wazaa.Machine;
import wazaa.Wazaa;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;

public class HTTPClientHandler extends Thread {
	public final static String CRLF = "\r\n";

	private Socket socket;
	private OutputStream output;
	private BufferedReader br;

	public HTTPClientHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.output = socket.getOutputStream();
		this.br = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
	}

	public void run() {
		try {
			processRequest();
		} catch (IOException e) {
			System.out.println("Connection to client terminated: "
					+ socket.getInetAddress().getHostAddress()
					+ ":" + socket.getPort());
		}
	}

	private void processRequest() throws IOException {
		while (true) {
			String headerLine = br.readLine();
			if (headerLine == null) {
				break;
			} else if (headerLine.isEmpty()) {
				continue;
			}
//			System.out.println(headerLine);

			StringTokenizer s = new StringTokenizer(headerLine);
			String headerTok = s.nextToken();

			if (headerTok.equalsIgnoreCase("GET")
					|| headerTok.equalsIgnoreCase("POST")) {
				String commandBits[] = s.nextToken().split("\\?");
				String command = commandBits[0];
				
				Map<String, String> commandArgs = null;
				if (commandBits.length > 1) {
					commandArgs = getCommandArgsFromString(commandBits[1]);
				}

				HTTPResponse resp = null;
				
				if (command.startsWith("/searchfile")) {
					resp = doSearchFile(commandArgs);
				} else if (command.startsWith("/getfile")) {
					resp = doGetFile(commandArgs);
				} else if (command.startsWith("/foundfile")
						&& headerTok.equalsIgnoreCase("POST")) {
					resp = doFoundFile();
				}
				
				if (resp == null) {
					resp = new HTTPResponse404();
				}

				if (resp != null && !socket.isClosed()) {
					output.write(resp.getResponse());
				}
			}
		}

		try {
			output.close();
			br.close();
			socket.close();
		} catch (IOException e) { }
		System.out.println("Connection to client closed: "
				+ socket.getInetAddress().getHostAddress()
				+ ":" + socket.getPort());
	}

	private HTTPResponse doFoundFile() throws IOException {
		/*
		 * {
		 *   "id": "wqeqwe23",
		 *	 "files":
		 *   [
		 *    {"ip":"11.22.33.66", "port":"5678", "name":"minufail1.txt"},
		 *    {"ip":"11.22.33.68", "port":"5678", "name":"xxfail1yy.txt"}
		 *   ]
		 * }
		 */
		/*
		 * Postituse saaja vastab eduka kättesaamise korral arvu 0,
		 * arusaamatuste korral mõne muu (vea) numbri.
		 */
		String line = null;
		boolean foundData = false;
		String data = "";
		while ((line = br.readLine()) != null) {
			if (foundData) {
				data += line + CRLF;
			} else if (line.equals("")) {
				foundData = true;
			}
		}

		String answer = "0";
		if (!data.isEmpty()) {
			JsonObject json = null;
			try {
				json = JsonObject.readFrom(data);
				//TODO: handle data
			} catch (ParseException | 
					UnsupportedOperationException e) {
				answer = "1";
			}
		}

		return new HTTPResponse200(
				"text/plain", 
				answer);
	}

	private static HTTPResponse doGetFile(Map<String, String> commandArgs) {
		/*
		 * fullname=fullfilename
		 */
		HTTPResponse resp = null;
		if (commandArgs != null
				&& commandArgs.containsKey("fullname")) {
			String fileName = commandArgs.get("fullname");
			if (fileName != null) {
				try {
					resp = new HTTPResponse200(
							FileIOUtil.getFileContentType(fileName),
							FileIOUtil.getFileBytes(fileName));
					String[] fileNameParts = 
							fileName.split("[/\\\\]");
					String fileNameShort = 
							fileNameParts[fileNameParts.length - 1];
					fileNameShort = 
							URLEncoder.encode(
									fileNameShort, "UTF-8");
					resp.setHeader(
							"Content-Disposition",
							"attachment; filename=\""
									+ fileNameShort + "\"");
				} catch (IOException | InvalidPathException e) { }
			}
		}
		return resp;
	}

	private static HTTPResponse doSearchFile(Map<String, String> commandArgs)
			throws IOException {
		/*
		 * name: otsitav string (peab olema failinimes)
		 * sendip: esialgse otsija ip
		 * sendport: esialgse otsija port
		 * ttl: kui mitu otsingu-hopi veel teha 
		 * 		(iga edasiküsimine vähendab ühe võrra)
		 * id: optsionaalne päringu identifikaator
		 * noask: optsionaalne list masinatest, kellelt 
		 * 			pole mõtet küsida (eraldajaks alakriips) 
		 */
		String answer = "1";
		if (commandArgs.containsKey("name") 
				&& commandArgs.containsKey("sendip")
				&& commandArgs.containsKey("sendport")
				&& commandArgs.containsKey("ttl")) {
			try {
				String name = commandArgs.get("name");
				InetAddress sendip = 
						InetAddress.getByName(
								commandArgs.get("sendip"));
				int sendport = 
						Integer.parseInt(
								commandArgs.get("sendport"));
				int ttl = 
						Integer.parseInt(
								commandArgs.get("ttl"));
				if (!name.isEmpty()
						&& sendport > 0 && sendport < 65535
						&& ttl >= 0) {
					if (ttl > 1) {
						String[] noaskMachines = null;
						if (commandArgs.containsKey("noask")
							&& !commandArgs.get("noask").isEmpty()) {
							noaskMachines = commandArgs.get("noask")
									.split("_");
						}
						synchronized (Wazaa.class) {
							Iterator<Machine> iter = 
									Wazaa.getMachines().iterator();
							while (iter.hasNext()) {
								Machine m = iter.next();
								
								if (noaskMachines != null) {
									for (String s : noaskMachines) {
										String[] noaskM = s.split(":");
										if (noaskM.length == 2
												&& noaskM[0].equals(
														m.getIP().getHostAddress())
												&& noaskM[1].equals(
														String.valueOf(
																m.getPort()))
												) {
											continue;
										}
									}
								}
								
								HTTPClient c = new HTTPClient(
										m, "GET",
										buildSearchFileCommand(
												commandArgs));
								c.start();
							}
						}
					}
					// TODO: handle optional id arg
					ArrayList<String> foundFiles = 
							FileIOUtil.findFiles(name);
					if (foundFiles != null && !foundFiles.isEmpty()) {
						// only send foundfile is there are actually files found
						Machine m = new Machine(sendip, sendport);
						JsonObject json = 
								buildFoundFileJson(foundFiles, commandArgs);
						HTTPClient c = new HTTPClient(
								m, "POST", "foundfile",
								json.toString());
						c.start();
					}
					
					answer = "0";
				}
			} catch (NumberFormatException
					| UnknownHostException e) { }
		}
		return new HTTPResponse200("text/plain", answer);
	}
	
	private static JsonObject buildFoundFileJson(
			ArrayList<String> foundFiles, Map<String, String> commandArgs) {
//		{ "id": "wqeqwe23",
//			  "files":
//			  [ 
//			    {"ip":"11.22.33.66", "port":"5678", "name":"minufail1.txt"},
//			    ...
//			    {"ip":"11.22.33.68", "port":"5678", "name":"xxfail1yy.txt"}
//			  ]
//		}
		JsonObject json = new JsonObject();
		if (commandArgs.containsKey("id")) {
			json.add("id", commandArgs.get("id"));
		}
		JsonArray files = new JsonArray();
		
		for (String fileName : foundFiles) {
			JsonObject file = new JsonObject();
			file.add("ip", ""); // TODO
			file.add("port", "");
			file.add("name", fileName);
		}
		
		json.add("files", files);
		return json;
	}

	private static String buildSearchFileCommand(
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
		if (commandArgs.containsKey("noask") 
				&& !commandArgs.get("noask").isEmpty()) {
			s.append(commandArgs.get("noask") + "_");
		}
		s.append("127.0.0.1:12345"); // TODO: own ip and port
		return s.toString();
	}

	public static Map<String, String> getCommandArgsFromString(String s) {
		Map<String, String> commandArgs = new HashMap<String, String>();
		String args[] = s.split("&");
		for (String a : args) {
			if (a.contains("=")) {
				String[] arg = a.split("=");
				commandArgs.put(arg[0], arg[1]);
			} else {
				commandArgs.put(a, null);
			}
		}
		return commandArgs;
	}
}
