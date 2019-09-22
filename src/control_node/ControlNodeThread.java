package control_node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import chunk_server.ChunkMetadata;
import chunk_server.ChunkServer;
import messages.ChunkAllLocation;
import messages.ErrorMessage;
import messages.FileStoringChunkServerList;
import messages.FileUploadRequest_CL_CN;
import messages.FreeChunkServerList;
import messages.HeartBeat;
import messages.MajorHeartBeat;
import messages.MessageType;
import messages.MinorHeartBeat;
import messages.RequestFileLocation_CL_CN;
import messages.RequestValidChunkLocation_CS_CL;

public class ControlNodeThread extends Thread
{
	private Socket socket;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	private String clientAddress;
	private int threadId;
	private ArrayList<ChunkServerInfo> chunkServerInfos;
	private HashMap<String, ArrayList<String>> chunkStorageInfo;
	private HeartBeatTracker heartBeatTracker;
	
	
	public ControlNodeThread(Socket s, int threadId, HeartBeatTracker heartBeatTracker)
	{
		socket = s;
		this.threadId = threadId;
		this.heartBeatTracker = heartBeatTracker;
		clientAddress = s.getInetAddress().getHostAddress() + " (" + this.threadId + ") ";
		chunkServerInfos = ControlNode.chunkServerInfos;
		chunkStorageInfo = ControlNode.chunkStorageInfo;
		
		try 
		{
			dataInputStream = new DataInputStream(s.getInputStream());
			dataOutputStream = new DataOutputStream(s.getOutputStream());
			objectInputStream = new ObjectInputStream(dataInputStream);
			objectOutputStream = new ObjectOutputStream(dataOutputStream);
		} 
		catch (IOException e) 
		{
			System.out.println("Can not get Data Stream for " + clientAddress);
			e.printStackTrace();
		}
		
	}
		
