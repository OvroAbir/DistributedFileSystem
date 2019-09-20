package chunk_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import messages.ErrorMessage;
import messages.FileUpload_CL_CS;
import messages.MessageType;
import messages.RequestFreshChunkCopy;

public class ChunkServerThreadForChunkServers extends Thread
{
	private Socket socketForChunkServers;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private ObjectInputStream objectInputStreamCS;
	private ObjectOutputStream objectOutputStreamCS;
	
	private String thisCSAddress;
	private int threadId;
	private String incomingChunkServerIpAddress;
	private FileStoreAndRetrieveHandler fileHandler;
	private ChunkServer chunkServerInstance;

	public ChunkServerThreadForChunkServers(Socket socketForChunkSevrs, String ipAddress, int chunkServerThreadCounter, ChunkServer chunkServerInstance) 
	{
		this.socketForChunkServers = socketForChunkSevrs;
		this.thisCSAddress = ipAddress;
		this.threadId = chunkServerThreadCounter;
		this.chunkServerInstance = chunkServerInstance;
		this.incomingChunkServerIpAddress = socketForChunkSevrs.getInetAddress().getHostAddress();
		this.fileHandler = new FileStoreAndRetrieveHandler(chunkServerInstance);
		
		try 
		{
			dataInputStream = new DataInputStream(socketForChunkServers.getInputStream());
			dataOutputStream = new DataOutputStream(socketForChunkServers.getOutputStream());
			objectInputStreamCS = new ObjectInputStream(dataInputStream);
			objectOutputStreamCS = new ObjectOutputStream(dataOutputStream);
		} 
		catch (IOException e) 
		{
			System.out.println("Can not get Data Stream for " + incomingChunkServerIpAddress);
			e.printStackTrace();
		}
		
	}

	
	private void sendReplyToChunkSever(MessageType msg)
	{
		try {
			objectOutputStreamCS.writeObject(msg);

			objectOutputStreamCS.flush();
		} catch (IOException e) {
			System.out.println("Could not send data");
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try {
			MessageType inComingMsg = (MessageType) objectInputStreamCS.readObject();
			
			if(inComingMsg.getMessageType() == MessageType.UPLOAD_FILE_CL_CS)
			{
				FileUpload_CL_CS fileUploadMsg = (FileUpload_CL_CS) inComingMsg;
				fileUploadMsg.removeFirstElementFromChunkServerList();
				
				Chunk chunk = fileUploadMsg.getFileChunk();
				fileHandler.storeFileChunk(chunk);
				
				if(fileUploadMsg.needToSendAnotherChunkServer())
					chunkServerInstance.forwardFileUploadMessageToAnotherChunkServer(fileUploadMsg);
			}
			else if(inComingMsg.getMessageType() == MessageType.REQUEST_FRESH_CHUNK_COPY_CS_CS)
			{
				String reqChunkName = ((RequestFreshChunkCopy)inComingMsg).getChunkName();
				Chunk chunk = fileHandler.retrieveFileChunk(reqChunkName);
				if(chunk == null)
					sendReplyToChunkSever(new ErrorMessage("Chunk " + reqChunkName +" is not stored in me.",
							chunkServerInstance.getIpAddress()));
				else
				{
					chunk.prepareChunkBeforeSendingToClient();
					FileUpload_CL_CS fileUpload = new FileUpload_CL_CS(chunkServerInstance.getIpAddress(), chunk,
							new ArrayList<String>());
					sendReplyToChunkSever(fileUpload);
					chunk.storeRealDataInDisk(ChunkServer.FILE_STORAGE_FOLDER_LOCATION);
				}
				
			}
			// TODO for receive req to rplace corrupted chunk in another cs
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		terminateConnection();
	}
	
	private void terminateConnection()
	{
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Connection is being terminated with CS " + incomingChunkServerIpAddress);
		try {
			objectInputStreamCS.close();
		} catch (Exception e) {
			System.out.println("Exception while closing objectInputStream with CS.");
			//e.printStackTrace();
		}
		try {
			objectOutputStreamCS.close();
		} catch (Exception e) {
			System.out.println("Exception while closing ObjectOutputStream with CS.");
			//e.printStackTrace();
		}
		try {
			dataInputStream.close();
		} catch (Exception e) {
			System.out.println("Exception while closing dataInputStream with CS.");
			//e.printStackTrace();
		}
		try {
			dataOutputStream.close();
		} catch (Exception e) {
			System.out.println("Exception while closing dataOutputStream with CS.");
			//e.printStackTrace();
		}
		try {
			socketForChunkServers.close();
		} catch (Exception e) {
			System.out.println("Exception while closing socket with CS.");
			//e.printStackTrace();
		}

	}
}