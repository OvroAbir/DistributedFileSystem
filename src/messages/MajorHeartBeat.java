package messages;

import java.util.ArrayList;

import chunk_server.ChunkMetadata;
import chunk_server.ChunkServer;

public class MajorHeartBeat extends HeartBeat
{
	private ArrayList<ChunkMetadata> allChunkMetaDatas;
	
	public MajorHeartBeat(String messageFrom) 
	{
		super(MAJOR_HEARTBEAT_CS_CN, messageFrom);
		allChunkMetaDatas = new ArrayList<ChunkMetadata>();
	}

	public ArrayList<ChunkMetadata> getAllChunkMetaDatas() {
		return allChunkMetaDatas;
	}

	public void setAllChunkMetaDatas(ArrayList<ChunkMetadata> allChunkMetaDatas) {
		this.allChunkMetaDatas = allChunkMetaDatas;
	}

	
	public void updateHeartBeatData(ArrayList<ChunkMetadata> chunkMetadatas)
	{
		allChunkMetaDatas.clear();
		allChunkMetaDatas.addAll(chunkMetadatas);
		
		// TODO add if new components added
	}
}
