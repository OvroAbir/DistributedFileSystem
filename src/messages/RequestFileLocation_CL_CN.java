package messages;

public class RequestFileLocation_CL_CN extends MessageType 
{
	private String fileName;
	public RequestFileLocation_CL_CN(String fileName, String messageFrom) {
		super(REQUEST_CHUNK_SERVER_LIST_FOR_STORED_FILE, messageFrom);
		this.fileName = fileName;
	}
	public String getFileName() {
		return fileName;
	}
	
}
