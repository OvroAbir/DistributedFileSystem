package ReedSolomonEntity;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import chunk_server.Chunk;
import control_node.ControlNode;
import erasure.ReedSolomon;

public class ReedSolomonHelper {

	public ReedSolomonHelper() {
	}

	private static ArrayList<Shard> getShardObjectsFromByteArray(byte[][] shardsAra)
	{
		ArrayList<Shard> shardObjects = new ArrayList<Shard>();
		
		for(int i=0;i<shardsAra.length;i++)
		{
			Shard shard = new Shard(shardsAra[i], i);
			System.out.println("shard len " + shardsAra[i].length);
			shardObjects.add(shard);
		}
		return shardObjects;
	}
	
	public static ArrayList<Shard> encode(String fragment)
	{
		System.out.println("Starting new encode");
		int fileSize = fragment.length();
		int storedSize = fileSize + ControlNode.BYTES_IN_INT;
		int shardSize = (storedSize + ControlNode.DATA_SHARDS - 1) / ControlNode.DATA_SHARDS;
		int bufferSize = shardSize * ControlNode.DATA_SHARDS;
		
		byte [] allBytes = new byte[bufferSize];
		byte[] fileSizeByteAra = ByteBuffer.allocate(ControlNode.BYTES_IN_INT).putInt(fileSize).array();
		byte[] payload = fragment.getBytes();
		System.arraycopy(fileSizeByteAra, 0, allBytes, 0, fileSizeByteAra.length);
		System.arraycopy(payload, 0, allBytes, ControlNode.BYTES_IN_INT, payload.length);
		for(int i=ControlNode.BYTES_IN_INT + payload.length;i<allBytes.length;i++)
			allBytes[i] = 0;
		
		byte[][] shards = new byte[ControlNode.TOTAL_SHARDS][shardSize];
		for (int i = 0; i < ControlNode.DATA_SHARDS; i++) {
			System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
		}
		
		ReedSolomon reedSolomon = new ReedSolomon(ControlNode.DATA_SHARDS, ControlNode.PARITY_SHARDS);
		reedSolomon.encodeParity(shards, 0, shardSize);
		
		return getShardObjectsFromByteArray(shards);
	}
	
	private static Shard findShardFromArrayListByIndex(ArrayList<Shard> shards, int index)
	{
		for(int i=0;i<shards.size();i++)
		{
			if(shards.get(i).getShardIndex() == index)
				return shards.get(i);
		}
		return null;
	}
	
	private static String getContentFromShards(byte[][] shards)
	{
		int shardSize = shards.length*shards[0].length;
		byte[] allBytes = new byte[shardSize];
		int count = 0;
		
		for(int i=0;i<shards.length;i++)
		{
			for(int j=0;j<shards[i].length;j++)
				allBytes[count++] = shards[i][j];
		}
		
		byte[] fileSizeByteAra = new byte[ControlNode.BYTES_IN_INT];
		System.arraycopy(allBytes, 0, fileSizeByteAra, 0, ControlNode.BYTES_IN_INT);
		int fileSize = new BigInteger(fileSizeByteAra).intValue();

		
		byte[] payload = new byte[fileSize];
		System.arraycopy(allBytes, ControlNode.BYTES_IN_INT, payload, 0, fileSize);
		return new String(payload);
	}
	
	public static String decode(ArrayList<Shard> shardObjects)
	{
		System.out.println("Decoding a new shard");
		if(shardObjects == null || shardObjects.size() < ControlNode.DATA_SHARDS)
		{
			System.out.println("Number of shards is lower than " + ControlNode.DATA_SHARDS);
			return "Not enough shards";
		}
		int shardLen = -1;
		for(int i=0;i<shardObjects.size();i++)
		{
			System.out.println("decoding shard length " + shardObjects.get(i).getShardLength());
			if(shardLen == -1)
				shardLen = shardObjects.get(i).getShardLength();
			else if(shardLen != shardObjects.get(i).getShardLength())
			{
				System.out.println("Shards length mismatch.");
				return "Shard length mismatch";
			}
		}
		
		byte[][] shards = new byte [ControlNode.TOTAL_SHARDS][];
		boolean[] shardPresent = new boolean [ControlNode.TOTAL_SHARDS];
		int shardSize = shardLen;
		
		for (int i = 0; i < ControlNode.TOTAL_SHARDS; i++) 
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
		
		ReedSolomon reedSolomon = new ReedSolomon(ControlNode.DATA_SHARDS, ControlNode.PARITY_SHARDS);
		reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);
		
		return getContentFromShards(shards);
	}
	
	public static ArrayList<Chunk> getEncodedChunks(String mainFileName, String msg, int fileFragmentIndex)
	{
		ArrayList<Shard> shards = encode(msg);
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		for(int i=0;i<shards.size();i++)
		{
			Chunk chunk = new Chunk(mainFileName, shards.get(i), fileFragmentIndex, shards.get(i).getShardIndex());
			chunks.add(chunk);
		}
		return chunks;
	}
}
