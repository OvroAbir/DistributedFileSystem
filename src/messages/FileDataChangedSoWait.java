package messages;

public class FileDataChangedSoWait extends MessageType {
	private String chunkName;
	private int sliceNum;
	
	public FileDataChangedSoWait(String ChunkName, int sliceNum, String messageFrom) {
		super(FILE_DATA_CHANGED_SO_WAIT_CS_CL, messageFrom);
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
