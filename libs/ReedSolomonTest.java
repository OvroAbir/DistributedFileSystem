import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import erasure.ReedSolomon;

class Shard
{
	private byte[] realShards;
	private int shardIndex;
	
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
}

public class ReedSolomonTest 
{	
	public static final int DATA_SHARDS = 4;
	public static final int PARITY_SHARDS = 2;
	public static final int TOTAL_SHARDS = 6;
	public static final int BYTES_IN_INT = 4;
	
	private ArrayList<Shard> getShardObjectsFromByteArray(byte[][] shardsAra)
	{
		ArrayList<Shard> shardObjects = new ArrayList<Shard>();
		
		for(int i=0;i<shardsAra.length;i++)
		{
			Shard shard = new Shard(shardsAra[i], i);
			shardObjects.add(shard);
		}
		return shardObjects;
	}
	
	public ArrayList<Shard> encode(String fragment)
	{
		int fileSize = fragment.length();
		int storedSize = fileSize + BYTES_IN_INT;
		int shardSize = (storedSize + DATA_SHARDS - 1) / DATA_SHARDS;
		int bufferSize = shardSize * DATA_SHARDS;
		
		byte [] allBytes = new byte[bufferSize];
		byte[] fileSizeByteAra = ByteBuffer.allocate(BYTES_IN_INT).putInt(fileSize).array();
		byte[] payload = fragment.getBytes();
		System.arraycopy(fileSizeByteAra, 0, allBytes, 0, fileSizeByteAra.length);
		System.arraycopy(payload, 0, allBytes, BYTES_IN_INT, payload.length);
		for(int i=BYTES_IN_INT + payload.length;i<allBytes.length;i++)
			allBytes[i] = 0;
		
		byte[][] shards = new byte[TOTAL_SHARDS][shardSize];
		for (int i = 0; i < DATA_SHARDS; i++) {
			System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
		}
		
		ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
		reedSolomon.encodeParity(shards, 0, shardSize);
		
		return getShardObjectsFromByteArray(shards);
	}
	
	private Shard findShardFromArrayListByIndex(ArrayList<Shard> shards, int index)
	{
		for(int i=0;i<shards.size();i++)
		{
			if(shards.get(i).getShardIndex() == index)
				return shards.get(i);
		}
		return null;
	}
	
	private String getContentFromShards(byte[][] shards)
	{
		int shardSize = shards.length*shards[0].length;
		byte[] allBytes = new byte[shardSize];
		int count = 0;
		
		for(int i=0;i<shards.length;i++)
		{
			for(int j=0;j<shards[i].length;j++)
				allBytes[count++] = shards[i][j];
		}
		
		byte[] fileSizeByteAra = new byte[BYTES_IN_INT];
		System.arraycopy(allBytes, 0, fileSizeByteAra, 0, BYTES_IN_INT);
		int fileSize = new BigInteger(fileSizeByteAra).intValue();

		
		byte[] payload = new byte[fileSize];
		System.arraycopy(allBytes, BYTES_IN_INT, payload, 0, fileSize);
		return new String(payload);
	}
	
	public String decode(ArrayList<Shard> shardObjects)
	{
		if(shardObjects == null || shardObjects.size() < DATA_SHARDS)
		{
			System.out.println("Number of shards is lower than " + DATA_SHARDS);
			return "Not enough shards";
		}
		int shardLen = -1;
		for(int i=0;i<shardObjects.size();i++)
		{
			if(shardLen == -1)
				shardLen = shardObjects.get(i).getShardLength();
			else if(shardLen != shardObjects.get(i).getShardLength())
			{
				System.out.println("Shards length mismatch.");
				return "Shard length mismatch";
			}
		}
		
		byte[][] shards = new byte [TOTAL_SHARDS][];
		boolean[] shardPresent = new boolean [TOTAL_SHARDS];
		int shardSize = shardLen;
		
		for (int i = 0; i < TOTAL_SHARDS; i++) 
		{
			Shard shard = findShardFromArrayListByIndex(shardObjects, i);
			if(shard == null)
			{
				shards[i] = new byte[shardSize];
				shardPresent[i] = false;
			}
			else
			{
				shards[i] = shard.getRealShards();
				shardPresent[i] = true;
			}
		}
		
		ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
		reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);
		
		return getContentFromShards(shards);
	}
	
	public static void main(String[] args)
	{
		ReedSolomonTest reedSolomonTest = new ReedSolomonTest();
		

		String content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut"
				+ " labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud"
				+ " exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in repre"
				+ "henderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidat"
				+ "at non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		
		ArrayList<Shard> shards = reedSolomonTest.encode(content);
		String contentBack = reedSolomonTest.decode(shards);
		
	}
}
