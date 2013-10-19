package wazaa;

public class WazaaFoundFile extends WazaaFile {
	private Machine machine;

	public WazaaFoundFile(
			String fileName, Machine machine) {
		super(fileName);
		this.machine = machine;
	}

	public Machine getMachine() {
		return machine;
	}
	
	public String getMachineString() {
		return machine.toString();
	}
}
