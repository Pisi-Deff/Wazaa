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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result
				+ ((fileSize == null) ? 0 : fileSize.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WazaaFile other = (WazaaFile) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (fileSize == null) {
			if (other.fileSize != null)
				return false;
		} else if (!fileSize.equals(other.fileSize))
			return false;
		return true;
	}
}
