package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import ReedSolomonEntity.ReedSolomonHelper;
import ReedSolomonEntity.Shard;
import TCP.UtilityMethods;
import chunk_server.Chunk;
import chunk_server.ChunkServer;
import control_node.ControlNode;
import messages.ErrorMessage;
import messages.FileDownload_CS_CL;
import messages.FileStoringChunkServerList;
import messages.FileUploadRequest_CL_CN;
import messages.FileUpload_CL_CS;
import messages.FreeChunkServerList;
import messages.MessageType;
import messages.RequestChunkData_CL_CS;
import messages.RequestFileLocation_CL_CN;

public class Client 
{
	private String ipAddress;
	private Socket socketWithControlNode;
	private DataInputStream dataInputStreamWithControlNode;
	private DataOutputStream dataOutputStreamWithControlNode;
	private ObjectOutputStream objectOutputStreamWithControlNode;
	private ObjectInputStream objectInputStreamWithControlNode;
	
	private static int WANT_TO_UPLOAD_FILE = 1;
	private static int WANT_TO_DOWNLOAD_FILE = 2;
	private static int WANT_TO_EXIT = 3;
	
	private Socket socketWithChunkServer;
	private DataInputStream dataInputStreamWithChunkServer;
	private DataOutputStream dataOutputStreamWithChunkServer;
	private ObjectInputStream objectInputStreamWithChunkServer;
	private ObjectOutputStream objectOutputStreamWithChunkServer;
	
	private static String DEFAULT_FILE_DOWNLOAD_LOCATION = "ClientDownloadFolder";
	
	private String currentFullFileName, currentFullFilePath;
	private int currentChoice;
	
