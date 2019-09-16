package chunk_server;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class FileStoreAndRetrieveHandler 
{
	private ConcurrentHashMap<String, Chunk> hashMap;
	private ArrayList<Chunk> newlyAddedChunks, allChunks;
	private ChunkServer chunkServerInstance;
	
	public FileStoreAndRetrieveHandler(ChunkServer chunkServer) 
	{
		this.chunkServerInstance = chunkServer;
		hashMap = chunkServer.getConcurrentHashMapForFiles();
		this.allChunks = chunkServer.getAllChunks();
		this.newlyAddedChunks = chunkServer.getNewlyAddedChunks();
	}
	
	public boolean storeFileChunk(Chunk chunk)
	{
		// TODO check for free space
		// TODO check for duplicate / version information
		chunk.storeRealDataInDisk(ChunkServer.FILE_STORAGE_FOLDER_LOCATION);
		
		hashMap.put(chunk.getChunkMetadata().getChunkFileName(), chunk);
		allChunks.add(chunk);
		newlyAddedChunks.add(chunk);
		System.out.println("Stored Chunk " + chunk.getChunkMetadata().getChunkFileName());
		return true;
	}
	
	public Chunk retrieveFileChunk(String chunkName)
	{
		Chunk chunk = null;
		if(hashMap.contains(chunkName))
			chunk = hashMap.get(chunkName);
		return chunk;
	}
}
