import TCP.Client;
import TCP.Server;

public class ServerRun {

	public static void main(String[] args) throws InterruptedException 
	{
		int numberOfClient = 1;
		System.out.println("Starting server");
		Server server = new Server("127.0.0.1", 9040);
		
	}

}
