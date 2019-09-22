import java.net.SocketException;
import java.net.UnknownHostException;

import client.Client;

public class ClientRun {

	public static void main(String[] args) throws SocketException, UnknownHostException {
		System.out.println("Starting client");
		Client client = new Client(ChunkServerRunner.getIpAdress());
	}

}
