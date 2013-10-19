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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((machine == null) ? 0 : machine.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WazaaFoundFile other = (WazaaFoundFile) obj;
		if (machine == null) {
			if (other.machine != null)
				return false;
		} else if (!machine.equals(other.machine))
			return false;
		return true;
	}
}
