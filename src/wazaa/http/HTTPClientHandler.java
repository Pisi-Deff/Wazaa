package wazaa.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import wazaa.FileIOUtil;
import wazaa.Machine;
import wazaa.Wazaa;
import wazaa.WazaaFile;
import wazaa.WazaaFoundFile;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
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
				System.out.println("Request from <" + 
						socket.getInetAddress().getHostAddress() +
						":" + socket.getPort() + ">: " + headerLine);
				
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
					resp = doFoundFile(socket, br);
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

	private static HTTPResponse doFoundFile(Socket sock, BufferedReader br)
			throws IOException {
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
		String answer = "0";
		String line = null;
		String data = "";
		int contentLength = 0;
		while (true) {
			line = br.readLine();
			if (line == null) {
				break;
			} else if (line.equals("")) {
				break;
			} else if (line.startsWith("Content-Length: ")) {
				line = line.replaceFirst(
						"Content-Length:", "");
				try {
					contentLength = Integer.parseInt(line.trim());
					System.out.println(
							"Found content length: " + contentLength);
				} catch (NumberFormatException e) { }
			}
		}
		
		if (contentLength == 0) {
			while (br.ready()) {
				char c = (char) br.read();
				if (c == -1) {
					break;
				}
				data += c;
			}
		} else {
			for (int i = 0; i < contentLength; i++) {
				char c = (char) br.read();
				if (c == -1) {
					break;
				}
				data += c;
			}
		}
		
		System.out.println("Received data: " + data);
		
		if (!data.isEmpty()) {
			try {
				JsonObject json = JsonObject.readFrom(data);
				String id = "";
				try {
					id = json.get("id").asString();
				} catch (Throwable e) { }
				JsonArray files = json.get("files").asArray();
				for (JsonValue valFile : files) {
					JsonObject file = valFile.asObject();
					Wazaa.getFoundFiles().addFoundFile(id,
							WazaaFoundFile.fromJson(file));
					Wazaa.getGUI().refreshFoundFiles();
				}
			} catch (ParseException | 
					UnsupportedOperationException |
					UnknownHostException |
					NumberFormatException e) {
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
			String fileName = "";
			try {
				fileName = URLDecoder.decode(commandArgs.get("fullname"), "UTF-8");
			} catch (UnsupportedEncodingException e1) { }
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

	private HTTPResponse doSearchFile(Map<String, String> commandArgs)
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
						HTTPUtil.sendSearchFileReqs(commandArgs, false);
					}
					ArrayList<WazaaFile> foundFiles = 
							FileIOUtil.findFiles(name);
					if (foundFiles != null && !foundFiles.isEmpty()) {
						// only send foundfile is there are actually files found
						Machine m = new Machine(sendip, sendport);
						JsonObject json = 
								buildFoundFileJson(foundFiles, commandArgs);
						new HTTPClient(
								m, "POST", "foundfile",
								json.toString());
					}
					
					answer = "0";
				}
			} catch (NumberFormatException
					| UnknownHostException e) { }
		}
		return new HTTPResponse200("text/plain", answer);
	}
	
	private JsonObject buildFoundFileJson(
			ArrayList<WazaaFile> foundFiles, 
			Map<String, String> commandArgs) {
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
		
		try {
			String myIP = InetAddress.getLocalHost().getHostAddress();
			myIP = socket.getLocalAddress().getHostAddress();
			for (WazaaFile wazaaFile : foundFiles) {
				JsonObject file = new JsonObject();
				file.add("ip", myIP);
				file.add("port", String.valueOf(
						Wazaa.getHTTPServer().getPort()));
				file.add("name", wazaaFile.getFileName());
				files.add(file);
			}
		} catch (UnknownHostException e) { }
		
		json.add("files", files);
		return json;
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
