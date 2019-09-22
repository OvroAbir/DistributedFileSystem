package messages;

import java.util.ArrayList;

public class ChunkAllLocation extends MessageType {

	ArrayList<String> chunkLocations;
	public ChunkAllLocation(ArrayList<String> chunkLocations, String messageFrom) {
		super(VALID_CHUNK_LOCATIONS, messageFrom);
		this.chunkLocations = chunkLocations;
	}

	public ArrayList<String> getOtherValidChunkLocations(String selfIpAddress)
	{
		ArrayList<String> copiedList = new ArrayList<String>(chunkLocations);
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
