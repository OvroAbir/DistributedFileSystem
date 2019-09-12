package chunk_server;

public class ChunkMetadata 
{
	private boolean isCorrupted;
	private String chunkFileName;
	private int realDatalength;
	private byte[] hashValue;
	
	public ChunkMetadata(String chunkFileName) 
	{
		this.chunkFileName = chunkFileName;
	}
	
	public computeAndStoreHash()
}
