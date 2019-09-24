package messages;

public class RequestChunkData_CL_CS extends MessageType {

	String fileName;
	int shardIndex;
	public RequestChunkData_CL_CS(String fileName, int shardIndex, String messageFrom) {
		super(REQUEST_CHUNK_DATA_TO_CHUNK_SERVER, messageFrom);
		this.fileName = fileName;
		this.shardIndex = shardIndex;
	}
	public String getFileName() {
		return fileName;
	}
	public int getShardIndex() {
		return shardIndex;
	}
	

}
