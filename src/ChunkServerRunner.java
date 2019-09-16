import chunk_server.ChunkServer;

public class ChunkServerRunner 
{
	public static void main(String[] arg)
	{
		System.out.println("Starting Chunk Server");
		ChunkServer chunkServer = new ChunkServer("127.0.0.1", 500000);
	}
}
