package TCP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server 
{
	public static String IP_ADDRESS;
	public static int PORT;
	private ServerSocket serverSocket;
	private int serverThreadCounter;
	
	public Server(String ipAdress, int port)
	{
		Server.IP_ADDRESS = ipAdress;
		Server.PORT = port;
		serverThreadCounter = 0;
		startServer();
	}
	
	public void startServer()
	{
		try 
		{
			serverSocket = new ServerSocket(Server.PORT);
			System.out.println("Server opened ServerSocket in port " + Server.PORT);
			
		} 
		catch (IOException e) 
		{
			System.out.println("Can not create server socket for Server.");
			e.printStackTrace();
		}
		
		while(true)
		{
			try 
			{
				Socket socket = serverSocket.accept();
				System.out.println("Server accepted a connection");
				ServerThread serverThread = new ServerThread(socket, ++serverThreadCounter);
				serverThread.start();
			}
			catch (IOException e) 
			{
				System.out.println("Tried to accept connection from client. But failed.");
				e.printStackTrace();
			}
			
		}
		
	}
}

