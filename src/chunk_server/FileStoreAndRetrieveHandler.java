package chunk_server;

import java.io.File;
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
		chunk.storeRealDataInDisk(chunkServerInstance.getChunkServerSpecificFileStorageLocation());
		
		hashMap.put(chunk.getChunkMetadata().getChunkFileName(), chunk);
		allChunks.add(chunk);
		newlyAddedChunks.add(chunk);
		System.out.println("Stored Chunk " + chunk.getChunkMetadata().getChunkFileName());
		chunkServerInstance.reduceFreeSpace(chunk.getTotalLength());
		return true;
	}
	
	public Chunk retrieveFileChunk(String chunkName)
	{
		Chunk chunk = null;
		if(hashMap.containsKey(chunkName))
			chunk = hashMap.get(chunkName);
		return chunk;
	}
	
	public void replaceChunk(String chunkName, Chunk newChunk)
	{
		Chunk oldChunk = hashMap.get(chunkName);
		String fullFilePath = chunkServerInstance.getChunkServerSpecificFileStorageLocation() 
				+ File.separator + chunkName;
		oldChunk.changeStoredFile(fullFilePath, newChunk.getData());
		System.out.println("Replaced data of chunk " + chunkName);
	}
}
