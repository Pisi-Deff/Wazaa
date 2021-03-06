package wazaa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import wazaa.http.HTTPClient;

public class Download {
	private HTTPClient httpClient;
	private File saveLocation;
	private boolean isWritten = false;
	
	public Download(HTTPClient httpClient, File saveLocation) {
		this.httpClient = httpClient;
		this.saveLocation = saveLocation;
	}
	
	public void writeToFile() {
		if (isFinished() && !isWritten()) {
			byte[] fileBytes = httpClient.getResponseFileBytes();
			if (fileBytes != null) {
				FileOutputStream out = null;
				try {
					out = 
							new FileOutputStream(saveLocation);
					out.write(fileBytes);
					
					isWritten = true;
					System.out.println("File saved to: " + 
							saveLocation.getAbsolutePath().toString());
				} catch (IOException e) {
				} finally {
					try {
						out.close();
					} catch (Throwable e) {}
				}
			}
		}
	}
	
	public HTTPClient getHTTPClient() {
		return httpClient;
	}
	
	public File getSaveLocation() {
		return saveLocation;
	}
	
	public synchronized boolean isFinished() {
		return httpClient.getResponseDone() || !httpClient.isAlive();
	}
	
	public synchronized boolean isWritten() {
		return isWritten;
	}
	
	public String getStatus() {
		if (httpClient.getResponseDone()) {
			return "Finished";
		} else if (!httpClient.isAlive()) {
			return "Failed";
		} else {
			return "Downloading";
		}
	}
	
	@Override
	public String toString() {
		return getStatus() + ": <" + httpClient.getResponseFileName() + 
				"> as <" + saveLocation.getName() + 
				"> from: " + httpClient.getMachine().toString();
	}
}