	public Client(String ipAddress)
	{
		this.ipAddress = ipAddress;
		try {
			socketWithControlNode = new Socket(ControlNode.IP_ADDRESS, ControlNode.PORT);
			System.out.println("Connected with Control node.");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startClient();
	}
	
	public Client()
	{
		this(" [client address not provided]");
	}
	
	private void whatUserWantToDo()
	{
		System.out.println("What do you want to do?");
		System.out.println("Enter " + WANT_TO_UPLOAD_FILE + " to upload a file. "
				+ "Or enter " + WANT_TO_DOWNLOAD_FILE + " to download a file."
						+ "Or enter " + WANT_TO_EXIT + " to exit.");
		
		Scanner scanner = new Scanner(System.in);
		currentChoice = Integer.parseInt(scanner.nextLine());
		
		
		if(currentChoice != WANT_TO_DOWNLOAD_FILE && currentChoice != WANT_TO_UPLOAD_FILE 
				&& currentChoice != WANT_TO_EXIT)
		{
			System.out.println("Wrong choice. Enter again.");
			whatUserWantToDo();
			return;
		}
		
		System.out.print("Enter the file name : ");
		currentFullFileName = scanner.nextLine();
		
		if(currentChoice == WANT_TO_UPLOAD_FILE)
		{
			System.out.print("Enter the path to the file : ");
			currentFullFilePath = scanner.nextLine();
			
			File file = new File(currentFullFilePath);
			if(file.exists() == false || file.isDirectory())
			{
				System.out.println("Can not find the file. Enter again.");
				whatUserWantToDo();
				return;
			}
		}
		else if(currentChoice == WANT_TO_DOWNLOAD_FILE)
		{
			System.out.println("Downloading your file.");
		}
		else if(currentChoice == WANT_TO_EXIT)
			return;
	}
	
	private void sendMessageToControllerNode(MessageType msg)
	{
		try {
			objectOutputStreamWithControlNode.writeObject(msg);
			objectOutputStreamWithControlNode.flush();
		} catch (IOException e) {
			System.out.println("Can not send message to controller from client");
			e.printStackTrace();
		}
	}
	
	public void startClient()
	{
		try 
		{
			dataInputStreamWithControlNode = new DataInputStream(socketWithControlNode.getInputStream());
			dataOutputStreamWithControlNode = new DataOutputStream(socketWithControlNode.getOutputStream());
			objectOutputStreamWithControlNode = new ObjectOutputStream(dataOutputStreamWithControlNode);
			objectInputStreamWithControlNode = new ObjectInputStream(dataInputStreamWithControlNode);
			
			MessageType inComingMsg, outGoingMsg;
			
			while(true)
			{
				whatUserWantToDo();
				
				if(currentChoice == WANT_TO_UPLOAD_FILE)
					uploadAFullFile();
				else if(currentChoice == WANT_TO_DOWNLOAD_FILE)
				{
					String fullFilePath = downloadAFullFile();
					if(fullFilePath == null)
						System.out.println(currentFullFileName + " file not found.");
					System.out.println("File has been downloaded to " + fullFilePath);
				}
				
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Problem creating socket.");
			e.printStackTrace();
		}
	}
	
	private ArrayList<String> getStoringChunkServerListFromController(String fileName, int chunkIndex)
	{
		MessageType askControlNode = new RequestFileLocation_CL_CN(fileName, chunkIndex, ipAddress);
		sendMessageToControllerNode(askControlNode);
		FileStoringChunkServerList chunkServerListMsg = null;
		
		try {
			MessageType rcvdMsg = (MessageType) objectInputStreamWithControlNode.readObject();
			if(rcvdMsg.getMessageType() != MessageType.CHUNK_SERVER_LIST_FOR_STORED_FILE)
			{
				System.out.println("Did not understand response from control node");
				return null;
			}
			chunkServerListMsg = (FileStoringChunkServerList) rcvdMsg;
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> chunkServerlist = chunkServerListMsg.getChunkServerList();
		
		return chunkServerlist;
	}
	
	private String concatFilePath(String folder, String filename)
	{
		return folder + File.separator + filename;
	}
	
	private String appendDataToFile(String fileName, String data)
	{
		File file = new File(fileName);
		BufferedWriter out;
		
		try {
			if(file.getParentFile().exists() == false)
				file.getParentFile().mkdirs();
			if(file.exists() == false)
				file.createNewFile();
		
			out = new BufferedWriter(new FileWriter(file, true));
			out.write(data);
			out.flush();
			
			out.close(); 
			
		} catch (IOException e) {
			System.out.println("Could not write data to file " + fileName);
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}
	
	private String getAChunkFromChunkServer(String fileName, int shardIndex, String chunkServerAddress)
	{
		MessageType request = new RequestChunkData_CL_CS(fileName, shardIndex, ipAddress);
		System.out.println("Requesting chunk " + fileName + ":" + shardIndex + " from " + chunkServerAddress );
		sendMessageToChunkServer(request, chunkServerAddress);
		
		MessageType rcvdMsg = receieveMessageFromChunkServer(chunkServerAddress);
		while(rcvdMsg.getMessageType() != MessageType.DOWNLOAD_CHUNK_CS_CL)
		{
			if(rcvdMsg.getMessageType() == MessageType.FILE_DATA_CHANGED_SO_WAIT_CS_CL)
			{
				System.out.println(fileName + " (shard "+ shardIndex +") was corrupted in " + chunkServerAddress +". Waiting...");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				rcvdMsg = receieveMessageFromChunkServer(chunkServerAddress);
				if(rcvdMsg.getMessageType() == MessageType.DOWNLOAD_CHUNK_CS_CL)
				{
					System.out.println("Got the valid chunk for " + fileName + "(shard " + shardIndex + ") from " + rcvdMsg.getMessageFrom());
					break;
				}
			}
			else if(rcvdMsg.getMessageType() == MessageType.DOWNLOAD_CHUNK_CS_CL)
			{
				System.out.println("Got the valid chunk for " + fileName + "(shard " + shardIndex + ").");
				break;
			}
			else 
			{
				System.out.println("Could not understand msg (type " + rcvdMsg.getMessageType() + " ) from Chunk Server " + chunkServerAddress);
				return null;
			}
		}
		
		FileDownload_CS_CL fileDownloadMsg = (FileDownload_CS_CL) rcvdMsg;
		String data = fileDownloadMsg.getFileChunk().getData();
		
		return data;
	}
	private String downloadAFullFile()
	{
		String fileName = currentFullFileName;
		String chunkFileName;

		String filePath = concatFilePath(DEFAULT_FILE_DOWNLOAD_LOCATION, fileName);
		String absPath = filePath;
		
		
		for(int chunkIndex=0;;chunkIndex++) 
		{
			ArrayList<String> chunkServerList = getStoringChunkServerListFromController(fileName, chunkIndex);
			if(chunkServerList == null)
				return null;
			if(chunkServerList.isEmpty())
				break;
			chunkFileName = fileName + ChunkServer.chunkNameSeperator + chunkIndex;
			ArrayList<Shard> shards = new ArrayList<Shard>();
			for(int shard=0;shard<chunkServerList.size();shard++)
			{
				String chunkServerAddress = chunkServerList.get(shard);
				openConnectionWithChunkServer(chunkServerAddress);
				String shardData = getAChunkFromChunkServer(chunkFileName, shard, chunkServerAddress);
				Shard shardObject = Shard.getShardObjectFromString(shardData);
				shards.add(shardObject);
				closeConnectionWithChunkServer();
			}
			String chunkData = ReedSolomonHelper.decode(shards);
			absPath = appendDataToFile(filePath, chunkData);
		}
		
		return absPath;
	}

	private void uploadAFullFile()
	{
		String filePath = currentFullFilePath;
		File file = new File(filePath);
		
		BufferedReader br;
		boolean errorOccured = false;
		
		try {
			br = new BufferedReader(new FileReader(file));
			String tempStr, content, chunkServerAddress;
			int chunkIndex = 0;
			MessageType msgType;
			
			StringBuilder tempStringBuilder;
			int result = 1, charRead;
			char ch;
			
			while (result != -1)
			{
				tempStringBuilder = new StringBuilder();
				charRead = 0;

				while(charRead < ControlNode.CHUNK_SIZE_BYTES)
				{
					result = br.read();
					if(result == -1)
						break;
					ch = (char) result;
					tempStringBuilder.append(ch);
					charRead++;
				}
				
				content = tempStringBuilder.toString();
				
				ArrayList<String> freeChunkServerList = getFreeChunkServerListFromController(content.length());
				
				ArrayList<Chunk> chunks = ReedSolomonHelper.getEncodedChunks(currentFullFileName, content, chunkIndex);
				for(Chunk chunk : chunks)
				{
					if(freeChunkServerList.isEmpty())
						freeChunkServerList = getFreeChunkServerListFromController(content.length());
					ArrayList<String> forwardingList = new ArrayList<String>();
					
					String chunkServerToSend = freeChunkServerList.get(0);
					
					forwardingList.add(chunkServerToSend);
					FileUpload_CL_CS fileUploadMsg = new FileUpload_CL_CS(ipAddress, chunk, forwardingList);
					
					openConnectionWithChunkServer(chunkServerToSend);
					sendMessageToChunkServer(fileUploadMsg, chunkServerToSend);
					closeConnectionWithChunkServer();
				}
				
				content = null;
				chunkIndex++;
			}
		} catch (FileNotFoundException e) {
			errorOccured = true;
			System.out.println("Could not find file " + currentFullFileName);
			e.printStackTrace();
		} catch (IOException e) {
			errorOccured = true;
			System.out.println("Could not read the file " + currentFullFileName);
			e.printStackTrace();
		}
		
		if(errorOccured == false)
			System.out.println("File successfully uploaded.");
		else
			System.out.println("There were some error uploading the file.");
	}
	
	private ArrayList<String> getFreeChunkServerListFromController(int contentLength)
	{
		FileUploadRequest_CL_CN fileUploadReqMsg = new FileUploadRequest_CL_CN(contentLength, ipAddress);
		sendMessageToControllerNode(fileUploadReqMsg);
		
		MessageType msgType;
		try {
			msgType = (MessageType) objectInputStreamWithControlNode.readObject();
			FreeChunkServerList freeChunkServerListMsg = null;
			
			if(msgType instanceof FreeChunkServerList)
				freeChunkServerListMsg = (FreeChunkServerList) msgType;
			else
			{
				System.out.println("FreeChunkServerList not received. Unexpected message received");
				return new ArrayList<String>();
			}
			ArrayList<String> freeChunkServerList = freeChunkServerListMsg.getFreeChunkServerList();
			return freeChunkServerList;
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		return new ArrayList<String>();
	}
	private MessageType receieveMessageFromChunkServer(String chunkServerAddress)
	{
		MessageType rcvdMsg = null;
		try {
			rcvdMsg = (MessageType) objectInputStreamWithChunkServer.readObject();
		}
		catch(EOFException e){
			System.out.println("Got EOF exception.");
			e.printStackTrace();
		}
		catch (UnknownHostException e) {
			System.out.println("Can not connect with Chunk Server " + chunkServerAddress);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can not get object stream for " + chunkServerAddress);
			e.printStackTrace(); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		if(rcvdMsg == null)
			System.out.println("Received message is null");
		
		if(rcvdMsg.getMessageType() == MessageType.ERROR_MESSAGE)
			System.out.println(((ErrorMessage)rcvdMsg).getErrorMessage());
		
		return rcvdMsg;
	}
	
	private void sendMessageToChunkServer(MessageType msg, String chunkServerAddress)
	{
		try {
			objectOutputStreamWithChunkServer.writeObject(msg);
			objectOutputStreamWithChunkServer.flush();
		} catch (UnknownHostException e) {
			System.out.println("Can not connect with Chunk Server " + chunkServerAddress);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can not get object stream for " + chunkServerAddress);
			e.printStackTrace(); 
		}
		
	}
	

	private void openConnectionWithChunkServer(String chunkServerAddress)
	{
		if(socketWithChunkServer != null)
		{
			System.out.println("Caution : Socket with chunk server already exists");
			//return;
		}
		try {
			socketWithChunkServer = new Socket(chunkServerAddress, ChunkServer.CHUNK_SERVER_SOCKET_PORT_FOR_CLIENTS);
			dataOutputStreamWithChunkServer = new DataOutputStream(socketWithChunkServer.getOutputStream());
			dataInputStreamWithChunkServer = new DataInputStream(socketWithChunkServer.getInputStream());
			
			objectOutputStreamWithChunkServer = new ObjectOutputStream(dataOutputStreamWithChunkServer);
			objectInputStreamWithChunkServer = new ObjectInputStream(dataInputStreamWithChunkServer);
		} catch (UnknownHostException e) {
			System.out.println("Can not connect with Chunk Server " + chunkServerAddress);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can not get object stream for " + chunkServerAddress);
			e.printStackTrace(); 
		}
		
	}
	
	private void closeConnectionWithChunkServer()
	{
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(socketWithChunkServer == null)
			return;
		try {
			objectOutputStreamWithChunkServer.close();
			objectInputStreamWithChunkServer.close();
			dataOutputStreamWithChunkServer.close();
			dataInputStreamWithChunkServer.close();
			socketWithChunkServer.close();
		} catch (IOException e) {
			//System.out.println("Exception while closing connection with client");
			//e.printStackTrace();
		}
		socketWithChunkServer = null;
		dataOutputStreamWithChunkServer = null;
		dataInputStreamWithChunkServer = null;
		objectInputStreamWithChunkServer = null;
		objectOutputStreamWithChunkServer = null;
	}
}
