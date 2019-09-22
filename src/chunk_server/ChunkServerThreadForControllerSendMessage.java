package chunk_server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import messages.FileUpload_CL_CS;
import messages.MessageType;
import messages.SendChunkCopyToAnotherChunksServer;

public class ChunkServerThreadForControllerSendMessage extends Thread{

	ChunkServer chunkServerInstance;
	ServerSocket serverSocketForController;
	public ChunkServerThreadForControllerSendMessage(ChunkServer chunkServerInstance) {
		this.chunkServerInstance = chunkServerInstance;
	}

	public void run()
	{
		try {
			serverSocketForController = new ServerSocket(ChunkServer.CHUNK_SERVER_SOCKET_PORT_FOR_CONTROL_NODE_SEND_CHUNK_MSG);
		} catch (IOException e) {
			System.out.println("Could not create server socket");
			e.printStackTrace();
		}
		
		while(true)
		{
			try {
				Socket socket = serverSocketForController.accept();
				ObjectInputStream objectInputStreamWithController = new ObjectInputStream(socket.getInputStream());
				SendChunkCopyToAnotherChunksServer msg = (SendChunkCopyToAnotherChunksServer) objectInputStreamWithController.readObject();
				sendToAnotherChunkServer(msg);
				
				sleep(100);
				
				objectInputStreamWithController.close();
				socket.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	private void sendToAnotherChunkServer(SendChunkCopyToAnotherChunksServer msg) 
	{
		ConcurrentHashMap<String, Chunk> hm = chunkServerInstance.getConcurrentHashMapForFiles();
		if(hm.contains(msg.getChunkName()) == false)
		{
			System.out.println("Could not find chunk to forward to another Chunk Server : " + msg.getChunkName());
			return;
		}
		
		Chunk chunk = hm.get(msg.getChunkName());
		
		ArrayList<String> listToSend = new ArrayList<String>();
		listToSend.add(msg.getSendTo());
		
		FileUpload_CL_CS fileUploadMsg = new FileUpload_CL_CS(chunkServerInstance.ipAddress, chunk, listToSend);
		
		chunkServerInstance.forwardFileUploadMessageToAnotherChunkServer(fileUploadMsg);
	}
}
