package messages;

import java.util.ArrayList;

import chunk_server.ChunkMetadata;

public class MinorHeartBeat extends HeartBeat
{
	private ArrayList<ChunkMetadata> newlyAddedChunkMetadatas;
	private ArrayList<String> corruptedChunkNames;
	
	public MinorHeartBeat(String messageFrom) 
	{
		super(MINOR_HEARTBEAT_CS_CN, messageFrom);
		newlyAddedChunkMetadatas = new ArrayList<ChunkMetadata>();
		corruptedChunkNames = new ArrayList<String>();
	}

	public ArrayList<ChunkMetadata> getNewlyAddedChunkNames() {
		return newlyAddedChunkMetadatas;
	}

	public void setNewlyAddedChunkNames(ArrayList<ChunkMetadata> newlyAddedChunkNames) {
		this.newlyAddedChunkMetadatas = newlyAddedChunkNames;
	}

	public ArrayList<String> getCorruptedChunkNames() {
		return corruptedChunkNames;
	}

	public void setCorruptedChunkNames(ArrayList<String> corruptedChunkNames) {
		this.corruptedChunkNames = corruptedChunkNames;
	}
	
	public void updateHeartBeatData(ArrayList<ChunkMetadata> newChunkMetadatas, ArrayList<String> corruptedChunks)
	{
		newlyAddedChunkMetadatas.clear();
		newlyAddedChunkMetadatas.addAll(newChunkMetadatas);
		
		corruptedChunkNames.clear();
		corruptedChunkNames.addAll(corruptedChunks);
		
		// TODO add if any new field missing
		
	}
}
