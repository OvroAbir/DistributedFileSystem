package chunk_server;

import java.util.concurrent.ConcurrentHashMap;

public class FileStoreAndRetrieveHandler 
{
	private ConcurrentHashMap<String, Chunk> hashMap;
	private ChunkServer chunkServerInstance;
	
	public FileStoreAndRetrieveHandler(ChunkServer chunkServer) 
	{
		this.chunkServerInstance = chunkServer;
		hashMap = chunkServer.getConcurrentHashMapForFiles();
	}
	
	public boolean storeFileChunk(Chunk chunk)
	{
		// TODO complete this
		return false;
	}
}
