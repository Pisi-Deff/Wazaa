package wazaa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import wazaa.http.HTTPServer;

public class Wazaa {
	public static final String WAZAANAME = "Wazaa";
	public static final String WAZAAVER = "0.1";
	
	private static HTTPServer srv = null;
	public static final int DEFAULTPORT = 1215;
	private static int port = DEFAULTPORT;
	
	public static final String MACHINESFILE = "machines.txt";
	public static final String REMOTEMACHINESFILE = ""; // TODO
	
	private static String shareFolder = "./wazaashare/";
	
	private static ArrayList<Machine> machines = new ArrayList<Machine>();
	
	public static void main(String[] args) {
		parseArgs(args);
		
		Path sharePath = FileSystems.getDefault().getPath(shareFolder);
		prepareShareFolder(sharePath);
		
		getMachinesFromFile(MACHINESFILE);
		
		srv = new HTTPServer(port);
		srv.start();
	}

	private static void parseArgs(String[] args) {
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("Invalid launch arguments.");
				System.out.println("The first and only argument needs to be the port"
						+ " that Wazaa will use.");
				System.exit(1);
			}
		}
	}

	private static void prepareShareFolder(Path sharePath) {
		if (!Files.isDirectory(sharePath)) {
			try {
				Files.createDirectories(sharePath);
				System.out.println("Created share folder: "
						+ sharePath.toAbsolutePath().toString());
			} catch (IOException e) {
				System.out.println(
						"Share folder does not exist and failure creating it. " +
						"(" + sharePath.toAbsolutePath().toString() + ")");
				System.exit(1);
			}
		}
		System.out.println("Using share folder: "
				+ sharePath.toAbsolutePath().toString());
	}

	private static void getMachinesFromFile(String fileName) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName)));
			parseMachinesFromJson(in);
		} catch (FileNotFoundException e) {
			System.out.println("Local file of machines missing or inacessible. ("
					+ MACHINESFILE + ")");
		} catch (IOException e) {
			System.out.println("Invalid syntax in local machines file (" + MACHINESFILE
					+ "). Skipping.");
		}
	}

	private static void parseMachinesFromJson(BufferedReader in)
			throws IOException {
		JsonArray jsonMachines = JsonArray.readFrom(in);
		for (JsonValue val : jsonMachines) {
			JsonArray machineArr = val.asArray();
			String IPStr = machineArr.get(0).asString();
			String PortStr = machineArr.get(1).asString();
			addMachine(IPStr, PortStr);
		}
	}

	public static void addMachine(String IPStr, String PortStr) {
		try {
			Machine m = new Machine(IPStr, PortStr);
			if (!machines.contains(m)) {
				machines.add(m);
				System.out.println("Added machine: "
						+ m.getIP().getHostAddress() + ":"
						+ m.getPort());
			}
		} catch (UnknownHostException | NumberFormatException e) {
			System.out.println("Skipping invalid machine: " + IPStr
					+ " - " + PortStr);
		}
	}
	
	public static Iterator<Machine> getMachinesIterator() {
		return machines.iterator();
	}
	
	public static Path getShareFilePath(String fileName) 
			throws InvalidPathException {
		return FileSystems.getDefault().getPath(shareFolder + fileName);
	}
	
	public static byte[] getFileBytes(String fileName) 
			throws IOException, InvalidPathException {
		return Files.readAllBytes(getShareFilePath(fileName));
	}
	
	public static String getFileContentType(String fileName)
			throws InvalidPathException, IOException {
		return Files.probeContentType(getShareFilePath(fileName));
	}
	
	public static ArrayList<String> findFiles(final String search) {
		ArrayList<String> foundFiles = new ArrayList<String>();
		Path sharePath = getShareFilePath("");
		
		if (search != null) {
			DirectoryStream.Filter<Path> filter = 
					new DirectoryStream.Filter<Path>() {
				@Override
				public boolean accept(Path entry) throws IOException {
					String actual = 
							entry.getFileName().toString().toLowerCase();
					return actual.contains(search.toLowerCase()); //TODO search better
				}
			};
			
			findFilesRecursivelyFromDirStream(
					filter, foundFiles, sharePath,
					sharePath);
		}
		return foundFiles;
	}

	public static void findFilesRecursivelyFromDirStream(
			DirectoryStream.Filter<Path> filter, 
			ArrayList<String> foundFiles, Path sharePath, Path shareFolder) {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(
				sharePath)) {
			for (Path file : ds) {
				if (Files.isDirectory(file)) {
					findFilesRecursivelyFromDirStream(
							filter, foundFiles, 
							getShareFilePath(
									file.getFileName().toString()),
							shareFolder);
				} else if (filter.accept(file)) {
					String relativeFileName = 
							shareFolder.relativize(file).toString();
					foundFiles.add(relativeFileName);
//					System.out.println(relativeFileName);
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading share folder contents. " +
					"(" + sharePath.toAbsolutePath().toString() + ")");
		}
	}
	
	public static String generateUniqueID() {
		return UUID.randomUUID().toString();
	}

}
