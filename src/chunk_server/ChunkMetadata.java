package chunk_server;

import java.io.Serializable;
import java.util.ArrayList;

public class ChunkMetadata implements Serializable
{
	private String mainFileName;
	private int realDatalength, chunkIndex, version;
	private ArrayList<String> sha1Values;
	
	// TODO do version work
		
	public ChunkMetadata(String mainFileName, int index, ArrayList<String> sha1Values, int realDataLength) 
	{
		this.mainFileName = mainFileName;
		this.chunkIndex = index;
		this.sha1Values = sha1Values;
		this.realDatalength = realDataLength;
	}

	public ArrayList<String> getSha1Values() {
		return sha1Values;
	}

	public void setSha1Values(ArrayList<String> sha1Values) {
		this.sha1Values = sha1Values;
	}

	public String getMainFileName() {
		return mainFileName;
	}

	public int getRealDatalength() {
		return realDatalength;
	}

	public int getChunkIndex() {
		return chunkIndex;
	}
	
	public String getChunkFileName()
	{
		return getMainFileName() + ChunkServer.chunkNameSeperator + getChunkIndex();
	}
}
