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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import wazaa.Machine;
import wazaa.Wazaa;

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
				
				HTTPResponse resp = new HTTPResponse404();
				
				if (command.startsWith("/searchfile")) {
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
									Iterator<Machine> iter = 
											Wazaa.getMachinesIterator();
									while (iter.hasNext()) {
										Machine m = iter.next();
										HTTPClient c = new HTTPClient(
												m, "GET",
												buildSearchFileCommand(
														commandArgs));
										c.run();
									}
								}
								//TODO do search
								answer = "0";
							}
						} catch (NumberFormatException
								| UnknownHostException e) { }
					}
					resp = new HTTPResponse200("text/plain", answer);
				} else if (command.startsWith("/getfile")) {
					/*
					 * fullname=fullfilename
					 */
					if (commandArgs != null
							&& commandArgs.containsKey("fullname")) {
						String fileName = commandArgs.get("fullname");
						if (fileName != null) {
							try {
								resp = new HTTPResponse200(
										Wazaa.getFileContentType(fileName),
										Wazaa.getFileBytes(fileName));
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
				} else if (command.startsWith("/foundfile")
						&& headerTok.equalsIgnoreCase("POST")) {
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

					resp = new HTTPResponse200(
							"text/plain", 
							answer);
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
	
	private static String buildSearchFileCommand(
			Map<String, String> commandArgs) {
		StringBuilder s = new StringBuilder("searchfile?");
		// TODO
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
