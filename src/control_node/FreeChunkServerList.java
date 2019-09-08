package control_node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import messages.MessageType;

public class FreeChunkServerList extends MessageType
{
	private int numberOfChunks;
	private ArrayList<String>[] chunkServerList;

	public FreeChunkServerList(int numberOfChunks)
	{
		super(MessageType.FREE_CHUNK_SERVER_LIST_CN_CL);
		this.numberOfChunks = numberOfChunks;
		chunkServerList = new ArrayList[numberOfChunks];
		for(int i=0;i<numberOfChunks;i++)
			chunkServerList[i] = new ArrayList<String>();
	}
	
	public int getNumberOfChunks() {
		return numberOfChunks;
	}

	public void setNumberOfChunks(int numberOfChunks) {
		this.numberOfChunks = numberOfChunks;
	}

	public ArrayList<String>[] getChunkServerList() {
		return chunkServerList;
	}

	public void setChunkServerList(ArrayList<String>[] chunkServerList) {
		this.chunkServerList = chunkServerList;
	}
	
	public void setFreeServers(ArrayList<ChunkServerInfo> serverInfos)
	{
		PriorityQueue<ChunkServerInfo> pq = new PriorityQueue<ChunkServerInfo>(
				new Comparator<ChunkServerInfo>() 
				{
					public int compare(ChunkServerInfo cs1, ChunkServerInfo cs2)
					{
						if(cs1.getFreeSpace() <= cs2.getFreeSpace())
							return 1;
						return -1;
					}
				});
		
		for(int chunk=0;chunk<numberOfChunks;chunk++)
		{
			pq.clear();
			for(ChunkServerInfo csi : serverInfos)
				pq.add(csi);
			
			int count = 0;
			while(count < ControlNode.REPLICATION_LEVEL && pq.size() > 0)
			{
				ChunkServerInfo csi = pq.poll();
				if(csi.hasSpace(ControlNode.CHUNK_SIZE_BYTES))
				{
					chunkServerList[chunk].add(csi.getIp_adress());
					csi.decreaseFreeSpace(ControlNode.CHUNK_SIZE_BYTES);
					count++;
				}
				else
				{
					System.out.println("No space available in " + csi.getIp_adress());
				}
			}
			
			if(count != ControlNode.REPLICATION_LEVEL)
				System.out.println("Only " + count + " replicas were possible for chunk " + chunk + ".");
		}
	}

}
