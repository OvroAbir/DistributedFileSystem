package chunk_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import ReedSolomonEntity.ReedSolomonHelper;
import ReedSolomonEntity.Shard;
import erasure.ReedSolomon;
import exceptions.FileDataChanged;
import messages.ChunkAllLocation;
import messages.ErrorMessage;
import messages.FileDataChangedSoWait;
import messages.FileDownload_CS_CL;
import messages.FileUpload_CL_CS;
import messages.MessageType;
import messages.RequestChunkData_CL_CS;
import messages.RequestFreshChunkCopy;
import messages.RequestValidChunkLocation_CS_CL;
import messages.SuccessMessage;

public class ChunkServerThreadForClients extends Thread
{
	private Socket socketForClients;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private ObjectInputStream objectInputStreamClients;
	private ObjectOutputStream objectOutputStreamClients;
	
	private String clientAddress;
	private int threadId;
	private String chunkServerIpAddress;
	private FileStoreAndRetrieveHandler fileHandler;
	private ChunkServer chunkServerInstance;
	
	public ChunkServerThreadForClients(Socket s, String ipAddress, int threadId, ChunkServer chunkServerInstance)
	{
		socketForClients = s;
		this.threadId = threadId;
		clientAddress = s.getInetAddress().getHostAddress();
		chunkServerIpAddress = ipAddress;
		this.chunkServerInstance = chunkServerInstance;
		fileHandler = new FileStoreAndRetrieveHandler(chunkServerInstance);
		
		try 
		{
			dataInputStream = new DataInputStream(s.getInputStream());
			dataOutputStream = new DataOutputStream(s.getOutputStream());
			objectInputStreamClients = new ObjectInputStream(dataInputStream);
			objectOutputStreamClients = new ObjectOutputStream(dataOutputStream);
		} 
		catch (IOException e) 
		{
			System.out.println("Can not get Data Stream for " + clientAddress);
			e.printStackTrace();
		}
		
	}
	
	
	private boolean storeFileChunk(FileUpload_CL_CS fileObject)
	{
		//chunkServerInstance.reduceFreeSpace(fileObject.getFileChunk().getTotalLength());
		return fileHandler.storeFileChunk(fileObject.getFileChunk());
	}
	
	private void forwardToOtherChunkServers(FileUpload_CL_CS fileObject)
	{
		chunkServerInstance.forwardFileUploadMessageToAnotherChunkServer(fileObject);
		// TODO implement this
	}
	
	private String getChunkFileWithShardName(String fileName, int shardIndex)
	{
		return fileName + ChunkServer.shardIndexSeperator + shardIndex;
	}
	
	private MessageType retrieveChunk(String chunkFileName) throws FileDataChanged
	{
		Chunk chunk = fileHandler.retrieveFileChunk(chunkFileName);
		if(chunk == null)
		{
			String s = "Can not find chunk named " + chunkFileName;
			System.out.println(s);
			return new ErrorMessage(s, chunkServerInstance.getIpAddress());
		}
		
		chunk.prepareChunkBeforeSendingToClient();
		
		return new FileDownload_CS_CL(chunk, chunkServerIpAddress);
	}
	
	private MessageType processReceivedMessage(MessageType msg)
	{
		if(msg == null)
			return new ErrorMessage("Message Field is null", clientAddress);
		else if(msg.getMessageType() == MessageType.UPLOAD_FILE_CL_CS)
		{
			FileUpload_CL_CS fileObject = (FileUpload_CL_CS) msg;			
			fileObject.removeFirstElementFromChunkServerList();
			if(fileObject.needToSendAnotherChunkServer())
				forwardToOtherChunkServers(fileObject);
			if(storeFileChunk(fileObject) == false)
				return new ErrorMessage("Some error happened while storing the file.", chunkServerIpAddress);
			return new SuccessMessage(chunkServerIpAddress);
		}
		else if(msg.getMessageType() == MessageType.REQUEST_CHUNK_DATA_TO_CHUNK_SERVER)
		{
			RequestChunkData_CL_CS request = (RequestChunkData_CL_CS) msg;
			String chunkFileName = getChunkFileWithShardName(request.getFileName(), request.getShardIndex());
			MessageType chunkDataMsg;
			try {
				chunkDataMsg = retrieveChunk(chunkFileName);
			} catch (FileDataChanged e) {
				System.out.println("Detected corrupted data for " + e.getChunkName() + " : " + e.getSliceNum());
				FileDataChangedSoWait fileDataChangeMsg = new FileDataChangedSoWait(e.getChunkName(), e.getSliceNum(), chunkServerIpAddress);
				
				sendMessageToClient(fileDataChangeMsg);
				
				System.out.println("Told client to wait.");
				
				chunkDataMsg = retrieveValidChunkData(e.getChunkName(), e.getSliceNum(), request.getShardIndex());
				// TODO replace the corrupted chunk
				repairCorruptedChunk(((FileDownload_CS_CL)chunkDataMsg).getFileChunk(), e.getChunkName());
				System.out.println("Got valid chunk data for " + e.getChunkName());
			}
			return chunkDataMsg;
		}
		else
		{
			System.out.println("Could not understand the message from " + clientAddress);
			return new ErrorMessage("Could not understand the type of message", chunkServerIpAddress);
		}
	}
	
	private void repairCorruptedChunk(Chunk chunk, String chunkName)
	{
		fileHandler.replaceChunk(chunkName, chunk);
	}
	
