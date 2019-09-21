package messages;

import java.io.Serializable;

public class MessageType implements Serializable
{
	private int messageType;
	private String messageFrom;
	private static int messageTypeCounter = 0;
	
	public static int MINOR_HEARTBEAT_CS_CN = ++messageTypeCounter;
	public static int MAJOR_HEARTBEAT_CS_CN = ++messageTypeCounter;
	public static int HEARTBEAT_CS_CN = ++messageTypeCounter;
	public static int UPLOAD_FILE_REQ_CL_CN = ++messageTypeCounter;
	public static int UPLOAD_FILE_CL_CS = ++messageTypeCounter;
	public static int FREE_CHUNK_SERVER_LIST_CN_CL = ++messageTypeCounter;
	public static int REQUEST_CHUNK_SERVER_LIST_FOR_STORED_FILE = ++messageTypeCounter;
	public static int CHUNK_SERVER_LIST_FOR_STORED_FILE = ++messageTypeCounter;
	public static int REQUEST_CHUNK_DATA_TO_CHUNK_SERVER = ++messageTypeCounter;
	public static int DOWNLOAD_CHUNK_CS_CL = ++messageTypeCounter;
	public static int ERROR_MESSAGE = ++messageTypeCounter;
	public static int SUCCESS_MESSAGE = ++messageTypeCounter;
	public static int REQUEST_FRESH_CHUNK_COPY_CS_CS = ++messageTypeCounter;
	public static int FILE_DATA_CHANGED_SO_WAIT_CS_CL = ++messageTypeCounter;
	public static int REQUEST_VALID_CHUNK_LOCATION = ++messageTypeCounter;
	public static int VALID_CHUNK_LOCATIONS = ++messageTypeCounter;
	
	
	public MessageType(int messageType, String messageFrom) 
	{
		this.messageType = messageType;
		this.messageFrom = messageFrom;
	}
	
	public int getMessageType() 
	{
		return messageType;
	}
	public void setMessageType(int messageType) 
	{
		this.messageType = messageType;
	}
	
	public String toString()
	{
		return "MessageType : " + Integer.toString(messageType);
	}

	public String getMessageFrom() {
		return messageFrom;
	}

	public void setMessageFrom(String messageFrom) {
		this.messageFrom = messageFrom;
	}
	
	
}
