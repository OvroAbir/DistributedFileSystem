package TCP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread extends Thread
{
	private Socket socket;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private String clientAddress;
	private int threadId;
	
	public ServerThread(Socket s, int threadId)
	{
		socket = s;
		this.threadId = threadId;
		clientAddress = s.getInetAddress().toString() + " (" + this.threadId + ") ";
		
		try 
		{
			dataInputStream = new DataInputStream(s.getInputStream());
			dataOutputStream = new DataOutputStream(s.getOutputStream());
		} 
		catch (IOException e) 
		{
			System.out.println("Can not get Data Stream for " + clientAddress);
			e.printStackTrace();
		}
		
	}
	
	public void run()
	{
		System.out.println("Sever Thread started for client " + clientAddress);
		String inComingMsg, outGoingMsg;
		
		try
		{
			while(true)
			{
				inComingMsg = dataInputStream.readUTF();
				// TODO Change this read line
				System.out.println(clientAddress + " : " + inComingMsg);
				
				outGoingMsg = "Got your Message";
				dataOutputStream.writeUTF(outGoingMsg);
				
				Thread.sleep(5000);
			}
		}
		catch (IOException | InterruptedException e) {
			try {
				socket.close();
			} catch (IOException e1) 
			{
				e1.printStackTrace();
			}
			System.out.println("Problem reading data stream for " + clientAddress);
			e.printStackTrace();
		}
		
	}
}
