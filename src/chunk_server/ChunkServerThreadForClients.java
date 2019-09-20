package chunk_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import messages.ErrorMessage;
import messages.FileDownload_CS_CL;
import messages.FileUpload_CL_CS;
import messages.MessageType;
import messages.RequestChunkData_CL_CS;
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
		chunkServerInstance.reduceFreeSpace(fileObject.getFileChunk().getTotalLength());
		return fileHandler.storeFileChunk(fileObject.getFileChunk());
	}
	
	private void forwardToOtherChunkServers(FileUpload_CL_CS fileObject)
	{
		// TODO implement this
	}
	
	private String getChunkFileName(String fileName, int chunkIndex)
	{
		return fileName + ChunkServer.chunkNameSeperator + chunkIndex;
	}
	
	private MessageType retrieveChunk(String chunkFileName)
	{
		Chunk chunk = fileHandler.retrieveFileChunk(chunkFileName);
		if(chunk == null)
		{
			String s = "Can not find chunk named " + chunkFileName;
			System.out.println(s);
			return new ErrorMessage(s, chunkServerInstance.getIpAddress());
		}
		chunk.prepareChunkBeforeSendingToClient();
		
		// TODO handle if chunk data is compromised
		
		return new FileDownload_CS_CL(chunk, chunkServerIpAddress);
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
		else if(msg.getMessageType() == MessageType.REQUEST_CHUNK_DATA_TO_CHUNK_SERVER)
		{
			RequestChunkData_CL_CS request = (RequestChunkData_CL_CS) msg;
			String chunkFileName = getChunkFileName(request.getFileName(), request.getChunkIndex());
			MessageType chunkDataMsg = retrieveChunk(chunkFileName);
			return chunkDataMsg;
		}
		else
		{
			System.out.println("Could not understand the message from " + clientAddress);
			return new ErrorMessage("Could not understand the type of message", chunkServerIpAddress);
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
				
				objectOutputStreamClients.writeObject(responseMsg);
				objectOutputStreamClients.flush();
				Thread.sleep(100);
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
