package control_node;

import java.util.HashMap;

public class ChunkServerInfo 
{
	private String ip_adress;
	private int freeSpace;
	
	public ChunkServerInfo(String ip_adress, int freeSpace) 
	{
		this.ip_adress = ip_adress;
		this.freeSpace = freeSpace;
	}
	
	public String toString()
	{
		return ip_adress + " : " + freeSpace + " Bytes";
	}

	public String getIp_adress() {
		return ip_adress;
	}

	public void setIp_adress(String ip_adress) {
		this.ip_adress = ip_adress;
	}

	public int getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(int freeSpace) {
		this.freeSpace = freeSpace;
	}
	
	public void decreaseFreeSpace(int bytes)
	{
		this.freeSpace -= bytes;
		freeSpace = Integer.max(freeSpace, 0);
	}
	
	public boolean hasSpace(int forTheseBytes)
	{
		return freeSpace >= forTheseBytes;
	}
}
