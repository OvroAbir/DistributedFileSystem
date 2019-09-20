package messages;

public class RequestFreshChunkCopy extends MessageType {

	String chunkName;
	public RequestFreshChunkCopy(String chunkName, String messageFrom) {
		super(REQUEST_FRESH_CHUNK_COPY_CS_CS, messageFrom);
		this.chunkName = chunkName;
	}
	
	public String getChunkName()
	{
		return chunkName;
	}
}
