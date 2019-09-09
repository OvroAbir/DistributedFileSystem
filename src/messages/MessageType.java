package messages;

import java.io.Serializable;

public class MessageType implements Serializable
{
	private int messageType;
	private String messageFrom;
	
	public static int MINOR_HEARTBEAT_CS_CN = 1;
	public static int MAJOR_HEARTBEAT_CS_CN = 2;
	public static int HEARTBEAT_CS_CN = 3;
	public static int UPLOAD_FILE_REQ_CL_CN = 4;
	public static int FREE_CHUNK_SERVER_LIST_CN_CL = 5;
	public static int ERROR_MESSAGE = 6;
	
	
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
