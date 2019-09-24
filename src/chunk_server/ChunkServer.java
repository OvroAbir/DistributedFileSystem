package chunk_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import TCP.Server;
import TCP.ServerThread;
import control_node.ControlNode;
import messages.FileUpload_CL_CS;
import messages.HeartBeat;
import messages.MajorHeartBeat;
import messages.MessageType;
import messages.MinorHeartBeat;

public class ChunkServer 
{
	public static int MINOR_HEART_BEAT_INTERVAL = 30000;
	public static int MAJOR_HEART_BEAT_INTERVAL = 300000;
	
	public static int CHUNK_SERVER_SOCKET_PORT_FOR_CLIENTS = 5050;
	public static int CHUNK_SERVER_SOCKET_PORT_FOR_CHUNK_SERVERS = 5088;
	public static int CHUNK_SERVER_SOCKET_PORT_FOR_CONTROL_NODE_SEND_CHUNK_MSG = 5090;
	
	public static String chunkNameSeperator = "_chunk";
	public static String shardIndexSeperator = "_";
	public static String FILE_STORAGE_FOLDER_LOCATION = 
			(System.getProperty("os.name").startsWith("Windows") ? "C:\\TempProjectData" : 
				//"/s/chopin/a/grad/joyghosh/Documents/tmp") ;
				"/tmp/joyghosh");
			// TODO change folder location
		// TODO different chhunk folder for different cs
	protected String ipAddress;

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

	private ServerSocket serverSocketWithChunkServer;
	private int chunkServerThreadForChunkServer;
	
	private Thread chunkServerThreadForConnectingClient;
	private ChunkServerThreadForChunkServers chunkServerThreadForChunkServers;
	
	private static int ChunkServerCounter = 0;
	private int chunkServerID;
	
	private ChunkServerThreadForControllerSendMessage chunkServerThreadForControllerSendMessage;
	
	public ChunkServer(String ipAddress, int freeSpace) 
	{
		this.ipAddress = ipAddress;
		this.freeSpace = freeSpace;
		
		chunkServerThreadForChunkServer = 0;
		chunkServerID = ChunkServerCounter++;
		
		allChunks = new ArrayList<Chunk>();
		newlyAddedChunks = new ArrayList<Chunk>();
		corruptedChunkNames = new ArrayList<String>();
		hashMapForFile = new ConcurrentHashMap<String, Chunk>();
		
		System.out.println("Chunk Server Storage Location : " + getChunkServerSpecificFileStorageLocation());
		
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
		
		openConnectionWithControlNodeForSendChunkMessage();
		startHeartBeatTimer();
		openConnectionWithClients();
		openConnectionWithChunkServers();
	}
	
	private void openConnectionWithControlNodeForSendChunkMessage() 
	{
		chunkServerThreadForControllerSendMessage = new ChunkServerThreadForControllerSendMessage(this);
		chunkServerThreadForControllerSendMessage.start();
	}

	protected int getChunkServerID()
	{
		return chunkServerID;
	}
	
	private String getHostName()
	{
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
		    return addr.getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "DummyFolder";
	}
	
	protected String getChunkServerSpecificFileStorageLocation()
	{
		return FILE_STORAGE_FOLDER_LOCATION + File.separator + getHostName();
	}
	
	private void openConnectionWithClients()
	{
		chunkServerThreadForConnectingClient = new ChunkServerThreadForConnectingClients(this);
		chunkServerThreadForConnectingClient.start();
	}
	
	private void openConnectionWithChunkServers()
	{
		try 
		{
			serverSocketWithChunkServer = new ServerSocket(CHUNK_SERVER_SOCKET_PORT_FOR_CHUNK_SERVERS);
			System.out.println("Chunk Server " + ipAddress + " opened ServerSocketForChunkServers in port " +  CHUNK_SERVER_SOCKET_PORT_FOR_CHUNK_SERVERS);
			
		} 
		catch (IOException e) 
		{
			System.out.println("Can not create ChunkServersocket for ChunkServers.");
			e.printStackTrace();
		}
		
		while(true)
		{
			try 
			{
				Socket socketForCS = serverSocketWithChunkServer.accept();
				System.out.println("ChunkServer accepted a connection from CS");
				chunkServerThreadForChunkServers = new ChunkServerThreadForChunkServers(socketForCS, ipAddress,
						++chunkServerThreadForChunkServer, this);
				chunkServerThreadForChunkServers.start();
			}
			catch (IOException e) 
			{
				System.out.println("Tried to accept connection from CS. But failed.");
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


	protected void sendMessageToControlNode(MessageType msg)
	{
		try {
			objectOutputStreamWithControlNode.writeObject(msg);
			objectOutputStreamWithControlNode.flush();
		} catch (IOException e) {
			System.out.println("Can not send message to control node.");
			e.printStackTrace();
		}
	}
	
	protected MessageType receieveMessageFromControlNode()
	{
		MessageType msg = null;
		try {
			msg = (MessageType) objectInputStreamWithControlNode.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
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
		MajorHeartBeat majorHeartBeat = new MajorHeartBeat(ipAddress, freeSpace);
		majorHeartBeat.updateHeartBeatData(extractMetadataFromChunks(allChunks));
		return majorHeartBeat;
	}
	
	protected MinorHeartBeat getUpdatedMinorHeartBeat()
	{
		MinorHeartBeat minorHeartBeat = new MinorHeartBeat(ipAddress, freeSpace);
		minorHeartBeat.updateHeartBeatData(extractMetadataFromChunks(newlyAddedChunks), corruptedChunkNames);
		return minorHeartBeat;
	}
	
	protected ConcurrentHashMap<String, Chunk> getConcurrentHashMapForFiles()
	{
		return hashMapForFile;
	}

	protected ArrayList<Chunk> getAllChunks() {
		return allChunks;
	}

	protected ArrayList<Chunk> getNewlyAddedChunks() {
		return newlyAddedChunks;
	}
	
	protected void reduceFreeSpace(int amount)
	{
		freeSpace -= amount;
		freeSpace = Math.max(0, freeSpace);
	}
	
	protected void forwardFileUploadMessageToAnotherChunkServer(FileUpload_CL_CS fileUploadMsg)
	{
		if(fileUploadMsg.needToSendAnotherChunkServer() == false)
			return;
		String csAddress = fileUploadMsg.nextChunkServerAddress();
		System.out.println("Sending chunk copy to " + csAddress + " : " + CHUNK_SERVER_SOCKET_PORT_FOR_CHUNK_SERVERS);
		
		try {
			Socket socket = new Socket(csAddress, CHUNK_SERVER_SOCKET_PORT_FOR_CHUNK_SERVERS);
			ObjectOutputStream objectOutputStreamWithCS = new ObjectOutputStream(socket.getOutputStream());	
			objectOutputStreamWithCS.writeObject(fileUploadMsg);
			objectOutputStreamWithCS.flush();
			
			Thread.sleep(100);
			
			objectOutputStreamWithCS.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("Could not open connection with " + csAddress);
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
