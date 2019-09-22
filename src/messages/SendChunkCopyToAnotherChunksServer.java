package messages;

public class SendChunkCopyToAnotherChunksServer extends MessageType {

	String sendFrom, sendTo, chunkName;
	public SendChunkCopyToAnotherChunksServer(String chunkName, String sendFrom, String sendTo, String messageFrom) {
		super(SEND_CHUNK_COPIES_CN_CS, messageFrom);
		this.chunkName = chunkName;
		this.sendFrom = sendFrom;
		this.sendTo = sendTo;
	}
	public String getSendFrom() {
		return sendFrom;
	}
	public String getSendTo() {
		return sendTo;
	}
	public String getChunkName()
	{
		return chunkName;
	}
	public String toString()
	{
		return String.format("<Send Chunk %s from %s to %s", chunkName, sendFrom, sendTo);
	}
	
}
