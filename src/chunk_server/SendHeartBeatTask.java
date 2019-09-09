package chunk_server;

import java.util.TimerTask;

import messages.MajorHeartBeat;
import messages.MinorHeartBeat;

public class SendHeartBeatTask extends TimerTask
{
	private int time;
	private ChunkServer chunkServer;
	
	public SendHeartBeatTask(ChunkServer chunkServer) 
	{
		time = 0;
		this.chunkServer = chunkServer;
	}
	
	
	@Override
	public void run() 
	{
		if(time % ChunkServer.MAJOR_HEART_BEAT_INTERVAL == 0)
		{
			System.out.println("Sending Major Heartbeat to Control Node.");
			MajorHeartBeat majorHeartBeat = chunkServer.getUpdatedMajorHeartBeat();
			chunkServer.sendHeartBeat(majorHeartBeat);
			time = ChunkServer.MINOR_HEART_BEAT_INTERVAL;
		}
		else
		{
			System.out.println("Sending Minor Heartbeat to Control Node.");
			MinorHeartBeat minorHeartBeat = chunkServer.getUpdatedMinorHeartBeat();
			chunkServer.sendHeartBeat(minorHeartBeat);
			chunkServer.clearNewlyAddedChunks();
			time += ChunkServer.MINOR_HEART_BEAT_INTERVAL;
		}
	}

}
