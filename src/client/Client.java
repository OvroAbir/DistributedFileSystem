package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import TCP.UtilityMethods;
import chunk_server.Chunk;
import chunk_server.ChunkServer;
import control_node.ControlNode;
import messages.FileUploadRequest_CL_CN;
import messages.FileUpload_CL_CS;
import messages.FreeChunkServerList;
import messages.MessageType;

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
		this("not provided");
	}
	
	private void whatUserWantToDo()
	{
		System.out.println("What do you want to do?");
		System.out.println("Enter " + WANT_TO_UPLOAD_FILE + " to upload a file. "
				+ "Or enter " + WANT_TO_DOWNLOAD_FILE + " to download a file.");
		Scanner scanner = new Scanner(System.in);
		currentChoice = scanner.nextInt();
		
		if(currentChoice != WANT_TO_DOWNLOAD_FILE && currentChoice != WANT_TO_UPLOAD_FILE)
		{
			System.out.println("Wrong choice. Enter again.");
			whatUserWantToDo();
			return;
		}
		
		System.out.println("Enter the file name ");
		currentFullFileName = scanner.nextLine();
		
		if(currentChoice == WANT_TO_UPLOAD_FILE)
		{
			System.out.println("Enter the path to the file ");
			currentFullFilePath = scanner.nextLine();
			
			File file = new File(currentFullFilePath);
			if(file.exists() == false || file.isDirectory())
			{
				System.out.println("Can not find the file. Enter again.");
				whatUserWantToDo();
				return;
			}
		}
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
				{
					uploadAFullFile();
				}
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Problem creating socket.");
			e.printStackTrace();
		}
	}
	

	private void uploadAFullFile()
	{
		String filePath = currentFullFilePath;
		File file = new File(filePath);
		
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader br;
		boolean errorOccured = false;
		
		try {
			br = new BufferedReader(new FileReader(file));
			String tempStr, content;
			int chunkIndex = 0;
			MessageType msgType;
			
			while ((tempStr = br.readLine()) != null) // TODO change if there is no new line 
			{
				stringBuilder.append(tempStr);
				if(stringBuilder.length() >= ControlNode.CHUNK_SIZE_BYTES)
				{
					content = stringBuilder.toString().substring(0, ControlNode.CHUNK_SIZE_BYTES);
					
					FileUploadRequest_CL_CN fileUploadReqMsg = new FileUploadRequest_CL_CN(content.length(),
							ipAddress);
					sendMessageToControllerNode(fileUploadReqMsg);
					
					msgType = (MessageType) objectInputStreamWithControlNode.readObject();
					FreeChunkServerList freeChunkServerListMsg = null;
					
					if(msgType instanceof FreeChunkServerList)
						freeChunkServerListMsg = (FreeChunkServerList) msgType;
					else
					{
						System.out.println("FreeChunkServerList not received. Unexpected message received");
						continue;
					}
					ArrayList<String> freeChunkServerList = freeChunkServerListMsg.getFreeChunkServerList();
					
					Chunk chunk = new Chunk(currentFullFileName, content, chunkIndex);
					
					FileUpload_CL_CS fileUploadMsg = new FileUpload_CL_CS(ipAddress, chunk, freeChunkServerList);
					
					sendMessageToChunkServer(fileUploadMsg, freeChunkServerList.get(0));
					
					stringBuilder.delete(0, ControlNode.CHUNK_SIZE_BYTES);
					content = null;
					chunkIndex++;
				}
			}
		} catch (FileNotFoundException e) {
			errorOccured = true;
			System.out.println("Could not find file " + currentFullFileName);
			e.printStackTrace();
		} catch (IOException e) {
			errorOccured = true;
			System.out.println("Could not read the file " + currentFullFileName);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			errorOccured = true;
			System.out.println("Can not cast messagetype");
			e.printStackTrace();
		}
		
		if(errorOccured == false)
			System.out.println("File successfully uploaded.");
		else
			System.out.println("There were some error uploading the file.");
	}
	
	private void sendMessageToChunkServer(MessageType msg, String chunkServerAddress)
	{
		try {
			Socket socketWithChunkServer = new Socket(chunkServerAddress, ChunkServer.CHUNK_SERVER_SOCKET_PORT_FOR_CLIENTS);
			DataOutputStream dataOutputStreamWithChunkServer = new DataOutputStream(socketWithChunkServer.getOutputStream());
			DataInputStream dataInputStreamWithChunkServer = new DataInputStream(socketWithChunkServer.getInputStream());
			
			ObjectOutputStream objectOutputStreamWithChunkServer = new ObjectOutputStream(dataOutputStreamWithChunkServer);
			ObjectInputStream objectInputStreamWithChunkServer = new ObjectInputStream(dataInputStreamWithChunkServer);
			
			objectOutputStreamWithChunkServer.writeObject(msg);
			
			objectOutputStreamWithChunkServer.flush();
			
			objectOutputStreamWithChunkServer.close();
			objectInputStreamWithChunkServer.close();
			dataOutputStreamWithChunkServer.close();
			dataInputStreamWithChunkServer.close();
			socketWithChunkServer.close();
			
		} catch (UnknownHostException e) {
			System.out.println("Can not connect with Chunk Server " + chunkServerAddress);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can not get object stream for " + chunkServerAddress);
			e.printStackTrace(); 
		}
		
	}
}
