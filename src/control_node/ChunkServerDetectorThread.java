package control_node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import chunk_server.ChunkServer;
import messages.FreeChunkServerList;
import messages.SendChunkCopyToAnotherChunksServer;

public class ChunkServerDetectorThread extends Thread {

	private ControlNode controlNodeInstance;
	private HeartBeatTracker heartBeatTracker;
	
	public ChunkServerDetectorThread(ControlNode controlNodeInstance) 
	{
		this.controlNodeInstance = controlNodeInstance;
		this.heartBeatTracker = controlNodeInstance.getHeartBeatTracker();
	}
	
	public void run()
	{
		while(true)
		{
			System.out.println("Checking if some ChunkServers are down...");
			ArrayList<String> deadCSs = heartBeatTracker.getDeadChunkServersAddresses();
			System.out.println("Found some chunk servers down : " + deadCSs);
			removeDeadChunkServerFromChunkInfos(deadCSs);
			makeAnotherCopiesOfDataNodes();
			try {
				sleep(ChunkServer.MINOR_HEART_BEAT_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void removeDeadChunkServerFromChunkInfos(ArrayList<String> deadCSs)
	{
		HashMap<String, ArrayList<String>> chunkStorageInfos = ControlNode.chunkStorageInfo;
		
		for(Map.Entry mapElement : chunkStorageInfos.entrySet())
		{
			String chunkName = (String) mapElement.getKey();
			ArrayList<String> chunkServers = (ArrayList<String>) mapElement.getValue();
			
			for(String deadCS : deadCSs)
			{
				for(int i=0;i<chunkServers.size();i++)
				{
					if(deadCS.equalsIgnoreCase(chunkServers.get(i)))
					{
						ControlNode.chunkStorageInfo.get(chunkName).remove(i);
					}
				}
			}
		}
		
		for(ChunkServerInfo csi : ControlNode.chunkServerInfos)
		{
			if(deadCSs.contains(csi.getIp_adress()))
				ControlNode.chunkServerInfos.remove(csi);
		}
	}
	
	private void makeAnotherCopiesOfDataNodes()
	{
		ArrayList<SendChunkCopyToAnotherChunksServer> messagesToSend = new ArrayList<SendChunkCopyToAnotherChunksServer>();
		
		for(Map.Entry mapElement : ControlNode.chunkStorageInfo.entrySet())
		{
			String chunkName = (String) mapElement.getKey();
			ArrayList<String> chunkServers = (ArrayList<String>) mapElement.getValue();
			
			ArrayList<String> freeChunkServerList = (new FreeChunkServerList("dummy", ControlNode.chunkServerInfos))
					.getFreeChunkServerList();
			
			if(chunkServers.size() >= ControlNode.REPLICATION_LEVEL || chunkServers.size() == 0)
				continue;
			
			int needMoreCS = ControlNode.REPLICATION_LEVEL - chunkServers.size();
			for(int i=0;i<needMoreCS && freeChunkServerList.isEmpty() == false;i++)
			{
				String freeCS = freeChunkServerList.get(0);
				freeChunkServerList.remove(0);
				
				SendChunkCopyToAnotherChunksServer sendMessage = new SendChunkCopyToAnotherChunksServer(chunkName, 
						chunkServers.get(0), freeCS, ControlNode.IP_ADDRESS);
				messagesToSend.add(sendMessage);
			}
		}
		sendMessagesToChunkServers(messagesToSend);
	}
	
	private void sendMessagesToChunkServers(ArrayList<SendChunkCopyToAnotherChunksServer> messagesToSend)
	{
		controlNodeInstance.sendMessagesToChunkServers(messagesToSend);
	}
}
