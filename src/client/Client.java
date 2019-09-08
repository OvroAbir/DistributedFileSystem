package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import TCP.UtilityMethods;
import control_node.ControlNode;
import messages.FileUploadRequest_CL_CN;
import messages.MessageType;

public class Client 
{
	private Socket socketWithControlNode;
	private DataInputStream dataInputStreamWithControlNode;
	private DataOutputStream dataOutputStreamWithControlNode;
	private ObjectOutputStream objectOutputStreamWithControlNode;
	private ObjectInputStream objectInputStreamWithControlNode;
	
	public Client()
	{
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
				// TODO Will need to change
				outGoingMsg = new FileUploadRequest_CL_CN(800);
				objectOutputStreamWithControlNode.writeObject(outGoingMsg);
				
				inComingMsg = (MessageType) objectInputStreamWithControlNode.readObject();
				System.out.println("Server : " + inComingMsg);
				resolveReceivedMessage(inComingMsg);
				
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Problem creating socket.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Received object missmatch");
			e.printStackTrace();
		}
	}
	
	private void resolveReceivedMessage(MessageType msg)
	{
		if(msg.getMessageType() == MessageType.FREE_CHUNK_SERVER_LIST_CN_CL)
		{
			// TODO Complete this block
			System.out.println("Got free Chunk Server list");
		}
		else
		{
			System.out.println("Can not resolve received Message.");
		}
	}
}
