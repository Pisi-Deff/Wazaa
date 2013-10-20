package wazaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javafx.application.Application;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import wazaa.http.HTTPClient;
import wazaa.http.HTTPServer;
import wazaa.http.HTTPUtil;
import wazaa.ui.WazaaJFXGUI;

/**
 * P2P file sharing software Wazaa.
 * Made for Network Applications II course.
 * 
 * @author Eerik Mägi 112156
 */
public class Wazaa {
	public static final String WAZAANAME = "Wazaa";
	public static final String WAZAAVER = "0.1";
	
	private static WazaaJFXGUI gui;
	private static FoundFiles foundFiles = new FoundFiles();
	private static DownloadManager downloadManager = new DownloadManager();
	private static HTTPServer httpServer;
	
	public static final int DEFAULTPORT = 1215;
	
	public static final String DEFAULTMACHINESFILE = "machines.txt";
	public static final String DEFAULTSHAREFOLDER = "wazaashare/";
	public static final int DEFAULTTTL = 5;
	
	private static File shareFolder = new File(DEFAULTSHAREFOLDER);
	
	private static List<Machine> machines = 
			Collections.synchronizedList(new ArrayList<Machine>());
	
	public static void main(String[] args) {
		Application.launch(WazaaJFXGUI.class, args);
	}
	
	public static void prepareShareFolder()
			throws IOException {
		Path sharePath = shareFolder.toPath();
		if (!Files.isDirectory(sharePath)) {
			try {
				Files.createDirectories(sharePath);
				System.out.println("Created share folder: "
						+ sharePath.toAbsolutePath().toString());
			} catch (IOException e) {
				System.out.println(
						"Share folder does not exist "
						+ "and failure creating it. " +
						"(" + sharePath.toAbsolutePath().toString() + ")");
				throw new IOException();
			}
		}
		System.out.println("Using share folder: "
				+ sharePath.toAbsolutePath().toString());
	}
	
	public static void setShareFolder(File shareFolder) {
		Wazaa.shareFolder = shareFolder; 
	}
	
	public static Path getShareFolderPath() 
			throws InvalidPathException {
		return shareFolder.toPath();
	}
	
	public static Path getShareFolderPathForFile(String fileName) 
			throws InvalidPathException {
		return FileSystems.getDefault().getPath(
				shareFolder.toString() + "/" + fileName);
	}

	public static int getMachinesFromFile(String fileName) {
		BufferedReader in = null;
		int count = 0;
		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName)));
			String jsonString = "";
			String line;
			while ((line = in.readLine()) != null) {
				jsonString += line;
			}
			count = parseMachinesFromJson(jsonString);
		} catch (FileNotFoundException e) {
			System.out.println("Machines file missing "
					+ "or inacessible. ("
					+ DEFAULTMACHINESFILE + ")");
		} catch (IOException e) {
			System.out.println("Invalid syntax in machines file ("
					+ DEFAULTMACHINESFILE + "). Skipping.");
		} finally {
			try {
				in.close();
			} catch (Exception e) {}
		}
		return count;
	}
	
	public static int getMachinesFromURL(URL url) {
		int count = 0;
		try {
			HTTPClient c = new HTTPClient(url);
			while (c.isAlive() && !c.getResponseDone()) {
				Thread.sleep(10);
			}
			// TODO: needs refactoring
			String json = null;
			if (c.getResponse() != null) {
				json = c.getResponse();
			} else if (c.getResponseFileBytes() != null) {
				json = new String(c.getResponseFileBytes());
			}
			count = parseMachinesFromJson(json);
		} catch (Exception e) { }
		return count;
	}

	public static int parseMachinesFromJson(String jsonString)
			throws IOException {
		int count = 0;
		if (jsonString != null) {
			JsonArray jsonMachines = JsonArray.readFrom(jsonString);
			for (JsonValue val : jsonMachines) {
				JsonArray machineArr = val.asArray();
				String IPStr = machineArr.get(0).asString();
				String PortStr = machineArr.get(1).asString();
				if (addMachine(IPStr, PortStr)) {
					count++;
				}
			}
		}
		return count;
	}

	public static boolean addMachine(String IPStr, String PortStr) {
		try {
			Machine m = new Machine(IPStr, PortStr);
			// TODO ignore own ip+port
			if (!(machines.contains(m) || isMyMachine(m))) {
				machines.add(m);
				System.out.println("Added machine: "
						+ m.getIP().getHostAddress() + ":"
						+ m.getPort());
				gui.refreshMachines();
				return true;
			}
		} catch (UnknownHostException | IllegalArgumentException e) {
			System.out.println("Skipping invalid machine: " + IPStr
					+ " - " + PortStr);
		}
		return false;
	}

	public static String searchForFile(String text) {
		try {
			String uuid = Wazaa.generateUniqueID();
			Map<String, String> commandArgs = 
					new HashMap<String, String>();
			commandArgs.put("name", 
					URLEncoder.encode(
							text, "UTF-8"));
			commandArgs.put("sendip", 
					InetAddress.getLocalHost().getHostAddress());
			commandArgs.put("sendport", 
					String.valueOf(Wazaa.getHTTPServer().getPort()));
			commandArgs.put("ttl", String.valueOf(Wazaa.DEFAULTTTL + 1));
			commandArgs.put("id", uuid);
			HTTPUtil.sendSearchFileReqs(commandArgs);
			return uuid;
		} catch (UnsupportedEncodingException | 
				UnknownHostException e) { }
		return null;
	}

	public static List<Machine> getMachines() {
		return machines;
	}
	
	public static String generateUniqueID() {
		return UUID.randomUUID().toString();
	}
	
	public synchronized static void setGUI(WazaaJFXGUI gui) {
		Wazaa.gui = gui;
	}
	
	public synchronized static WazaaJFXGUI getGUI() {
		return gui;
	}
	
	public static FoundFiles getFoundFiles() {
		return foundFiles;
	}
	
	public static DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public synchronized static HTTPServer getHTTPServer() {
		return httpServer;
	}

	public synchronized static void setHTTPServer(HTTPServer httpServer) {
		Wazaa.httpServer = httpServer;
	}

	public static boolean isMyMachine(Machine m) 
			throws UnknownHostException {
		String myIP = InetAddress.
				getLocalHost().getHostAddress();
		int myPort = 0;
		try {
			myPort = getHTTPServer().getPort();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return (m.getIP().getHostAddress().equals(myIP) &&
			m.getPort() == myPort);
	}

}
