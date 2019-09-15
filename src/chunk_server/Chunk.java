package chunk_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Chunk 
{
	private ChunkMetadata chunkMetadata;
	private int totalLength;
	private String realContent;
	private String storedFileName;
	private boolean isDataOnDisk;
	
	public static int SHA1_INPUT_LEN = 8 * 1024;
	
	public Chunk(String mainFileName, String realContent, int index) 
	{
		this.realContent = realContent;
		
		ArrayList<String> sha1Values = calculateWholeSHA1(realContent);
		chunkMetadata = new ChunkMetadata(mainFileName, index, sha1Values, realContent.length());
		this.isDataOnDisk = false;
	}

	public ChunkMetadata getChunkMetadata() {
		return chunkMetadata;
	}

	public void setChunkMetadata(ChunkMetadata chunkMetadata) {
		this.chunkMetadata = chunkMetadata;
	}
	
	public String storeRealDataInDisk(String folderName)
	{
		storedFileName = folderName + File.separator + chunkMetadata.getMainFileName() + ChunkServer.chunkNameSeperator
				+ chunkMetadata.getChunkIndex();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(storedFileName));
			pw.write(realContent);
		} catch (FileNotFoundException e) {
			System.out.println("Could not create file");
			e.printStackTrace();
		}
		pw.close();
		
		isDataOnDisk = true;
		realContent = null;
		
		return storedFileName;
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
	
}
