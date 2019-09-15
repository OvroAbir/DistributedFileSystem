package messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import control_node.ChunkServerInfo;
import control_node.ControlNode;
import messages.MessageType;

public class FreeChunkServerList extends MessageType
{
	private ArrayList<String> chunkServerList;

	public FreeChunkServerList(String messageFrom, ArrayList<ChunkServerInfo> serverInfos)
	{
		super(MessageType.FREE_CHUNK_SERVER_LIST_CN_CL, messageFrom);
		chunkServerList = new ArrayList<String>();
		setFreeServers(serverInfos);
	}

	public ArrayList<String> getFreeChunkServerList() {
		return chunkServerList;
	}

	private void setFreeServers(ArrayList<ChunkServerInfo> serverInfos)
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
		
		
		for(ChunkServerInfo csi : serverInfos)
			pq.add(csi);
		
		int count = 0;
		while(count < ControlNode.REPLICATION_LEVEL && pq.size() > 0)
		{
			ChunkServerInfo csi = pq.poll();
			if(csi.hasSpace(ControlNode.CHUNK_SIZE_BYTES))
			{
				chunkServerList.add(csi.getIp_adress());
				csi.decreaseFreeSpace(ControlNode.CHUNK_SIZE_BYTES);
				count++;
			}
			else
			{
				System.out.println("No space available in " + csi.getIp_adress());
				break;
			}
		}
		
		if(count != ControlNode.REPLICATION_LEVEL)
			System.out.println("Only " + count + " replicas are possible to write.");
	
	}

}
