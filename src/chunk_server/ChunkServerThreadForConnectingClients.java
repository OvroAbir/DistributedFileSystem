package chunk_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChunkServerThreadForConnectingClients extends Thread {

	private ChunkServer chunkServerInstance;
	private ServerSocket serverSocketWithClients;
	private int chunkServerThreadCounter;
	
	public ChunkServerThreadForConnectingClients(ChunkServer chunkServerInstance) 
	{
		this.chunkServerInstance = chunkServerInstance;
		this.chunkServerThreadCounter = 0;
	}
	
	public void run()
	{
		try 
		{
			serverSocketWithClients = new ServerSocket(ChunkServer.CHUNK_SERVER_SOCKET_PORT_FOR_CLIENTS);
			System.out.println("Chunk Server " + chunkServerInstance.ipAddress + " opened ServerSocketForLients in port " +  ChunkServer.CHUNK_SERVER_SOCKET_PORT_FOR_CLIENTS);
			
		} 
		catch (IOException e) 
		{
			System.out.println("Can not create ChunkServersocket for Clients.");
			e.printStackTrace();
		}
		
		while(true)
		{
			try 
			{
				Socket socketForClients = serverSocketWithClients.accept();
				System.out.println("ChunkServer accepted a connection");
				ChunkServerThreadForClients serverThreadForClients = new ChunkServerThreadForClients(socketForClients, chunkServerInstance.ipAddress,
						++chunkServerThreadCounter, chunkServerInstance);
				serverThreadForClients.start();
			}
			catch (IOException e) 
			{
				System.out.println("Tried to accept connection from client. But failed.");
				e.printStackTrace();
			}
			
		}
		
	}
}
