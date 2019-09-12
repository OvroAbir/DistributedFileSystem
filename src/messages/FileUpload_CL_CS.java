package messages;

import java.util.ArrayList;

import chunk_server.Chunk;

public class FileUpload_CL_CS extends MessageType 
{
	private Chunk fileChunk;
	private ArrayList<String> chunkServerAddressToForward;
	
	public FileUpload_CL_CS(String messageFrom, Chunk fileChunk, ArrayList<String> chunkServerAddressToForward) {
		super(UPLOAD_FILE_CL_CS, messageFrom);
		this.fileChunk = fileChunk;
		this.chunkServerAddressToForward = chunkServerAddressToForward;
	}

	public Chunk getFileChunk() {
		return fileChunk;
	}

	public boolean needToSendAnotherChunkServer()
	{
		return chunkServerAddressToForward.size() > 0;
	}
	
	public void removeFirstElementFromChunkServerList()
	{
		if(chunkServerAddressToForward.size() == 0)
			return;
		chunkServerAddressToForward.remove(0);
	}
}
