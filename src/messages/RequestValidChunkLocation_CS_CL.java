package messages;

public class RequestValidChunkLocation_CS_CL extends MessageType{

	String chunkName;
	public RequestValidChunkLocation_CS_CL(String chunkName, String messageFrom) 
	{
		super(REQUEST_VALID_CHUNK_LOCATION, messageFrom);
		this.chunkName = chunkName;
	}
	
	public String getChunkName()
	{
		return chunkName;
	}

}
