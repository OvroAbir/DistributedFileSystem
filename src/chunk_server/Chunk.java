package chunk_server;

public class Chunk 
{
	private ChunkMetadata chunkMetadata;
	
	public Chunk() 
	{
		chunkMetadata = new ChunkMetadata();
	}

	public ChunkMetadata getChunkMetadata() {
		return chunkMetadata;
	}

	public void setChunkMetadata(ChunkMetadata chunkMetadata) {
		this.chunkMetadata = chunkMetadata;
	}
	
	
}
