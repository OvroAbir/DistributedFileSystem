import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import chunk_server.ChunkServer;

public class ChunkServerRunner 
{
	public static String getIpAdress() throws SocketException, UnknownHostException
	{
		DatagramSocket socket = new DatagramSocket();
		socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
		return socket.getLocalAddress().getHostAddress();
			
	}
	
	public static void main(String[] arg) throws SocketException, UnknownHostException
	{
		System.out.println("Starting Chunk Server");
		ChunkServer chunkServer = new ChunkServer(getIpAdress(), 500000);
	}
}
