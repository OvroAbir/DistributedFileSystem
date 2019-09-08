package control_node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import TCP.Server;
import TCP.ServerThread;

public class ControlNode
{
	public static int CHUNK_SIZE_BYTES = 8 * 1024; // TODO May need to change this
	public static int REPLICATION_LEVEL = 3;
	protected static ArrayList<ChunkServerInfo> chunkServerInfos;
	
	
	public static String IP_ADDRESS = "127.0.0.1"; // TODO Update Server address before running
	public static int PORT = 5000;
	private ServerSocket serverSocket;
	private int serverThreadCounter;
	
	
	public ControlNode()
	{
		// TODO Try to make this thread safe
		chunkServerInfos = new ArrayList<ChunkServerInfo>();
		
		serverThreadCounter = 0;
		startControlNode();
	}
	
	
	
	private void startControlNode()
	{
		try 
		{
			serverSocket = new ServerSocket(Server.PORT);
			System.out.println("Server opened ServerSocket in port " +  Server.PORT);
			
		} 
		catch (IOException e) 
		{
			System.out.println("Can not create server socket for Control Node.");
			e.printStackTrace();
		}
		
		while(true)
		{
			try 
			{
				Socket socket = serverSocket.accept();
				System.out.println("Control node accepted a connection");
				ControlNodeThread controlNodeThread = new ControlNodeThread(socket, ++serverThreadCounter);
				controlNodeThread.start();
			}
			catch (IOException e) 
			{
				System.out.println("Tried to accept connection. But failed.");
				e.printStackTrace();
			}
			
		}
		
	}
}
