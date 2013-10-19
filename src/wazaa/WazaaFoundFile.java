package wazaa;

import java.net.UnknownHostException;

import com.eclipsesource.json.JsonObject;

public class WazaaFoundFile extends WazaaFile {
	private Machine machine;

	public WazaaFoundFile(
			String fileName, Machine machine) {
		super(fileName);
		this.machine = machine;
	}
	
	public static WazaaFoundFile fromJson(JsonObject file)
			throws NumberFormatException, UnknownHostException {
		String name = file.get("name").asString();
		String ip = file.get("ip").asString();
		String port = file.get("port").asString();
		Machine m = new Machine(ip, port);
		return new WazaaFoundFile(name, m);
	}

	public Machine getMachine() {
		return machine;
	}
}
