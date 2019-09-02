package TCP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client 
{
	private String serverIpAddress;
	private int serverPort;
	private Socket socket;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	
	public Client()
	{
		serverIpAddress = Server.IP_ADDRESS;
		serverPort = Server.PORT;
		startClient();
	}
	
	public void startClient()
	{
		try 
		{
			socket = new Socket(serverIpAddress, serverPort);
			System.out.println("Connected with Server");
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			
			String inComingMsg, outGoingMsg;
			int counter = 0;
			
			while(true)
			{
				outGoingMsg = "Can you read my msg " + counter +"?";
				dataOutputStream.writeUTF(outGoingMsg);
				// TODO Change this block
				inComingMsg = dataInputStream.readUTF();
				// TODO Change this block
				System.out.println("Server : " + inComingMsg);
				
				try {
				Thread.sleep(5000);
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Problem creating socket.");
			e.printStackTrace();
		}
	}
}
