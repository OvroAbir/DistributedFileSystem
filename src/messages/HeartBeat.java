package messages;

public class HeartBeat extends MessageType
{
	private int heartBeatType;
	
	public HeartBeat(int heartBeatType) 
	{
		super(HEARTBEAT_CS_CN);
		this.heartBeatType = heartBeatType;
	}
}
