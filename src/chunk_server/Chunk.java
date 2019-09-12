package chunk_server;

public class Chunk 
{
	private ChunkMetadata chunkMetadata;
	private byte[] realData;
	
	public Chunk(String chunkName) 
	{
		chunkMetadata = new ChunkMetadata(chunkName);
	}

	public ChunkMetadata getChunkMetadata() {
		return chunkMetadata;
	}

	public void setChunkMetadata(ChunkMetadata chunkMetadata) {
		this.chunkMetadata = chunkMetadata;
	}
	
	
}
