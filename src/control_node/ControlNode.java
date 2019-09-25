package control_node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import TCP.Server;
import TCP.ServerThread;
import chunk_server.ChunkServer;
import messages.SendChunkCopyToAnotherChunksServer;

public class ControlNode
{
	public static final int DATA_SHARDS = 4; // TODO change this befroe submission
	public static final int PARITY_SHARDS = 2;
	public static final int TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS;
	public static final int BYTES_IN_INT = 4;
	
	public static int CHUNK_SIZE_BYTES = (int) Math.ceil(64 * 1024 / (double)DATA_SHARDS); // TODO May need to change this
	public static int REPLICATION_LEVEL = 3;
	protected static ArrayList<ChunkServerInfo> chunkServerInfos;
	protected static HashMap<String, ArrayList<String>> chunkStorageInfo; // chunkname to ipadress
	// TODO store chunk storage info
	
	
	public static String IP_ADDRESS = (System.getProperty("os.name").startsWith("Windows") ? 
			"127.0.0.1" : "129.82.44.134"); // TODO Update Server address before running
	public static int PORT = 5000;
	private ServerSocket serverSocket;
	private int serverThreadCounter;
	private HeartBeatTracker heartBeatTracker;
	private ChunkServerDetectorThread chunkServerDetectorThread;

	
	private HashMap<String, ControlNodeThread> controlNodeThreadMap;
	
	public ControlNode()
	{
		// TODO Try to make this thread safe
		chunkServerInfos = new ArrayList<ChunkServerInfo>();
		chunkStorageInfo = new HashMap<String, ArrayList<String>>();
		
		heartBeatTracker = new HeartBeatTracker();
		
		serverThreadCounter = 0;
		controlNodeThreadMap = new HashMap<String, ControlNodeThread>();
		createDeadChunkServerDetectorThread();
		startControlNode();
	}
	
	
	private void startControlNode()
	{
		try 
		{
			serverSocket = new ServerSocket(Server.PORT);
			System.out.println("Control node opened ServerSocket in port " +  Server.PORT);
			
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
				System.out.println("Control node accepted a connection from " + socket.getInetAddress().getHostAddress());
				ControlNodeThread controlNodeThread = new ControlNodeThread(socket, ++serverThreadCounter, heartBeatTracker);
				controlNodeThreadMap.put(socket.getInetAddress().getHostAddress(), controlNodeThread);
				controlNodeThread.start();
			}
			catch (IOException e) 
			{
				System.out.println("Tried to accept connection. But failed.");
				e.printStackTrace();
			}
			
		}
		
	}
	
	private void createDeadChunkServerDetectorThread()
	{
		chunkServerDetectorThread = new ChunkServerDetectorThread(this);
		chunkServerDetectorThread.start();
	}

	protected HeartBeatTracker getHeartBeatTracker() {
		return heartBeatTracker;
	}
	
	protected void sendMessagesToChunkServers(ArrayList<SendChunkCopyToAnotherChunksServer> msgs)
	{
		for(SendChunkCopyToAnotherChunksServer msg : msgs)
		{
			sendChunkCopyMessageToChunkServer(msg);
		}
	}
	
	private void sendChunkCopyMessageToChunkServer(SendChunkCopyToAnotherChunksServer msg)
	{
		try {
			Socket socket = new Socket(msg.getSendFrom(), ChunkServer.CHUNK_SERVER_SOCKET_PORT_FOR_CONTROL_NODE_SEND_CHUNK_MSG);
			ObjectOutputStream objectOutputStreamWithCS = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStreamWithCS.writeObject(msg);
			objectOutputStreamWithCS.flush();
			
			Thread.sleep(500);
			
			objectOutputStreamWithCS.close();
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
