package exceptions;

public class FileDataChanged extends Exception 
{
	private String chunkName;
	private int sliceNum;
	public FileDataChanged(String chunkName, int sliceNum) 
	{
		super(chunkName + " data has been changed. Integrity compromised of slice " + sliceNum);
		this.chunkName = chunkName;
		this.sliceNum = sliceNum;
	}
	public String getChunkName() {
		return chunkName;
	}
	public int getSliceNum() {
		return sliceNum;
	}

}
