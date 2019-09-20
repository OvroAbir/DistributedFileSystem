package messages;

public class RequestChunkData_CL_CS extends MessageType {

	String fileName;
	int chunkIndex;
	public RequestChunkData_CL_CS(String fileName, int chunkIndex, String messageFrom) {
		super(REQUEST_CHUNK_DATA_TO_CHUNK_SERVER, messageFrom);
		this.fileName = fileName;
		this.chunkIndex = chunkIndex;
	}
	public String getFileName() {
		return fileName;
	}
	public int getChunkIndex() {
		return chunkIndex;
	}
	

}
