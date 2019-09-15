package chunk_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import TCP.Server;
import TCP.ServerThread;
import control_node.ControlNode;
import messages.HeartBeat;
import messages.MajorHeartBeat;
import messages.MessageType;
import messages.MinorHeartBeat;

public class ChunkServer 
{
	public static int MINOR_HEART_BEAT_INTERVAL = 30000;
	public static int MAJOR_HEART_BEAT_INTERVAL = 300000;
	
	public static int CHUNK_SERVER_SOCKET_PORT_FOR_CLIENTS = 5050;
	
	public static String chunkNameSeperator = "_chunk";
	
	private String ipAddress;
	private int freeSpace; // in bytes
	
	private ArrayList<Chunk> allChunks;
	private ArrayList<Chunk> newlyAddedChunks;
	private ArrayList<String> corruptedChunkNames;
	protected ConcurrentHashMap<String, Chunk> hashMapForFile;
	// TODO will need to add files to corrupted list
	
	
	private Socket socketWithControlNode;
	private DataInputStream dataInputStreamWithControlNode;
	private DataOutputStream dataOutputStreamWithControlNode;
	private ObjectInputStream objectInputStreamWithControlNode;
	private ObjectOutputStream objectOutputStreamWithControlNode;
	
	private ServerSocket serverSocketWithClients;
	private int chunkServerThreadCounter;
	
	public ChunkServer(String ipAddress, int freeSpace) 
	{
		this.ipAddress = ipAddress;
		this.freeSpace = freeSpace;
		
		chunkServerThreadCounter = 0;
		
		allChunks = new ArrayList<Chunk>();
		newlyAddedChunks = new ArrayList<Chunk>();
		corruptedChunkNames = new ArrayList<String>();
		
		try {
			socketWithControlNode = new Socket(ControlNode.IP_ADDRESS, ControlNode.PORT);
			
			dataInputStreamWithControlNode = new DataInputStream(socketWithControlNode.getInputStream());
			dataOutputStreamWithControlNode = new DataOutputStream(socketWithControlNode.getOutputStream());
			
			objectOutputStreamWithControlNode = new ObjectOutputStream(dataOutputStreamWithControlNode);
			objectInputStreamWithControlNode = new ObjectInputStream(dataInputStreamWithControlNode);
			
			
		} catch (IOException e) {
			System.out.println("Can not create socket with control node");
			e.printStackTrace();
		}
		
		startHeartBeatTimer();
		openConnectionWithClients();
	}
	
	private void openConnectionWithClients()
	{
		try 
		{
			serverSocketWithClients = new ServerSocket(CHUNK_SERVER_SOCKET_PORT_FOR_CLIENTS);
			System.out.println("Chunk Server " + ipAddress + " opened ServerSocketForLients in port " +  CHUNK_SERVER_SOCKET_PORT_FOR_CLIENTS);
			
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
				ChunkServerThreadForClients serverThreadForClients = new ChunkServerThreadForClients(socketForClients, ipAddress,
						++chunkServerThreadCounter, this);
				serverThreadForClients.start();
			}
			catch (IOException e) 
			{
				System.out.println("Tried to accept connection from client. But failed.");
				e.printStackTrace();
			}
			
		}
		
	}
	
	private void startHeartBeatTimer()
	{
		Timer timer = new Timer("HearBeatTimer");
		TimerTask sendHeartBeatTask = new SendHeartBeatTask(this);
		timer.schedule(sendHeartBeatTask, 0, MINOR_HEART_BEAT_INTERVAL);
	}
	
	public String getIpAddress() {
		return ipAddress;
	}


	private void sendMessageToControlNode(MessageType msg)
	{
		try {
			objectOutputStreamWithControlNode.writeObject(msg);
		} catch (IOException e) {
			System.out.println("Can not send message to control node.");
			e.printStackTrace();
		}
	}
	
	protected void sendHeartBeat(HeartBeat heartBeat)
	{
		sendMessageToControlNode(heartBeat);
	}
	
	protected void clearNewlyAddedChunks()
	{
		newlyAddedChunks.clear();
	}
	
	private ArrayList<ChunkMetadata> extractMetadataFromChunks(ArrayList<Chunk> chunks)
	{
		ArrayList<ChunkMetadata> metadatas = new ArrayList<ChunkMetadata>();
		for(Chunk chunk : chunks)
			metadatas.add(chunk.getChunkMetadata());
		return metadatas;
	}
	
	protected MajorHeartBeat getUpdatedMajorHeartBeat()
	{
		MajorHeartBeat majorHeartBeat = new MajorHeartBeat(ipAddress);
		majorHeartBeat.updateHeartBeatData(extractMetadataFromChunks(allChunks));
		return majorHeartBeat;
	}
	
	protected MinorHeartBeat getUpdatedMinorHeartBeat()
	{
		MinorHeartBeat minorHeartBeat = new MinorHeartBeat(ipAddress);
		minorHeartBeat.updateHeartBeatData(extractMetadataFromChunks(newlyAddedChunks), corruptedChunkNames);
		return minorHeartBeat;
	}
	
	protected ConcurrentHashMap<String, Chunk> getConcurrentHashMapForFiles()
	{
		return hashMapForFile;
	}
}
