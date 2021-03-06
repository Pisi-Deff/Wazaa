package wazaa;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Machine {
	private String host;
	private InetAddress ip;
	private int port;
	
	public Machine(String ip, String port)
			throws UnknownHostException,
			IllegalArgumentException {
		this.ip = InetAddress.getByName(ip);
		this.host = ip;
		this.port = Integer.parseInt(port);
		if (this.port < 0 || this.port > 65535) {
			throw new IllegalArgumentException();
		}
	}
	
	public Machine(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public InetAddress getIP() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Machine other = (Machine) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String h = ip.getHostAddress();
		if (host != null) {
			h = host;
		}
		return h + ":" + port;
	}
}
