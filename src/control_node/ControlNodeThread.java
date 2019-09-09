package control_node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import messages.ErrorMessage;
import messages.FileUploadRequest_CL_CN;
import messages.MessageType;

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
	
	public ControlNodeThread(Socket s, int threadId)
	{
		socket = s;
		this.threadId = threadId;
		clientAddress = s.getInetAddress().toString() + " (" + this.threadId + ") ";
		chunkServerInfos = ControlNode.chunkServerInfos;
		
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
	
	private void sendMessage(MessageType msg)
	{
		try {
			objectOutputStream.writeObject(msg);
		} catch (IOException e) {
			System.out.println("Can not send message.");
			e.printStackTrace();
		}
	}
	
	private void resolveReceivedMessage(MessageType msg)
	{
		if(msg.getMessageType() == MessageType.UPLOAD_FILE_REQ_CL_CN)
		{
			System.out.println("Received file upload request from client " + msg.getMessageFrom());
			MessageType chunkServerList = findFreeChunkServers(((FileUploadRequest_CL_CN) msg).getFileSize());
			sendMessage(chunkServerList);
		}
		else if(msg.getMessageType() == MessageType.MAJOR_HEARTBEAT_CS_CN)
		{
			System.out.println("Received Major heartbeat from " + msg.getMessageFrom());
			// TODO Process the message
		}
		else if(msg.getMessageType() == MessageType.MINOR_HEARTBEAT_CS_CN)
		{
			System.out.println("Received Minor heartbeat from " + msg.getMessageFrom());
			// TODO Process the message
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
		FreeChunkServerList chunkServerList = new FreeChunkServerList(numberOfChunks, ControlNode.IP_ADDRESS);
		chunkServerList.setFreeServers(chunkServerInfos);
		return chunkServerList;
	}
	
	private int calculateNumberOfChunks(int fileSize)
	{
		if(fileSize <= 0)
			return 0;
		return (int) Math.ceil((fileSize / (double)ControlNode.CHUNK_SIZE_BYTES));
	}
	
}
