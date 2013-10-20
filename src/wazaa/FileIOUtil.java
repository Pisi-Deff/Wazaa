package wazaa;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileIOUtil {
	public static byte[] getFileBytes(String fileName) 
			throws IOException, InvalidPathException {
		return Files.readAllBytes(Wazaa.getShareFolderPathForFile(fileName));
	}
	
	public static String getFileContentType(String fileName)
			throws InvalidPathException, IOException {
		return Files.probeContentType(Wazaa.getShareFolderPathForFile(fileName));
	}
	
	public static ArrayList<WazaaFile> findFiles(final String search) {
		ArrayList<WazaaFile> foundFiles = new ArrayList<WazaaFile>();
		Path sharePath = Wazaa.getShareFolderPath();
		
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
			ArrayList<WazaaFile> foundFiles, 
			Path sharePath, Path shareFolder) {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(
				sharePath)) {
			for (Path file : ds) {
				if (Files.isDirectory(file)) {
					String folderPath = 
						shareFolder.relativize(sharePath).toString() +
						"/" +
						file.getFileName().toString();
					findFilesRecursivelyFromDirStream(
							filter, foundFiles, 
							Wazaa.getShareFolderPathForFile(
									folderPath),
							shareFolder);
				} else if (filter.accept(file)) {
					String relativeFileName = 
							shareFolder.relativize(file).toString();
					foundFiles.add(new WazaaFile(relativeFileName));
//					System.out.println(relativeFileName);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error reading share folder contents. " +
					"(" + sharePath.toAbsolutePath().toString() + ")");
		}
	}
}
