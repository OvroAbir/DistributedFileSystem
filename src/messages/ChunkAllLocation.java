package messages;

import java.util.ArrayList;

public class ChunkAllLocation extends MessageType {

	ArrayList<String> chunkLocationsAndShardName;
	public ChunkAllLocation(ArrayList<String> chunkLocations, String messageFrom) {
		super(VALID_CHUNK_LOCATIONS, messageFrom);
		this.chunkLocationsAndShardName = chunkLocations;
	}

	public ArrayList<String> getOtherValidChunkLocationsAndFileName(String selfIpAddress)
	{
		ArrayList<String> copiedList = new ArrayList<String>(chunkLocationsAndShardName);
		for(int i=0;i<copiedList.size();i++)
		{
			if(copiedList.get(i).equals(selfIpAddress)) {
				copiedList.remove(i);
				break;
			}
		}
		return copiedList;
	}
}
