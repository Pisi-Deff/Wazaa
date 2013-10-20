package wazaa;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import wazaa.http.HTTPClient;

public class DownloadManager extends Thread {
	private List<Download> downloads = 
			Collections.synchronizedList(new ArrayList<Download>());
	
	public DownloadManager() {
		start();
	}
	
	@Override
	public void run() {
		while (true) {
			for (Download d : downloads) {
				if (d.isFinished() && !d.isWritten()) {
					d.writeToFile();
					Wazaa.getGUI().refreshDownloads();
				}
			}
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) { }
		}
	}
	
	public void addClient(File saveLocation, HTTPClient client) {
		downloads.add(new Download(client, saveLocation));
	}
	
	public List<Download> getDownloads() {
		return downloads;
	}
}
