package chunk_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import exceptions.FileDataChanged;

public class Chunk implements Serializable
{
	private ChunkMetadata chunkMetadata;
	private int totalLength;
	private String realContent;
	private String storedFileName;
	private boolean isDataInMemory;
	
	public static int SHA1_INPUT_LEN = 8 * 1024;
	
	public Chunk(String mainFileName, String realContent, int index) 
	{
		this.realContent = realContent;
		
		ArrayList<String> sha1Values = calculateWholeSHA1(realContent);
		chunkMetadata = new ChunkMetadata(mainFileName, index, sha1Values, realContent.length());
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
	
	public void prepareChunkBeforeSendingToClient()
	{
		if(isDataInMemory == false)
		{
			try {
				retrieveRealDataFromDisk();
			} catch (FileDataChanged e) {
				System.out.println("Chunk data has been changed");
				// TODO Request data from another CS
				e.printStackTrace();
			}
		}
	}
	
	public String storeRealDataInDisk(String folderName)
	{
		storedFileName = folderName + File.separator + chunkMetadata.getChunkFileName();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(storedFileName));
			pw.write(realContent);
		} catch (FileNotFoundException e) {
			System.out.println("Could not create file");
			e.printStackTrace();
		}
		pw.close();
		
		isDataInMemory = false;
		realContent = null;
		
		return storedFileName;
	}
	
	private boolean isFileDataUnChanged(String content)
	{
		ArrayList<String> newSha1Values = calculateWholeSHA1(content);
		ArrayList<String> oldSha1Values = chunkMetadata.getSha1Values();
		
		if(content.length() != chunkMetadata.getRealDatalength() || newSha1Values == null || oldSha1Values == null || 
				newSha1Values.size() != oldSha1Values.size())
			return false;
		
		for(int i=0;i<oldSha1Values.size();i++)
		{
			String oldS = oldSha1Values.get(i);
			String newS = newSha1Values.get(i);
			
			if(oldS.equals(newS) == false)
				return false;
		}
		
		return true;
	}
	
	public void retrieveRealDataFromDisk() throws FileDataChanged
	{
		String fullFilePath = storedFileName;
		String content = readFile(fullFilePath);
		if(isFileDataUnChanged(content) == false)
			throw new FileDataChanged();
		realContent = content;
		isDataInMemory = true;
	}
	
	private ArrayList<String> calculateWholeSHA1(String fileContent)
	{
		ArrayList<String> shaValues = new ArrayList<String>();
		int offset = 0;
		
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
			String subString = fileContent.substring(offset, offset + SHA1_INPUT_LEN);
			offset += SHA1_INPUT_LEN;
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
		
		try {
			br = new BufferedReader(new FileReader(file));
			String tempStr;
			
			while ((tempStr = br.readLine()) != null) // TODO change if there is no new line 
			{
				stringBuilder.append(tempStr);
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
