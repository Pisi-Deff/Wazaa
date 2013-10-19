package wazaa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wazaa.ui.WazaaJFXGUI;

public class FoundFiles {
	private WazaaJFXGUI gui;
	private Map<String, List<WazaaFoundFile>> foundFiles = 
			new HashMap<String, List<WazaaFoundFile>>();
	
	public FoundFiles(WazaaJFXGUI gui) {
		this.gui = gui;
	}
	
	public void addFoundFile(String searchID, WazaaFoundFile file) {
		if (!foundFiles.containsKey(searchID)) {
			foundFiles.put(searchID,
					Collections.synchronizedList(
							new ArrayList<WazaaFoundFile>()));
		}
		foundFiles.get(searchID).add(file);
		gui.foundFilesUpdated();
	}
	
	public List<WazaaFoundFile> getFilesList(String searchID) {
		return foundFiles.get(searchID);
	}
}
