package messages;

public class FileUploadRequest_CL_CN extends MessageType
{
	private int fileSize;
	
	public FileUploadRequest_CL_CN(int fileSize, String messageFrom) 
	{
		super(UPLOAD_FILE_REQ_CL_CN, messageFrom);
		this.fileSize = fileSize;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

}
