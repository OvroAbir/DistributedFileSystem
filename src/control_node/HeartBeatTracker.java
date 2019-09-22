package control_node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chunk_server.ChunkServer;

public class HeartBeatTracker
{
	private List<String> ipAdresses;
	private List<Long> lastResponse;
	
	public static int CHUNK_SERVER_NOT_DEAD_UNTILL_SEC = (ChunkServer.MINOR_HEART_BEAT_INTERVAL /* * 2*/) / 1000 + 5;
	
	public HeartBeatTracker() 
	{
		ipAdresses = Collections.synchronizedList(new ArrayList<String>()); 
		lastResponse = Collections.synchronizedList(new ArrayList<Long>());
	}
	
	private double elapsedTimeSeconds(long prevTime)
	{
		long currentTime = System.currentTimeMillis();
		return ((currentTime - prevTime) / 1000.0);
	}
	
	protected void addNewChunkServerToTrack(String ip)
	{
		ipAdresses.add(ip);
		long currentTime = System.currentTimeMillis();
		ipAdresses.add(ip);
		lastResponse.add(currentTime);
	}

	private boolean isDead(int index)
	{
		double elapsedTime = elapsedTimeSeconds(lastResponse.get(index));
		System.out.println(ipAdresses.get(index) + " :: " + elapsedTime +"s");
		if(elapsedTime > CHUNK_SERVER_NOT_DEAD_UNTILL_SEC)
			return true;
		return false;
	}
	
	protected ArrayList<String> getDeadChunkServersAddresses()
	{
		ArrayList<String> adrs = new ArrayList<String>();
		
		for(int i=0;i<ipAdresses.size();i++)
		{
			if(isDead(i))
				adrs.add(ipAdresses.get(i));
		}
		
		return adrs;
	}
	
	private int getIndexOfCS(String ip)
	{
		for(int i=0;i<ipAdresses.size();i++)
		{
			if(ipAdresses.get(i).equals(ip))
				return i;
		}
		ipAdresses.add(ip);
		lastResponse.add(System.currentTimeMillis());
		
		return ipAdresses.size()-1;
	}
	
	protected void noteDownReportingTime(String msgFrom)
	{
		long curTime = System.currentTimeMillis();
		int index = getIndexOfCS(msgFrom);
		lastResponse.set(index, curTime);
	}
	
	
}
