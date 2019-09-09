package messages;

public class HeartBeat extends MessageType
{
	private int heartBeatType;
	private int totalNumberOfChunks;
	private int freeSpace;
	
	public HeartBeat(int heartBeatType, String messageFrom) 
	{
		super(heartBeatType, messageFrom);
		this.heartBeatType = heartBeatType;
	}

	public int getHeartBeatType() {
		return heartBeatType;
	}

	public void setHeartBeatType(int heartBeatType) {
		this.heartBeatType = heartBeatType;
	}

	public int getTotalNumberOfChunks() {
		return totalNumberOfChunks;
	}

	public void setTotalNumberOfChunks(int totalNumberOfChunks) {
		this.totalNumberOfChunks = totalNumberOfChunks;
	}

	public int getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(int freeSpace) {
		this.freeSpace = freeSpace;
	}
	
	
}
