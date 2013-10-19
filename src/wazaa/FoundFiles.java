package wazaa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wazaa.ui.WazaaJFXGUI;

public class FoundFiles {
	private Map<String, List<WazaaFoundFile>> foundFiles = 
			new HashMap<String, List<WazaaFoundFile>>();
	
	public void addFoundFile(String searchID, WazaaFoundFile file) {
		if (!foundFiles.containsKey(searchID)) {
			foundFiles.put(searchID,
					Collections.synchronizedList(
							new ArrayList<WazaaFoundFile>()));
		}
		if (!foundFiles.get(searchID).contains(file)) {
			foundFiles.get(searchID).add(file);
		}
	}
	
	public List<WazaaFoundFile> getFilesList(String searchID) {
		return foundFiles.get(searchID);
	}
}
