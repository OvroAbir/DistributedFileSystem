package messages;

import chunk_server.Chunk;

public class FileDownload_CS_CL extends MessageType {

	Chunk fileChunk;
	public FileDownload_CS_CL(Chunk chunk, String messageFrom) {
		super(DOWNLOAD_CHUNK_CS_CL, messageFrom);
		fileChunk = chunk;
	}
	
	public Chunk getFileChunk()
	{
		return fileChunk;
	}
}
