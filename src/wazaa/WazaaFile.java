package wazaa;

public class WazaaFile {
	private final String fileName;
	private final String fileSize;
	
	public WazaaFile(String fileName) {
		this.fileName = fileName;
		this.fileSize = "";
	}

	public String getFileName() {
		return fileName;
	}
	
	public String getFileSize() {
		return fileSize;
	}
	
	@Override
	public String toString() {
		return this.fileName;
	}
}
