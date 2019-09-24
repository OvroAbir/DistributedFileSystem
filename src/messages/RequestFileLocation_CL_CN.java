package messages;

import chunk_server.ChunkServer;

public class RequestFileLocation_CL_CN extends MessageType 
{
	private String fileName;
	private int chunkIndex;
	public RequestFileLocation_CL_CN(String fileName, int chunkIndex, String messageFrom) {
		super(REQUEST_CHUNK_SERVER_LIST_FOR_STORED_FILE, messageFrom);
		this.fileName = fileName;
		this.chunkIndex = chunkIndex;
	}
	public String getFileName() {
		return fileName + ChunkServer.chunkNameSeperator + chunkIndex;
	}
	
	public int getchunkIndex() {
		return chunkIndex;
	}
	
}
