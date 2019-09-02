import TCP.Client;

public class ClientRun {

	public static void main(String[] args) {
		System.out.println("Starting client");
		System.setProperty("java.net.preferIPv4Stack" , "true");
		Client client = new Client();
	}

}
