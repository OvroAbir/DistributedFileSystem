package messages;

import java.util.ArrayList;

import control_node.ControlNode;

public class FileStoringChunkServerList extends MessageType 
{
	private ArrayList<String> chunkServerList;
	public FileStoringChunkServerList(ArrayList<String> chunkServerList) {
		super(CHUNK_SERVER_LIST_FOR_STORED_FILE, ControlNode.IP_ADDRESS);
		this.chunkServerList = chunkServerList;
	}
	public ArrayList<String> getChunkServerList() {
		return chunkServerList;
	}

}
