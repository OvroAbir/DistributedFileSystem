package ReedSolomonEntity;

import java.io.Serializable;

public class Shard implements Serializable
{
	private byte[] realShards;
	private int shardIndex;
	
	public static char SHARD_SEPERATOR = '_';
	
	public Shard(byte[] realShards, int shardIndex) 
	{
		this.realShards = realShards;
		this.shardIndex = shardIndex;
	}

	public byte[] getRealShards() {
		return realShards;
	}

	public int getShardIndex() {
		return shardIndex;
	}
	
	public int getShardLength()
	{
		return realShards.length; 
	}
	
	public String toString()
	{
		return realShards.toString() + SHARD_SEPERATOR + shardIndex;
	}
	
	public static Shard getShardObjectFromString(String content)
	{
		int seperatorIndex = content.lastIndexOf(SHARD_SEPERATOR);
		String data = content.substring(0, seperatorIndex);
		int index = Integer.parseInt(content.substring(seperatorIndex + 1));
		return new Shard(data.getBytes(), index);
	}
}
