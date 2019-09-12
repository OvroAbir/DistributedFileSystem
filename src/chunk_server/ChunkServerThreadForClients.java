package chunk_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import messages.ErrorMessage;
import messages.FileUpload_CL_CS;
import messages.MessageType;
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
		clientAddress = s.getInetAddress().toString();
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
		// TODO add this file to newly added and whole list
		return fileHandler.storeFileChunk(fileObject.getFileChunk());
		
	}
	
	private void forwardToOtherChunkServers(FileUpload_CL_CS fileObject)
	{
		// TODO implement this
	}
	private MessageType processReceivedMessage(MessageType msg)
	{
		if(msg == null)
			return new ErrorMessage("Message Field is null", clientAddress);
		else if(msg.getMessageType() == MessageType.UPLOAD_FILE_CL_CS)
		{
			FileUpload_CL_CS fileObject = (FileUpload_CL_CS) msg;
			if(storeFileChunk(fileObject) == false)
				return new ErrorMessage("Some error happened while storing the file.", chunkServerIpAddress);
			fileObject.removeFirstElementFromChunkServerList();
			if(fileObject.needToSendAnotherChunkServer())
				forwardToOtherChunkServers(fileObject);
			return new SuccessMessage(chunkServerIpAddress);
		}
		else
		{
			System.out.println("Could not understand the message from " + clientAddress);
			return new ErrorMessage("Could nt understand the type of message", chunkServerIpAddress);
		}
	}
	
	
	public void run()
	{
		MessageType inComingMsg;
		while(true)
		{
			try {
				inComingMsg = (MessageType) objectInputStreamClients.readObject();
				// TODO handle what to do with this message
			}
			catch (IOException | ClassNotFoundException e) {
				System.out.println("Could not receieve message");
				e.printStackTrace();
			}
		}
	}
}