	public void run()
	{
		System.out.println("Sever Thread started for " + clientAddress);
		MessageType inComingMsg;
		
		try
		{
			while(true)
			{
				inComingMsg = (MessageType) objectInputStream.readObject();
				resolveReceivedMessage(inComingMsg);
			}
		}
		catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) 
			{
				e1.printStackTrace();
			}
			System.out.println("Problem reading data stream for " + clientAddress);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Class mismatch error");
			e.printStackTrace();
		}
		
	}
	
	protected void sendMessage(MessageType msg)
	{
		try {
			objectOutputStream.writeObject(msg);
			objectOutputStream.flush();
		} catch (IOException e) {
			System.out.println("Can not send message.");
			e.printStackTrace();
		}
	}
	
	
	private String extractIpAddressFromClientAddress(String address)
	{
		address = address.substring(0, address.indexOf(' '));
		return address;
	}
	
	private int findChunkServerInfoIndex(String address)
	{
		address = extractIpAddressFromClientAddress(address);
		int i=0;
		for(ChunkServerInfo csi : chunkServerInfos)
		{
			if(csi.getIp_adress().equals(address))
				return i;
			i++;
		}
		
		return -1;
	}
	
	private void updateChunkServerInfos(MessageType msg)
	{
		HeartBeat hb = (HeartBeat) msg;
		int index = findChunkServerInfoIndex(clientAddress);
		if(index == -1)
		{
			ChunkServerInfo csi = new ChunkServerInfo(extractIpAddressFromClientAddress(clientAddress),
					hb.getFreeSpace());
			chunkServerInfos.add(csi);
		}
		else
		{
			chunkServerInfos.get(index).setFreeSpace(hb.getFreeSpace());
		}
	}
	
	private void addChunkInfosToChunkStorage(ArrayList<ChunkMetadata> chunkDatas)
	{
		String ipaddress = extractIpAddressFromClientAddress(clientAddress);
		
		for(ChunkMetadata chunkMetadata : chunkDatas)
		{
			String chunkName = chunkMetadata.getChunkFileName();
			if(chunkStorageInfo.containsKey(chunkName) == false)
			{
				ArrayList<String> addresses = new ArrayList<String>();
				addresses.add(ipaddress);
				
				chunkStorageInfo.put(chunkName, addresses);
			}
			else if(chunkStorageInfo.get(chunkName).contains(ipaddress) == false)
					chunkStorageInfo.get(chunkName).add(ipaddress);
		}
	}
	
	private void updateChunkStorageInformation(MessageType msg) 
	{
		if(msg instanceof MajorHeartBeat)
		{
			ArrayList<ChunkMetadata> allChunkMetadatas = ((MajorHeartBeat) msg).getAllChunkMetaDatas();
			addChunkInfosToChunkStorage(allChunkMetadatas);
		}
		else if(msg instanceof MinorHeartBeat)
		{
			ArrayList<ChunkMetadata> newChunkMetadatas = ((MinorHeartBeat) msg).getNewlyAddedChunkNames();
			addChunkInfosToChunkStorage(newChunkMetadatas);
		}
	}
	
	
	private FileStoringChunkServerList findStoredFileLocations(RequestFileLocation_CL_CN msg)
	{
		String fileName = msg.getFileName();
		ArrayList<String> chunkServerList = new ArrayList<String>();
		Random random = new Random();
		
		for(int chunk = 0;;chunk++)
		{
			String chunkName = fileName + ChunkServer.chunkNameSeperator + chunk;
			if(chunkStorageInfo.containsKey(chunkName) == false)
				break;
			ArrayList<String> csList = chunkStorageInfo.get(chunkName);
			if(csList.size() == 0)
				break;
			String randDomCSAdrs = csList.get(random.nextInt(csList.size()));
			chunkServerList.add(randDomCSAdrs);
		}
		
		return new FileStoringChunkServerList(chunkServerList);
	}
	
	private void updateHeartBeatTracker(String msgFrom)
	{
		heartBeatTracker.noteDownReportingTime(msgFrom);
	}
	
	private void resolveReceivedMessage(MessageType msg)
	{
		if(msg.getMessageType() == MessageType.UPLOAD_FILE_REQ_CL_CN)
		{
			System.out.println("Received file upload request from client " + msg.getMessageFrom());
			MessageType chunkServerList = findFreeChunkServers(((FileUploadRequest_CL_CN) msg).getFileSize());
			sendMessage(chunkServerList);
		}
		else if(msg.getMessageType() == MessageType.REQUEST_CHUNK_SERVER_LIST_FOR_STORED_FILE)
		{
			System.out.println("Received file location request from client " + msg.getMessageFrom());
			MessageType fileLocations = findStoredFileLocations((RequestFileLocation_CL_CN) msg);
			sendMessage(fileLocations);
		}
		// TODO when server goes down remove its chunks from chunk storage infs
		else if(msg.getMessageType() == MessageType.MAJOR_HEARTBEAT_CS_CN)
		{
			System.out.println("Received Major heartbeat from " + msg.getMessageFrom());
			updateChunkServerInfos(msg);
			updateChunkStorageInformation(msg);
			updateHeartBeatTracker(msg.getMessageFrom());
			// TODO Process the message
		}
		else if(msg.getMessageType() == MessageType.MINOR_HEARTBEAT_CS_CN)
		{
			System.out.println("Received Minor heartbeat from " + msg.getMessageFrom());
			updateChunkServerInfos(msg);
			updateChunkStorageInformation(msg);
			updateHeartBeatTracker(msg.getMessageFrom());
			printCurrentSituaton();
			// TODO Process the message
		}
		else if(msg.getMessageType() == MessageType.REQUEST_VALID_CHUNK_LOCATION)
		{
			System.out.println("Received valid chunk location request from " + msg.getMessageFrom());
			String chunkName = ((RequestValidChunkLocation_CS_CL)msg).getChunkName();
			ArrayList<String> csList = chunkStorageInfo.get(chunkName);
			csList.remove(socket.getInetAddress().getHostAddress());
			ChunkAllLocation allChunkLocations = new ChunkAllLocation(csList, ControlNode.IP_ADDRESS);
			sendMessage(allChunkLocations);
			System.out.println("Sent valid chunk locations to " + msg.getMessageFrom());
		}
		else
		{
			System.out.println("Could not understand the received messsage from " + msg.getMessageFrom()
			+ "." + msg);
		}
	}
	
	
	private MessageType findFreeChunkServers(int fileSize)
	{
		if(fileSize < 0)
		{
			String s = "File size can not be negative";
			System.out.println(s);
			return new ErrorMessage(s, ControlNode.IP_ADDRESS);
		}
		int numberOfChunks = calculateNumberOfChunks(fileSize);
		FreeChunkServerList chunkServerList = getFreeChunkServerList(numberOfChunks);
		return chunkServerList;
	}
	
	private FreeChunkServerList getFreeChunkServerList(int numberOfChunks)
	{
		FreeChunkServerList chunkServerList = new FreeChunkServerList(ControlNode.IP_ADDRESS, chunkServerInfos);
		return chunkServerList;
	}
	
	private int calculateNumberOfChunks(int fileSize)
	{
		if(fileSize <= 0)
			return 0;
		return (int) Math.ceil((fileSize / (double)ControlNode.CHUNK_SIZE_BYTES));
	}
	
	private void printCurrentSituaton()
	{
		System.out.println("---------------------------------\n"
				+ "Chunk Server Infos");
		for(ChunkServerInfo csi: chunkServerInfos)
			System.out.println(csi);
		System.out.println();
		
		System.out.println("Chunk Infos");
		System.out.println(chunkStorageInfo);
		System.out.println("---------------------------------\n");
	}
	
}
