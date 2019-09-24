package chunk_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import ReedSolomonEntity.Shard;
import exceptions.FileDataChanged;

public class Chunk implements Serializable
{
	private ChunkMetadata chunkMetadata;
	private int totalLength;
	private String realContent;
	private String storedFileName;
	private boolean isDataInMemory;
	
	public static int SHA1_INPUT_LEN = 8 * 1024;
	
//	public Chunk(String mainFileName, String realContent, int index) 
//	{
//		this.realContent = realContent;
//		
//		ArrayList<String> sha1Values = calculateWholeSHA1(realContent);
//		chunkMetadata = new ChunkMetadata(mainFileName, index, sha1Values, realContent.length());
//		this.isDataInMemory = true;
//	}
	
	public Chunk(String mainFileName, Shard shard, int fileFragmentIndex, int shardIndex)
	{
		this.realContent = shard.toString();

		ArrayList<String> sha1Values = calculateWholeSHA1(realContent);
		chunkMetadata = new ChunkMetadata(mainFileName, fileFragmentIndex, shardIndex, sha1Values, realContent.length());
		this.isDataInMemory = true;
	}

	public ChunkMetadata getChunkMetadata() {
		return chunkMetadata;
	}

	public void setChunkMetadata(ChunkMetadata chunkMetadata) {
		this.chunkMetadata = chunkMetadata;
	}
	
	public String getData()
	{
		if(isDataInMemory == false)
		{
			System.out.println("File Data was not in memory");
			return null;
		}
		return realContent;
	}
	
	public void prepareChunkBeforeSendingToClient() throws FileDataChanged
	{
		if(isDataInMemory == false)
		{
			try {
				retrieveRealDataFromDisk();
			} catch (FileDataChanged e) {
				System.out.println("Chunk data has been changed");
				// TODO Request data from another CS
				e.printStackTrace();
				throw new FileDataChanged(e.getChunkName(), e.getSliceNum());
			}
		}
	}
	
	public String storeRealDataInDisk(String folderName)
	{
		storedFileName = folderName + File.separator + chunkMetadata.getChunkFileName();
		
		if(isDataInMemory == false)
			return storedFileName;
		
		File file = new File(storedFileName);
		
		PrintWriter pw = null;
		try {
			if(file.getParentFile().exists() == false)
				file.getParentFile().mkdirs();
			
			if(file.exists() == false)
				file.createNewFile();
			
			pw = new PrintWriter(file);
			pw.write(realContent);
		} catch (FileNotFoundException e) {
			System.out.println("Could not create file");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not create file");
			e.printStackTrace();
		}
		pw.close();
		
		isDataInMemory = false;
		realContent = null;
		
		return storedFileName;
	}
	
	private int isFileDataUnChanged(String content)
	{
		ArrayList<String> newSha1Values = calculateWholeSHA1(content);
		ArrayList<String> oldSha1Values = chunkMetadata.getSha1Values();
		
		//if(content.length() != chunkMetadata.getRealDatalength() || newSha1Values == null || oldSha1Values == null || 
		//		newSha1Values.size() != oldSha1Values.size())
		//	return 0;
		
		int len = Math.min(newSha1Values.size(), oldSha1Values.size());
		int i=0;
		for(i=0;i<len;i++)
		{
			String oldS = oldSha1Values.get(i);
			String newS = newSha1Values.get(i);
			
			if(oldS.equals(newS) == false)
				return i;
		}
		
		if(i != oldSha1Values.size())
			return i;
		if(i < newSha1Values.size())
			return i;
		
		return -1;
	}
	
	public void changeStoredFile(String storedFileName, String newContent)
	{
		try {
			Files.deleteIfExists(Paths.get(storedFileName));
		} catch (IOException e) {
			System.out.println("Could not delete file " + storedFileName);
			e.printStackTrace();
		}
		realContent = newContent;
		
		
		File file = new File(storedFileName);
		
		PrintWriter pw = null;
		try {
			if(file.getParentFile().exists() == false)
				file.getParentFile().mkdirs();
			
			if(file.exists() == false)
				file.createNewFile();
			
			pw = new PrintWriter(file);
			pw.write(realContent);
		} catch (FileNotFoundException e) {
			System.out.println("Could not create file");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not create file");
			e.printStackTrace();
		}
		pw.close();
		
		isDataInMemory = false;
		realContent = null;
	}
	
	public void retrieveRealDataFromDisk() throws FileDataChanged
	{
		String fullFilePath = storedFileName;
		String content = readFile(fullFilePath);
		int sliceNum;
		
		if((sliceNum=isFileDataUnChanged(content)) != -1)
			throw new FileDataChanged(chunkMetadata.getChunkFileName(), sliceNum);
		realContent = content;
		isDataInMemory = true;
		
		System.out.println("Retrieved chunk data from disk.");
	}
	
	private ArrayList<String> calculateWholeSHA1(String fileContent)
	{
		ArrayList<String> shaValues = new ArrayList<String>();
		int offset = 0, leftCharCount = fileContent.length();
		
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not find algorithm.");
			e.printStackTrace();
		}
		byte[] digested;
		
		while(offset < fileContent.length())
		{
			String subString = fileContent.substring(offset, Math.min(offset + SHA1_INPUT_LEN, offset + leftCharCount));
			offset += subString.length();
			leftCharCount = Math.max(0, leftCharCount - subString.length());
			digested = messageDigest.digest(subString.getBytes());
			shaValues.add(new String(digested));
		}
		
		return shaValues;
	}
	
	private String readFile(String fileName)
	{
		File file = new File(fileName);
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader br;
		int result;
		char ch;
		
		try {
			br = new BufferedReader(new FileReader(file));
			String tempStr;
			
			while ((result = br.read()) != -1)
			{
				ch = (char) result;
				stringBuilder.append(ch);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file " + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not read the file " + fileName);
			e.printStackTrace();
		}
		
		return stringBuilder.toString();
	}
	
	public int getTotalLength()
	{
		return totalLength;
	}
}