	private MessageType retrieveValidChunkData(String chunkName, int sliceNum, int shardIndex)
	{
		// TODO handle slicenum
		System.out.println("ChunkServerThreadForClients.retrieveValidChunkData()");
		System.out.println("Got chunk name " + chunkName);
		RequestValidChunkLocation_CS_CL reqValidChunkMsg = new RequestValidChunkLocation_CS_CL(chunkName.substring(0, chunkName.lastIndexOf(ChunkServer.shardIndexSeperator)),
				chunkServerIpAddress);
		chunkServerInstance.sendMessageToControlNode(reqValidChunkMsg);
		MessageType replyFromControlNode = chunkServerInstance.receieveMessageFromControlNode();
		
		if(replyFromControlNode.getMessageType() != MessageType.VALID_CHUNK_LOCATIONS)
		{
			System.out.println("Unexpected message from Controller " + replyFromControlNode.getMessageType());
			return replyFromControlNode;
		}
		
		ArrayList<String> chunkLocations = ((ChunkAllLocation) replyFromControlNode).getOtherValidChunkLocationsAndFileName(chunkServerIpAddress);
		
		System.out.println("Got valid chunk locations from control node. " + chunkLocations);
		
		RequestFreshChunkCopy chunkRequest;
		MessageType replyFromCS = null;
		
		ArrayList<Shard> retrievedShards = new ArrayList<>();
		
		for(String validChunkAddressAndFileName : chunkLocations)
		{
			int sepIndex = validChunkAddressAndFileName.indexOf(ChunkServer.shardIndexSeperator);
			String address = validChunkAddressAndFileName.substring(0, sepIndex);
			String cName = validChunkAddressAndFileName.substring(sepIndex + 1);
			
			chunkRequest = makeRequestForValidChunk(validChunkAddressAndFileName);
			replyFromCS = sendAndGetReplyFromAnotherChunkServer(chunkRequest, address);
			if(replyFromCS.getMessageType() == MessageType.VALID_CHUNK_LOCATIONS)
			{
				System.out.println("Found valid chunk from " + validChunkAddressAndFileName);
				String shardData = ((FileUpload_CL_CS)replyFromCS).getFileChunk().getData();
				Shard shard = Shard.getShardObjectFromString(shardData);
				retrievedShards.add(shard);
			}
		}
		
		String realContent = ReedSolomonHelper.decode(retrievedShards);
		
		return new FileDownload_CS_CL(new Chunk(chunkName, Shard.getShardObjectFromString(realContent), getFragmentIndexFromChunkName(chunkName), shardIndex), chunkServerIpAddress);
	}
	
	private int getFragmentIndexFromChunkName(String name)
	{
		int idx = name.lastIndexOf(ChunkServer.chunkNameSeperator);
		return Integer.parseInt(name.substring(idx + 1));
	}
	
	private RequestFreshChunkCopy makeRequestForValidChunk(String addressAndFileName)
	{
		int sepIndex = addressAndFileName.indexOf(ChunkServer.shardIndexSeperator);
		String address = addressAndFileName.substring(0, sepIndex);
		String chunkName = addressAndFileName.substring(sepIndex + 1);
		System.out.println("Requesting " + address + " for " + chunkName);
		return new RequestFreshChunkCopy(chunkName, address);
	}
	
	private MessageType sendAndGetReplyFromAnotherChunkServer(MessageType msgToSend, String toChunkServerAddress)
	{
		MessageType rcvdMsg = null;
		try {
			Socket socket = new Socket(toChunkServerAddress, ChunkServer.CHUNK_SERVER_SOCKET_PORT_FOR_CHUNK_SERVERS);
			
			ObjectOutputStream objectOutputStreamWithCS = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objectInputStreamWithCS = new ObjectInputStream(socket.getInputStream());
			
			objectOutputStreamWithCS.writeObject(msgToSend);
			objectOutputStreamWithCS.flush();
			
			rcvdMsg = (MessageType) objectInputStreamWithCS.readObject();
			
			Thread.sleep(500);
			
			objectOutputStreamWithCS.close();
			objectInputStreamWithCS.close();
			socket.close();
			
		} catch (IOException e) {
			System.out.println("can not open socket with " + toChunkServerAddress);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return rcvdMsg;
	}
	
	private void sendMessageToClient(MessageType msg)
	{
		try {
			objectOutputStreamClients.writeObject(msg);
			objectOutputStreamClients.flush();
			Thread.sleep(100);
		} catch (IOException e) {
			System.out.println("Can not send message to client ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		MessageType inComingMsg, responseMsg;

		//while(true)
		//{
			try {
				inComingMsg = (MessageType) objectInputStreamClients.readObject();
				responseMsg = processReceivedMessage(inComingMsg);
				
				sendMessageToClient(responseMsg);
				// TODO handle what to do with this message
			}
			catch (Exception  e) { // TODO EOFException?
				System.out.println("Could not receieve message");
				e.printStackTrace();
			}
			terminateConnection();
		//}
	}

	private void terminateConnection() {
		System.out.println("Connection is being terminated with client " + clientAddress);
		try {
			objectInputStreamClients.close();
		} catch (Exception e) {
			System.out.println("Exception while closing objectInputStream with client.");
			//e.printStackTrace();
		}
		try {
			objectOutputStreamClients.close();
		} catch (Exception e) {
			System.out.println("Exception while closing ObjectOutputStream with client.");
			//e.printStackTrace();
		}
		try {
			dataInputStream.close();
		} catch (Exception e) {
			System.out.println("Exception while closing dataInputStream with client.");
			//e.printStackTrace();
		}
		try {
			dataOutputStream.close();
		} catch (Exception e) {
			System.out.println("Exception while closing dataOutputStream with client.");
			//e.printStackTrace();
		}
		try {
			socketForClients.close();
		} catch (Exception e) {
			System.out.println("Exception while closing socket with client.");
			//e.printStackTrace();
		}

	}
}
