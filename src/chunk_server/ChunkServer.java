package chunk_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import control_node.ControlNode;
import messages.HeartBeat;
import messages.MajorHeartBeat;
import messages.MessageType;
import messages.MinorHeartBeat;

public class ChunkServer 
{
	public static int MINOR_HEART_BEAT_INTERVAL = 30000;
	public static int MAJOR_HEART_BEAT_INTERVAL = 300000;
	
	
	private String ipAddress;
	private int freeSpace; // in bytes
	
	private ArrayList<Chunk> allChunks;
	private ArrayList<Chunk> newlyAddedChunks;
	private ArrayList<String> corruptedChunkNames;
	// TODO will need to create a map from name to chunk
	// TODO will need to add files to corrupted list
	
	
	private Socket socketWithControlNode;
	private DataInputStream dataInputStreamWithControlNode;
	private DataOutputStream dataOutputStreamWithControlNode;
	private ObjectInputStream objectInputStreamWithControlNode;
	private ObjectOutputStream objectOutputStreamWithControlNode;
	
	
	public ChunkServer(String ipAddress, int freeSpace) 
	{
		this.ipAddress = ipAddress;
		this.freeSpace = freeSpace;
		
		allChunks = new ArrayList<Chunk>();
		newlyAddedChunks = new ArrayList<Chunk>();
		corruptedChunkNames = new ArrayList<String>();
		
		try {
			socketWithControlNode = new Socket(ControlNode.IP_ADDRESS, ControlNode.PORT);
			
			dataInputStreamWithControlNode = new DataInputStream(socketWithControlNode.getInputStream());
			dataOutputStreamWithControlNode = new DataOutputStream(socketWithControlNode.getOutputStream());
			
			objectOutputStreamWithControlNode = new ObjectOutputStream(dataOutputStreamWithControlNode);
			objectInputStreamWithControlNode = new ObjectInputStream(dataInputStreamWithControlNode);
			
			
		} catch (IOException e) {
			System.out.println("Can not create socket with control node");
			e.printStackTrace();
		}
		
		startHeartBeatTimer();
	}
	
	
	private void startHeartBeatTimer()
	{
		Timer timer = new Timer("HearBeatTimer");
		TimerTask sendHeartBeatTask = new SendHeartBeatTask(this);
		timer.schedule(sendHeartBeatTask, 0, MINOR_HEART_BEAT_INTERVAL);
	}
	
	public String getIpAddress() {
		return ipAddress;
	}


	private void sendMessageToControlNode(MessageType msg)
	{
		try {
			objectOutputStreamWithControlNode.writeObject(msg);
		} catch (IOException e) {
			System.out.println("Can not send message to control node.");
			e.printStackTrace();
		}
	}
	
	protected void sendHeartBeat(HeartBeat heartBeat)
	{
		sendMessageToControlNode(heartBeat);
	}
	
	protected void clearNewlyAddedChunks()
	{
		newlyAddedChunks.clear();
	}
	
	private ArrayList<ChunkMetadata> extractMetadataFromChunks(ArrayList<Chunk> chunks)
	{
		ArrayList<ChunkMetadata> metadatas = new ArrayList<ChunkMetadata>();
		for(Chunk chunk : chunks)
			metadatas.add(chunk.getChunkMetadata());
		return metadatas;
	}
	
	protected MajorHeartBeat getUpdatedMajorHeartBeat()
	{
		MajorHeartBeat majorHeartBeat = new MajorHeartBeat(ipAddress);
		majorHeartBeat.updateHeartBeatData(extractMetadataFromChunks(allChunks));
		return majorHeartBeat;
	}
	
	protected MinorHeartBeat getUpdatedMinorHeartBeat()
	{
		MinorHeartBeat minorHeartBeat = new MinorHeartBeat(ipAddress);
		minorHeartBeat.updateHeartBeatData(extractMetadataFromChunks(newlyAddedChunks), corruptedChunkNames);
		return minorHeartBeat;
	}
	
	
}
