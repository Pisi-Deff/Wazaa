package wazaa;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoundFiles {
	private Map<String, List<WazaaFoundFile>> foundFiles = 
			new HashMap<String, List<WazaaFoundFile>>();
	
	public void addFoundFile(String searchID, WazaaFoundFile file) {
		if (!foundFiles.containsKey(searchID)) {
			foundFiles.put(searchID,
					Collections.synchronizedList(
							new ArrayList<WazaaFoundFile>()));
		}
		boolean notMyMachine = true;
		try {
			notMyMachine = !Wazaa.isMyMachine(file.getMachine());
		} catch (UnknownHostException e) { }
		if (!foundFiles.get(searchID).contains(file) &&
				notMyMachine) {
			foundFiles.get(searchID).add(file);
		}
	}
	
	public List<WazaaFoundFile> getFilesList(String searchID) {
		return foundFiles.get(searchID);
	}
}
